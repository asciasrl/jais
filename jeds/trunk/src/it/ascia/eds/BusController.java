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
	/**
	 * Il nome del nostro bus.
	 */
	String busName;
	
	/**
	 * Costruttore.
	 * 
	 * @param bus il bus che controlliamo.
	 * @param busName il nome del bus che controlliamo.
	 */
	public BusController(Bus bus) {
		addressParser = new BusAddressParser();
		addressParser.registerBus(bus, 0);
		this.bus = bus;
		this.busName = "0";
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
					retval += devices[i].getStatus(address.getPorts(),
							address.getBusName());
				}
				if (retval.length() == 0) {
					retval = "ERROR: address " + address.getAddress() + 
						" not found.";
				}
			} catch (EDSException e) {
				retval = "ERROR: " + e.getMessage();
			}
		} else if (command.equals("getAll")) {
			// Comando "getAll"
			retval = "";
			Device[] devices = bus.getDevices();
			for (int i = 0; i < devices.length; i++) {
				retval += devices[i].getStatus("*", busName);
			}
		} else if (command.equals("set")) {
			// Comando "set"
			try {
				BusAddress address = addressParser.parseAddress(name);
				Device[] devices = address.getDevices();
				if (devices.length == 1) {
					Device device = devices[0];
					device.setPort(address.getPorts(), value);
					retval = "OK";
				} else {
					retval = "ERROR: indirizzo ambiguo";
				}
			} catch (EDSException e) {
				retval = "ERROR: " + e.getMessage();
			}
		} else {
			retval = "ERROR: Unknown command \"" + command + "\".";
		}
		return retval;
	}
}
