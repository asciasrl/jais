/**
 * Copyright (C) 2008 ASCIA S.r.l.
 */
package it.ascia.ais;

import java.beans.PropertyChangeEvent;

/**
 * Evento generato da Device: una porta ha cambiato valore.
 * 
 * @author sergio
 *
 */
@SuppressWarnings("serial")
public class DevicePortChangeEvent extends PropertyChangeEvent {

	private long timeStamp;

	/**
	 * Costruttore.
	 * @param devicePort porta che ha cambiato valore.
	 * @param oldValue valore precedente della porta.
	 */
	public DevicePortChangeEvent(DevicePort devicePort, Object oldValue, Object newValue) {
		super(devicePort, devicePort.getFullAddress(), oldValue, newValue);
		timeStamp = System.currentTimeMillis();
	}
	
	/**
	 * @return the device of the port
	 * @deprecated
	 */
	public Device getDevice() {		
		return ((DevicePort)getSource()).getDevice();
	}

	/**
	 * @return the port fullAddress
	 */
	public String getFullAddress() {
		return getPropertyName();
	}
	
	public String toString() {
		return getFullAddress()+"@"+getTimeStamp()+" " + getOldValue() + " -> " + getNewValue();
	}

	/**
	 * @return the timeStamp
	 */
	public long getTimeStamp() {
		return timeStamp;
	}
	
}
