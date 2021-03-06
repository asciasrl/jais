package it.ascia.ais;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.security.InvalidParameterException;

import org.apache.log4j.Logger;

public class DevicePort {
	
	protected String portId;
	
	public String getPortId() {
		return portId;
	}

	private Object cachedValue;
	
	private boolean dirty = true;
	
	protected Device device;

	public Device getDevice() {
		return device;
	}

	private String portName;
	
	/**
	 * Tempo di ultimo aggiornamento del valore
	 */
	private long timeStamp;

	protected Logger logger;

	public static long DEFAULT_CACHE_RETENTION = 60000;

	private long cacheRetention = DEFAULT_CACHE_RETENTION;

	/**
	 * Tempo fino a cui e' valido il valore in cache
	 */
	private long expiration;

	protected final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

	public void addPropertyChangeListener( PropertyChangeListener listener )
    {
        this.pcs.addPropertyChangeListener( listener );
		logger.trace(pcs.getPropertyChangeListeners().length + " (+) PCL's for "+getFullAddress());
    }

    public void removePropertyChangeListener( PropertyChangeListener listener )
    {
        this.pcs.removePropertyChangeListener( listener );
		logger.trace(pcs.getPropertyChangeListeners().length + " (-) PCL's for "+getFullAddress());
    }

	/**
	 * Check if there are any listeners registered on this port.
	 * @return true if there are one or more listeners for this port
	 */
    public boolean hasListeners() {
    	return this.pcs.hasListeners(null);
    }
		
    /**
	 * Legge il valore di durata della cache
     * @return tempo is mS
     */
	public long getCacheRetention() {
		return cacheRetention;
	}

	/**
	 * Imposta il valore di durata della cache
	 * @param cacheRetention tempo in mS
	 */
	public void setCacheRetention(long cacheRetention) {
		this.cacheRetention = cacheRetention;
	}
	
	/**
	 * Imposta la durata della cache al valore di default 
	 */
	public void resetCacheRetention() {
		setCacheRetention(DEFAULT_CACHE_RETENTION);		
	}


	public DevicePort(Device device, String portId) {
		this(device, portId, null);	
	}	
	
	/**
	 * @param device
	 * @param portId
	 * @param portName se null il default � connector.address:portId
	 */
	public DevicePort(Device device, String portId, String portName) {
		logger = Logger.getLogger(getClass());
		this.device = device;
		this.portId = portId;
		if (portName == null) {
			this.portName = device.getFullAddress()+":"+portId;
		} else {
			this.portName = portName;
		}
		timeStamp = 0;
		expiration = 0;
	}
		
	public String getStatus() throws AISException {
		return getFullAddress() + "=" + getCachedValue();
	}

	public String getName() {
		return portName;
	}

	public void setName(String portName) {
		this.portName = portName;
	}

	/**
	 * Ritorna il valore della porta.  Se il valore non risulta aggiornato, invoca Device.updatePort() per richiederne l'aggiornamento.
	 * Questo metodo e' sincronizzato con setValue, che aggiorna il valore della porta.
	 * 
	 * @return Oggetto memorizzato in cache o null
	 * @throws AISException 
	 */
	public Object getValue() throws AISException {
		if (isDirty() || isExpired()) {
			setCachedValue(readValue());
		}
		return getCachedValue();
	}
	
	/**
	 * Legge il valore della porta di un dispositivo fisico
	 * Questa implementazione lavora in modo asincrono, in quanto richiede al device
	 * di aggiornare la porta con il metodo updatePort(portId) ed attende un certo tempo 
	 * affinche' la porta venga aggiornata.
	 * Il valore della porta deve essere aggiornato con setValue(), che interrompe
	 * subito l'attesa di readValue()
	 * Se il valore non e' aggiornato, restituisce il valore in cache
	 * @return
	 */
	protected Object readValue() {
		long start = System.currentTimeMillis();
		long timeout = device.updatePort(portId);
		if (timeout > 0) {
			synchronized (this) {
				if (isDirty() || isExpired()) {
					try {
						wait(timeout);
					} catch (InterruptedException e) {
						logger.warn("Interrotto: ",e);
					}
				} else {
					// il valore, nel frattempo, e' stato aggiornato
					return cachedValue;
				}
			}
		}	
		if (isDirty() || isExpired()) {
			logger.error("Non aggiornato valore porta "+getFullAddress()+" in "+(System.currentTimeMillis()-start)+"mS");
		}
		return cachedValue;
	}

	/**
	 * Aggiorna la porta con il valore effettivo  
	 * @param newValue
	 */
	public void setValue(Object newValue) {
		Object oldValue = getCachedValue();
		if (isDirty() || oldValue == null || ! oldValue.equals(newValue)) {
			timeStamp = System.currentTimeMillis();
		}
		synchronized (this) {
			expiration = System.currentTimeMillis() + cacheRetention;
			cachedValue = newValue;
			dirty = false;
			// sveglia getValue()
			notify();								
		}
		fireDevicePortChangeEvent(new DevicePortChangeEvent(this,oldValue,newValue));
	}

	/**
	 * Aggiorna la porta con il valore effettivo e dichiara fino a quanto tempo il dato puo' essere ritenuto valido
	 * @param newValue 
	 * @param duration durata della cache in mS
	 */
	public void setValue(Object newValue, long duration) {
		setValue(newValue);
		setDuration(duration);
	}
	
	/**
	 * Scrive un nuovo valore sulla porta del device cui appartiene
	 * Questa implementazione richiama {@link #Device.writePort()} dopo aver invalidato il valore in cache
	 * Se la porta e' virtuale, la sottoclasse deve gestire la richiesta di scrittura in maniera specifica 
	 * @param newValue
	 * @return true se scrittura effettuata correttamente
	 * @throws AISException 
	 */
	public boolean writeValue(Object newValue) {
		// FIXME Aggiungere setValue(newValue); ?
		invalidate();
		return device.sendPortValue(portId, newValue);
	}
	
	/**
	 * Imposta il valore della porta analizzando il testo fornito 
	 */
	public boolean writeValue(String newValue) {
		return writeValue((Object) newValue);
	}
	
	/**
	 * Propaga l'evento di modifica a tutti i listener registrati ed al device cui appartiene
	 * @param evt
	 */
	public void fireDevicePortChangeEvent(DevicePortChangeEvent evt) {
		this.pcs.firePropertyChange(evt);
		device.fireDevicePortChangeEvent(evt);
	}
			
	/**
	 * Indica che il valore contenuto nella cache non e' valido, cioe' potrebbe non essere aggiornato
	 */
	public void invalidate() {
		if (!dirty) {
			logger.trace("Invalidate "+getFullAddress());
		}
		dirty = true;
	}

	public void setCachedValue(Object newValue) {
		cachedValue = newValue;		
	}

	/**
	 * @return Valore in cache della porta
	 */
	public Object getCachedValue() {
		return cachedValue;
	}
	
	/**
	 * Questo metodo gestisce la logica di caching
	 * Se ritorna true vuol dire che il valore non si deve ritenere
	 * aggiornato
	 * @return
	 */
	public boolean isDirty()
	{
		return dirty;
	}

	/**
	 * 
	 * @return true se il valore in cache e' scaduto
	 */
	public boolean isExpired() {
		return System.currentTimeMillis() >= expiration;
	}

	/**
	 * @return indirizzo completo della porta
	 */
	public String getFullAddress() {
		return device.getFullAddress()+":"+portId;
	}

	/**
	 * Imposta il momento di scadenza in modo che scada entro il tempo specificato
	 * Se il valore ha una durata residua minore di quella specificata, non viene modificata
	 * @param i Tempo di durata in mS del valore in cache
	 */
	public void setDuration(long i) {
		if (i < 0) {
			throw(new InvalidParameterException());
		}
		expiration = Math.min(expiration,System.currentTimeMillis() + i);
		logger.trace(getFullAddress()+" set duration "+i+"mS");
	}

	/**
	 * Fornisce il momento in cui risulta l'ultima modifica del valore
	 * @return
	 */
	public long getTimeStamp() {
		return timeStamp;
	}

	/**
	 * Ritorna la rappresentazione testuale del valore
	 */
	public String getAsText() {
		return getValue().toString();
	}

	/**
	 * Se il valore deve essere uno dei valori di un insieme, questo metodo ne fornisce l'elenco  
	 */
	public String[] getTags() {
		return null;
	}

}
