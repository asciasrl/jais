package it.ascia.ais;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

public abstract class DevicePort {

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

	private String description;
	
	/**
	 * Momento di ultima variazione del valore
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

	private boolean queuedForUpdate = false;

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		this.pcs.addPropertyChangeListener(listener);
		/*
		logger.trace(pcs.getPropertyChangeListeners().length
				+ " (+) PCL's for " + getFullAddress());
		*/
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		this.pcs.removePropertyChangeListener(listener);
		logger.trace(pcs.getPropertyChangeListeners().length
				+ " (-) PCL's for " + getAddress());
	}

	/**
	 * Check if there are any listeners registered on this port.
	 * 
	 * @return true if there are one or more listeners for this port
	 */
	public boolean hasListeners() {
		return this.pcs.hasListeners(null);
	}

	/**
	 * Legge il valore di durata della cache
	 * 
	 * @return tempo is mS
	 */
	public long getCacheRetention() {
		return cacheRetention;
	}

	/**
	 * Imposta il valore di durata della cache
	 * 
	 * @param cacheRetention
	 *            tempo in mS
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

	public DevicePort(String portId, String portName) {
		this(portId);
		setDescription(portName);
	}

	/**
	 * @param device
	 * @param portId
	 *            se null il default e' connector.address:portId
	 */
	public DevicePort(String portId) {
		logger = Logger.getLogger(getClass());
		this.portId = portId;
		timeStamp = 0;
		expiration = 0;
	}

	public Map<String, Object> getStatus() throws AISException {
		HashMap<String, Object> m = new HashMap<String, Object>();
		m.put("A",getAddress().toString());
		m.put("C",getClass().getSimpleName());
		Object value = getCachedValue();
		if (value == null) {
			m.put("V",value);			
		} else {
			m.put("V",value.toString());
		}
		return m;
	}

	/**
	 * 
	 * @return Name of the port (default = full address)
	 */
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Ritorna il valore della porta. Se il valore non risulta aggiornato,
	 * invoca Device.updatePort() per richiederne l'aggiornamento.
	 * Questo metodo e' sincrono.
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
	 * Legge il valore della porta di un dispositivo fisico Questa
	 * implementazione lavora in modo asincrono, in quanto richiede al device di
	 * aggiornare la porta con il metodo updatePort(portId) ed attende un certo
	 * tempo affinche' la porta venga aggiornata. Il valore della porta deve
	 * essere aggiornato con setValue(), che interrompe subito l'attesa di
	 * readValue() Se il valore non e' aggiornato, restituisce il valore in
	 * cache
	 * 
	 * TODO: updatePort e' sincrona, va quindi cambiata la logica
	 * 
	 * @return
	 */
	public Object readValue() {
		long start = System.currentTimeMillis();
		long timeToWait;
		if (isQueuedForUpdate()) {
			logger.trace("Port already queued for update");
			timeToWait = 300;
		} else {
			timeToWait = device.updatePort(portId);
		}
		if (timeToWait > 0) {
			synchronized (this) {
				if (isDirty() || isExpired()) {
					try {
						wait(timeToWait);
					} catch (InterruptedException e) {
						logger.warn("Thread Interrupted: ", e);
					}
				} else {
					// il valore, nel frattempo, e' stato aggiornato
					return getCachedValue();
				}
			}
		}
		if (isDirty() || isExpired()) {
			logger.error("Non aggiornato valore porta " + getAddress()
					+ " in " + (System.currentTimeMillis() - start) + "mS");
		}
		return getCachedValue();
	}

	/**
	 * Aggiorna la porta con il valore effettivo
	 * 
	 * @param newValue
	 */
	public void setValue(Object newValue) {
		Object oldValue = getCachedValue();
		boolean changed = false; 
		if (isDirty() || oldValue == null || !oldValue.equals(newValue)) {
			timeStamp = System.currentTimeMillis();
			changed = true;
		}
		synchronized (this) {
			expiration = System.currentTimeMillis() + cacheRetention;
			setCachedValue(newValue);
			dirty = false;
			// sveglia getValue()
			notify();
		}
		if (changed) {
			DevicePortChangeEvent evt = new DevicePortChangeEvent(this, oldValue,getCachedValue());
			fireDevicePortChangeEvent(evt);
		}
	}

	/**
	 * Aggiorna la porta con il valore effettivo e dichiara fino a quanto tempo
	 * il dato puo' essere ritenuto valido
	 * 
	 * @param newValue
	 * @param duration
	 *            durata della cache in mS
	 */
	public void setValue(Object newValue, long duration) {
		setValue(newValue);
		setDuration(duration);
	}

	/**
	 * Scrive un nuovo valore sulla porta del device cui appartiene Questa
	 * implementazione richiama {@link #Device.sendPortValue()} quindi invalida
	 * il valore in cache Se la porta e' virtuale, la sottoclasse deve gestire
	 * la richiesta di scrittura in maniera specifica
	 * 
	 * @param newValue
	 * @return true se scrittura effettuata correttamente
	 * @throws AISException
	 */
	public boolean writeValue(Object newValue) {
		if (device.sendPortValue(portId, normalize(newValue))) {
			invalidate();
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Subclasses must ovveride this method to normalize and check values.
	 * If newValue is null, return null
	 * If newValue is an instance of the class of port, return newValue.
	 * This metod try to convert supplied value to the class of port value.
	 * Can check boundaries or list of values. 
	 * @param newValue The value to be checked and converted
	 * @return Normalized (converted) value
	 * @throws IllegalArgumentException if the argument cannot be converted
	 */
	protected abstract Object normalize(Object newValue) throws IllegalArgumentException;
	
	/**
	 * Propaga l'evento di modifica a tutti i listener registrati ed al device
	 * cui appartiene
	 * 
	 * @param evt
	 */
	public void fireDevicePortChangeEvent(DevicePortChangeEvent evt) {
		if (evt.getOldValue() == null || evt.getNewValue() == null || !evt.getNewValue().equals(evt.getOldValue())) {
			logger.info(evt);
		} else {
			logger.trace(evt);
		}
		pcs.firePropertyChange(evt);
	}

	/**
	 * Indica che il valore contenuto nella cache non e' valido, cioe' potrebbe
	 * non essere aggiornato
	 */
	public void invalidate() {
		if (!dirty) {
			logger.trace("Invalidate " + getAddress());
		}
		dirty = true;
		queueUpdate();
	}

	private void setCachedValue(Object newValue) {
		cachedValue = normalize(newValue);
	}

	/**
	 * @return Valore in cache della porta
	 */
	public Object getCachedValue() {
		return cachedValue;
	}

	/**
	 * Questo metodo gestisce la logica di caching Se ritorna true vuol dire che
	 * il valore non si deve ritenere aggiornato
	 * 
	 * @return
	 */
	public boolean isDirty() {
		return dirty;
	}

	/**
	 * 
	 * @return true se il valore in cache e' scaduto
	 */
	public boolean isExpired() {
		return System.currentTimeMillis() >= expiration;
	}

	public Address getAddress() {
		Address a = device.getAddress();
		a.setPortId(portId);
		return a;
	}

	/**
	 * Imposta il momento di scadenza in modo che scada entro il tempo
	 * specificato Se il valore ha una durata residua minore di quella
	 * specificata, non viene modificata
	 * 
	 * @param i
	 *            Tempo di durata in mS del valore in cache ( deve essere > 0)
	 */
	public void setDuration(long i) {
		if (i > 0) {
			expiration = Math.min(expiration, System.currentTimeMillis() + i);
			logger.trace(getAddress() + " set duration " + i + "mS");
		}
	}

	/**
	 * Fornisce il momento in cui risulta l'ultima modifica del valore
	 * 
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

	public void queueUpdate() {
		if (!isQueuedForUpdate()) {
			device.getConnector().queueUpdate(this);
		}
	}
	
	public String toString() {
		return getClass().getSimpleName() + " " + getAddress();
	}

	/**
	 * Aggiorna il valore della porta, delegando al device
	 * @return
	 */
	public long update() {
		return device.updatePort(portId);		
	}

	public void setQueuedForUpdate() {
		queuedForUpdate = true;
	}
	
	public void resetQueuedForUpdate() {
		queuedForUpdate = false;
	}

	public boolean isQueuedForUpdate() {
		return queuedForUpdate;
	}

	/**
	 * Used by Device.addPort() to set the device to which belong
	 * @param device
	 */
	public void setDevice(Device device) {
		this.device = device;		
	}

}
