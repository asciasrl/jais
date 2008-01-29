/**
 * Copyright (C) 2008 ASCIA S.R.L.
 */
package it.ascia.eds;

import it.ascia.eds.device.Device;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * @author arrigo
 * L'indirizzo di una o piu' porte su un bus.
 * 
 * Lo scopo di questa classe e' di risalire ai Device e alle porte indicate 
 * attraverso il protocollo di comunicazione di AUI.
 */
public class BusAddress {
	/**
	 * Indirizzo indicato dal protocollo.
	 */
	private String address;
	/**
	 * Il nome del bus.
	 */
	private String busName;
	/**
	 * L'indirizzo del dispositivo sul bus.
	 */
	private String deviceAddress;
	/**
	 * Le porte interessate (su tutti i device trovati).
	 */
	private String ports;
	/**
	 * I device trovati. 
	 */
	private Set devices; 
	
	/**
	 * Costruttore.
	 * 
	 * @param address l'indirizzo indicato dall'interfaccia.
	 * @param busName il nome del bus estratto dall'indirizzo.
	 * @param bus il bus identificato da busName.
	 * @param deviceAddress l'indirizzo del device estratto dall'indirizzo.
	 * @param ports le porte del device interessate.
	 */
	public BusAddress(String address, String busName, String deviceAddress,
			String ports) throws EDSException {
		this.address = address;
		this.busName = busName;
		this.deviceAddress = deviceAddress;
		this.ports = ports;
		devices = new HashSet();
	}
	
	/**
	 * Aggiunge un device al set di quelli corrispondenti a questo indirizzo.
	 */
	public void addDevice(Device d) {
		devices.add(d);
	}
	
	/**
	 * Ritorna l'indirizzo completo, come specificato da AUI.
	 */
	public String getAddress() {
		return address;
	}
	
	/**
	 * Ritorna il nome del bus.
	 */
	public String getBusName() {
		return busName;
	}

	/**
	 * Ritorna l'indirizzo del device.
	 */
	public String getDeviceAddress() {
		return deviceAddress;
	}

	/**
	 * Ritorna le porte specificate nell'indirizzo.
	 */
	public String getPorts() {
		return ports;
	}
	
	/**
	 * Ritorna i device interessati da questo indirizzo.
	 */
	public Device[] getDevices() {
		int i = 0;
		Device retval[] = new Device[devices.size()];
		Iterator it = devices.iterator();
		while (it.hasNext()) {
			retval[i] = (Device)it.next();
			i++;
		}
		return retval;
	}
}
