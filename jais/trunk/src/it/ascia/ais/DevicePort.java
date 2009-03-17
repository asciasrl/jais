package it.ascia.ais;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

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
	
	private long timestamp;

	private Logger logger;

	private long cacheRetention = 60000;
	
	public long getCacheRetention() {
		return cacheRetention;
	}

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
		timestamp = 0;
	}
		
	public String getStatus() throws AISException {
		return getFullAddress() + "=" + getValue();
	}

	public long getTimestamp() {
		return timestamp;
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
		}
		timestamp = System.currentTimeMillis();
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
		if ((System.currentTimeMillis() - timestamp) > cacheRetention ) {
			logger.trace("Expired "+getFullAddress()+" "+((1.0 + System.currentTimeMillis()-timestamp)/1000.0));
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
	 * Imposta il timestamp in modo che scada dopo un tempo prefissato
	 * @param i
	 */
	public void expire(long i) {
		timestamp = System.currentTimeMillis() - cacheRetention + i;
	}

	public long getTimeStamp() {
		return timestamp;
	}

}
