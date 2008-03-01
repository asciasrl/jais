/**
 * Copyright (C) 2008 ASCIA S.r.l.
 */
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;

import it.ascia.ais.Connector;
import it.ascia.ais.AISException;
import it.ascia.ais.BusAddress;
import it.ascia.ais.BusAddressParser;
import it.ascia.ais.Controller;
import it.ascia.ais.Device;
import it.ascia.ais.DeviceEvent;
import it.ascia.ais.DeviceListener;

/**
 * Il controller di uno o piu' connector.
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
public class MyController implements Controller, DeviceListener {
	/**
	 * Il nostro interprete di indirizzi.
	 */
	private BusAddressParser addressParser;
	/**
	 * I nostri connector.
	 */
	private Set connectors;
	/**
	 * Il nostro logger.
	 */
	private Logger logger;
	/**
	 * Il nostro PIN.
	 */
	private String pin;
	
	/**
	 * Costruttore.
	 * 
	 * @param connector il connector che controlliamo.
	 * @param pin il pin da richiedere; se null, qualunque pin verra' accettato.
	 */
	public MyController(String pin) {
		this.logger = Logger.getLogger(getClass());
		addressParser = new BusAddressParser();
		this.pin = pin;
		connectors = new HashSet();
	}
	
	public void addConnector(Connector connector) {
		connectors.add(connector);
		addressParser.registerBus(connector);
	}

	/**
	 * Associa se stesso a tutti i device di tutti i connector.
	 */
	public void setDevicesListener() {
		Iterator it = connectors.iterator();
		while (it.hasNext()) {
			Device devices[] = ((Connector)it.next()).getDevices();
			for (int i = 0; i < devices.length; i++) {
				devices[i].setDeviceListener(this);
				logger.info(String.valueOf(devices[i].getAddress()));
			}
		}
	}
	
	/**
	 * Si auto-invia una richiesta.
	 * 
	 * <p>Questo metodo e' per uso interno: esegue una richiesta senza
	 * controllare il pin.</p>
	 */
	private String receiveAuthenticatedRequest(String command, String name,
			String value) {
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
			Iterator it = connectors.iterator();
			retval = "";
			while (it.hasNext()) {
				Device[] devices = ((Connector)it.next()).getDevices();
				for (int i = 0; i < devices.length; i++) {
					retval += devices[i].getStatus("*");
				}
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
	 * Il cuore del controllore: riceve la richiesta e produce una risposta.
	 */
	public String receiveRequest(String command, String name, String value,
			String pin) {
		if ((this.pin != null) && (!this.pin.equals(pin))) {
			logger.warn("Richiesta con PIN errato:  \"" + command + "\" \"" +
					name + "\" \"" + value + "\"");
			return "ERROR: PIN errato.";
		}
		return receiveAuthenticatedRequest(command, name, value);
	}

	public void statusChanged(DeviceEvent event) {
		logger.trace(event.getInfo());
		// Esempio: accendiamo un dimmer
		Device device = event.getDevice();
		if (device.getAddress() == 1) {
			logger.info("Comando da BMC virtuale");
			receiveAuthenticatedRequest("set", "0.5:Out2", "100");
		}
	}
}
