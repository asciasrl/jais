/**
 * Copyright (C) 2008 ASCIA S.r.l.
 */
import org.apache.log4j.Logger;

import it.ascia.ais.Connector;
import it.ascia.ais.AISException;
import it.ascia.ais.Controller;
import it.ascia.ais.Device;
import it.ascia.ais.DeviceEvent;

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
public class MyController extends Controller {
	/**
	 * Il nostro logger.
	 */
	protected Logger logger;
	/**
	 * Il nostro PIN.
	 */
	private String pin;
	
	/**
	 * Costruttore.
	 * 
	 * @param pin il pin da richiedere; se null, qualunque pin verra' accettato.
	 */
	public MyController(String pin) {
		logger = Logger.getLogger(getClass());
		this.pin = pin;
	}
	
	/**
	 * Si auto-invia una richiesta.
	 * 
	 * <p>Questo metodo e' per uso interno: esegue una richiesta senza
	 * controllare il pin.</p>
	 * TODO spostare in HTTPServerControllerPlugin
	 */
	private String receiveAuthenticatedRequest(String command, String name,
			String value) {
		String retval;
		logger.debug("Comando: \"" + command + "\" \"" + name + "\" \"" +
				value + "\"");
		if (command.equals("get")) {
			// Comando "get"
			try {
				String deviceAddress = getDeviceFromAddress(name);
				String portName = getPortFromAddress(name);
				Device devices[] = findDevices(deviceAddress);
				if (devices.length > 0) {
					retval = "";
					for (int i = 0; i < devices.length; i++) {
						retval += devices[i].getStatus(portName, 0);
					}
				} else {
					retval = "ERROR: address " + name + " not found.";
				}
			} catch (AISException e) {
				retval = "ERROR: " + e.getMessage();
			}
		} else if (command.equals("getAll")) {
			// Comando "getAll": equivale a "get *:*"
			try {
				retval = System.currentTimeMillis() + "\n";
				Device[] devices = findDevices("*");
				long timestamp = 0;
				if (name.equals("timestamp")) {
					try {
						timestamp = Long.parseLong(value);
					} catch (NumberFormatException e) {
						// Manteniamo il valore di default: zero
					}
				}
				for (int i = 0; i < devices.length; i++) {
					retval += devices[i].getStatus("*", timestamp);
				}
			} catch (AISException e) {
				retval = e.getMessage();
			}
		} else if (command.equals("set")) {
			// Comando "set"
			try {
				String deviceAddress = getDeviceFromAddress(name);
				String portName = getPortFromAddress(name);
				Device devices[] = findDevices(deviceAddress);
				if (devices.length == 1) {
					devices[0].poke(portName, value);
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
		logger.trace("Ricevuto evento: " + event.getInfo());
		// Esempio: accendiamo un dimmer
		Device device = event.getDevice();
//		if (device.getAddress().equals("1")) {
//			logger.info("Comando da BMC virtuale");
//			receiveAuthenticatedRequest("set", "0.5:Out2", "100");
//		}
	}
}
