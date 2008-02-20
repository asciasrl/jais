/**
 * Copyright (C) 2008 ASCIA S.R.L.
 */
package it.ascia.ais;

import org.apache.log4j.Logger;

import it.ascia.ais.Bus;
import it.ascia.ais.AISException;

/**
 * Il controller di uno o piu' bus.
 * 
 * <p>Il suo lavoro e':</p>
 * <ul>
 * <li>rispondere ai comandi di AUI, attraverso il metodo receiveRequest();</li>
 * <li>reagire agli allarmi comunicati dalla centralina;</li>
 * <li>reagire alle variazioni di stato dei dispositivi "finti" (ad es. BMC
 * standard I/O).</li>
 * </ul>
 * 
 * @author arrigo
 */
public class BusController implements AlarmReceiver, VirtualDeviceListener {
	/**
	 * Il nostro interprete di indirizzi.
	 */
	private BusAddressParser addressParser;
	/**
	 * Il nostro bus.
	 */
	private Bus bus;
	/**
	 * Il nome del nostro bus.
	 */
	private String busName;
	/**
	 * Il nostro logger.
	 */
	private Logger logger;
	
	/**
	 * Costruttore.
	 * 
	 * @param bus il bus che controlliamo.
	 */
	public BusController(Bus bus) {
		this.logger = Logger.getLogger(getClass());
		addressParser = new BusAddressParser();
		addressParser.registerBus(bus, 0);
		this.bus = bus;
		this.busName = bus.getName();
	}
	
	/**
	 * Il cuore del controllore: riceve la richiesta e produce una risposta.
	 */
	public String receiveRequest(String command, String name, String value) {
		String retval;
		logger.trace("Comando: \"" + command + "\" \"" + name + "\" \"" +
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
				if (retval.length() == 0) {
					retval = "ERROR: address " + address.getAddress() + 
						" not found.";
				}
			} catch (AISException e) {
				retval = "ERROR: " + e.getMessage();
			}
		} else if (command.equals("getAll")) {
			// Comando "getAll"
			retval = "";
			Device[] devices = bus.getDevices();
			for (int i = 0; i < devices.length; i++) {
				retval += devices[i].getStatus("*");
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
			} catch (AISException e) {
				retval = "ERROR: " + e.getMessage();
			}
		} else {
			retval = "ERROR: Unknown command \"" + command + "\".";
		}
		return retval;
	}

	/**
	 * Reagisce a un allarme.
	 * 
	 * <p>Questo metodo viene invocato dall'oggetto centralina.</p>
	 */
	public void alarmReceived(String alarm) {
		// Esempio: accendiamo un dimmer
		logger.info("Ricevuto allarme");
		receiveRequest("set", "0.5:Out1", "100");
	}

	/**
	 * Reagisce al cambiamento di stato di un Device virtuale.
	 */
	public void statusChanged(Device device, String port, String newValue) {
		// Esempio: accendiamo un dimmer
		if (device.getAddress() == 1) {
			logger.info("Comando da BMC virtuale");
			receiveRequest("set", "0.5:Out2", "100");
		}
	}
}
