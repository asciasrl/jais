/**
 * Copyright (C) 2008 ASCIA S.R.L.
 */
package it.ascia.eds;

import it.ascia.eds.device.Device;

/**
 * @author arrigo
 *
 * Il controller di uno o piu' bus.
 * 
 * Il suo lavoro e' di rispondere ai comandi di AUI, attraverso il metodo
 * receiveRequest().
 */
public class BusController {
	/**
	 * Il nostro interprete di indirizzi.
	 */
	BusAddressParser addressParser;
	/**
	 * Il nostro bus.
	 */
	Bus bus;
	
	public BusController(Bus bus) {
		addressParser = new BusAddressParser();
		addressParser.registerBus(bus, 0);
		this.bus = bus;
	}
	
	/**
	 * Il cuore del controllore: riceve la richiesta e produce una risposta.
	 */
	public String receiveRequest(String command, String name, String value) {
		String retval;
		System.out.println("Comando: \"" + command + "\" \"" + name + "\" \"" +
				value + "\"");
		if (command.equals("get")) {
			// Comando "get"
			try {
				BusAddress address = addressParser.parseAddress(name);
				Device[] devices = address.getDevices();
				retval = "";
				for (int i = 0; i < devices.length; i++) {
					retval += devices[i].getStatus(address.getPorts());
				}
			} catch (EDSException e) {
				retval = "ERROR: " + e.getMessage();
			}
		} else if (command.equals("getAll")) {
			// Comando "getAll"
			retval = "";
			Device[] devices = bus.getDevices();
			for (int i = 0; i < devices.length; i++) {
				retval += devices[i].getStatus("*");
			}
		} else {
			retval = "ERROR: Unknown command \"" + command + "\".";
		}
		return retval;
	}
}
