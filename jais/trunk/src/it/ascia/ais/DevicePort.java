package it.ascia.ais;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.apache.log4j.Logger;

public class DevicePort {
	
	protected String portId;
	
	public String getPortId() {
		return portId;
	}

	protected Object cachedValue;
	
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
    }

    public void removePropertyChangeListener( PropertyChangeListener listener )
    {
        this.pcs.removePropertyChangeListener( listener );
    }
		
	public long getCacheRetention() {
		return cacheRetention;
	}

	/**
	 * Imposta il valore di durata della cache
	 * @param cacheRetention
	 */
	public void setCacheRetention(long cacheRetention) {
		this.cacheRetention = cacheRetention;
	}

	public DevicePort(Device device, String portId) {
		this(device, portId, null);	
	}	
	
	/**
	 * @param device
	 * @param portId
	 * @param portName se null il default è connector.address:portId
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
		return getFullAddress() + "=" + getValue();
	}

	public String getName() {
		return portName;
	}

	public void setName(String portName) {
		this.portName = portName;
	}

	/**
	 * Ritorna il valore della porta.  Se il valore non risulta aggiornato, invoca Device.updatePort() per richiederne l'aggiornamento.
	 * Questo metodo e' sincronizzato che setValue, che aggiorna il valore della porta
	 * 
	 * @return
	 * @throws AISException 
	 */
	public Object getValue() throws AISException {
		if (isDirty() || isExpired()) {
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
		}
		return cachedValue;
	}
	
	/**
	 * Aggiorna la porta con il valore effettivo  
	 * @param newValue
	 */
	public void setValue(Object newValue) {
		Object oldValue = getCachedValue();
		//logger.trace("setValue "+getFullAddress()+"="+oldValue+" -> "+newValue);
		boolean changed = false;
		if (isDirty() || oldValue == null || ! oldValue.equals(newValue)) {
			changed = true;
			timeStamp = System.currentTimeMillis();
		}
		synchronized (this) {
			expiration = System.currentTimeMillis() + cacheRetention;
			cachedValue = newValue;
			dirty = false;
			// sveglia getValue()
			notify();								
		}
		if (changed) {
			/**
			 * Se i due valori sono uguali vuol dire che e' avvenuto un cambiamento che non abbiamo potuto verificare.
			 * Vengono inviati due eventi:  valore -> null e null -> valore
			 * Senza questo artificio non verrebbe notificato nessun evento ai listener. 
			 */
			DevicePortChangeEvent evt;
			if (oldValue != null && oldValue.equals(newValue)) {
				evt = new DevicePortChangeEvent(this,oldValue,null);
				fireDevicePortChangeEvent(evt);				
				evt = new DevicePortChangeEvent(this,null,newValue);
				fireDevicePortChangeEvent(evt);				
			} else {
				evt = new DevicePortChangeEvent(this,oldValue,newValue);
				fireDevicePortChangeEvent(evt);
			}
		}
	}
	
	public void fireDevicePortChangeEvent(DevicePortChangeEvent evt) {
		this.pcs.firePropertyChange(evt);
		device.fireDevicePortChangeEvent(evt);
	}
			
	/**
	 * Indica che il valore contenuto nella cache non è valido
	 */
	public void invalidate() {
		if (!dirty) {
			logger.trace("Invalidate "+getFullAddress());
		}
		dirty = true;
	}

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

	public boolean isExpired() {
		return System.currentTimeMillis() >= expiration;
	}

	/**
	 * Restituisce l'indirizzo completo della porta
	 * @return
	 */
	public String getFullAddress() {
		return device.getFullAddress()+":"+portId;
	}

	/**
	 * Imposta il momento di scadenza in modo che scada dopo il tempo specificato
	 * @param i
	 */
	public void setDuration(long i) {
		expiration = System.currentTimeMillis() + i;
	}

	public long getTimeStamp() {
		return timeStamp;
	}

}
