/**
 * Copyright (C) 2008 ASCIA S.r.l.
 */
package it.ascia.ais;

/**
 * Evento generato da Device: una porta ha cambiato valore.
 * 
 * @author arrigo
 * TODO Usare l'oggetto DevicePort al posto di DeviceEvent
 *
 */
public class DeviceEvent {
	/**
	 * Device che ha generato l'evento.
	 */
	private Device device;
	
	/**
	 * Porta del device che ha cambiato valore.
	 */
	private String portId;
		
	/**
	 * Nuovo valore assunto dalla porta del device.
	 */
	private Object value;
	
	/**
	 * Costruttore.
	 * @param device device che genera l'evento.
	 * @param port porta che ha cambiato valore.
	 * @param 
	 * @param newValue nuovo valore assunto dalla porta.
	 */
	public DeviceEvent(Device device, String portId, Object newValue) {
		this.device = device;
		this.portId = portId;
		this.value = newValue;
	}

	/**
	 * @return the device
	 */
	public Device getDevice() {
		return device;
	}

	/**
	 * @return the port
	 */
	public String getPort() {
		return portId;
	}

	/**
	 * @return the newValue
	 */
	public Object getValue() {
		return value;
	}
	
	/**
	 * Restituisce una rappresentazione leggibile dell'evento.
	 * 
	 * @return una stringa: "indirizzo_device:porta = nuovo_valore".
	 */
	public String getInfo() {
		return device.getFullAddress() + ":" + getPort() + " = " + getValue();
	}

}
