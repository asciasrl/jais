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
	 * @return the device
	 */
	public Device getDevice() {
		return ((DevicePort)source).getDevice();
	}

	/**
	 * @return the port fullAddress
	 */
	public String getFullAddress() {
		return ((DevicePort)source).getFullAddress();
	}
	
	public String toString() {
		return getFullAddress()+"@"+getTimeStamp()+"="+getNewValue();
	}

	/**
	 * @return the timeStamp
	 */
	public long getTimeStamp() {
		return timeStamp;
	}
	
}
