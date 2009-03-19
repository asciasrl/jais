package it.ascia.ais;

import org.apache.log4j.Logger;

public class DevicePort {
	
	private String portId;
	
	public String getPortId() {
		return portId;
	}

	private Object cachedValue;
	
	private boolean dirty = true;
	
	private Device device;

	public Device getDevice() {
		return device;
	}

	private String portName;
	
	/**
	 * Tempo di ultimo aggiornamento del valore
	 */
	private long timeStamp;

	private Logger logger;

	public static long DEFAULT_CACHE_RETENTION = 60000;

	private long cacheRetention = DEFAULT_CACHE_RETENTION;

	/**
	 * Tempo fino a cui e' valido il valore in cache
	 */
	private long expiration;
	
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
	 * @param portName se null il default è <connector>.<address>:<portId>
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
			long timeout = device.updatePort(portId);
			if (isDirty()) {
				synchronized (this) {
					try {
						wait(timeout);
					} catch (InterruptedException e) {
					}
				}
			}	
			if (isDirty() || isExpired()) {
				logger.error("Non aggiornato valore porta "+getFullAddress());
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
		expiration = System.currentTimeMillis() + cacheRetention;
		cachedValue = newValue;
		dirty = false;
		// sveglia getValue()
		synchronized (this) {
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
				device.fireDevicePortChangeEvent(evt);				
				evt = new DevicePortChangeEvent(this,null,newValue);
				device.fireDevicePortChangeEvent(evt);				
			} else {
				evt = new DevicePortChangeEvent(this,oldValue,newValue);
				device.fireDevicePortChangeEvent(evt);
			}
		}
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
		if (System.currentTimeMillis() >= expiration ) {
			logger.trace("Expired by "+getFullAddress()+" "+(1.0 + System.currentTimeMillis() - expiration)/1000.0+"s");
			return true;
		} else {
			return false;
		}
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
