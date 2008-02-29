/**
 * Copyright (C) 2008 ASCIA S.r.l.
 */
package it.ascia.ais;

/**
 * Evento generato da Device: una porta ha cambiato valore.
 * 
 * @author arrigo
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
	private String port;
	
	/**
	 * Nuovo valore assunto dalla porta del device.
	 */
	private String newValue;
	
	/**
	 * Costruttore.
	 * @param device device che genera l'evento.
	 * @param port porta che ha cambiato valore.
	 * @param newValue nuovo valore assunto dalla porta.
	 */
	public DeviceEvent(Device device, String port, String newValue) {
		this.device = device;
		this.port = port;
		this.newValue = newValue;
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
		return port;
	}

	/**
	 * @return the newValue
	 */
	public String getNewValue() {
		return newValue;
	}
	
	/**
	 * Restituisce una rappresentazione leggibile dell'evento.
	 * 
	 * @return una stringa: "indirizzo_device:porta = nuovo_valore".
	 */
	public String getInfo() {
		return device.getConnector().getName() + "." + 
			device.getAddress() + ":" + getPort() + " = " + getNewValue();
	}
}
