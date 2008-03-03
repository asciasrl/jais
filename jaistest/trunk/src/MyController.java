/**
 * Copyright (C) 2008 ASCIA S.r.l.
 */
import org.apache.log4j.Logger;

import it.ascia.ais.Connector;
import it.ascia.ais.AISException;
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
public class MyController extends Controller implements DeviceListener {
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
	 * @param pin il pin da richiedere; se null, qualunque pin verra' accettato.
	 */
	public MyController(String pin) {
		this.logger = Logger.getLogger(getClass());
		this.pin = pin;
	}
	
	public void addConnector(Connector connector) {
		registerConnector(connector);
	}

	/**
	 * Associa se stesso a tutti i device di tutti i connector.
	 */
	public void setDevicesListener() throws AISException {
		Device devices[] = findDevices("*");
		for (int i = 0; i < devices.length; i++) {
			devices[i].setDeviceListener(this);
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
				String deviceAddress = getDeviceFromAddress(name);
				String portName = getPortFromAddress(name);
				Device devices[] = findDevices(deviceAddress);
				if (devices.length > 0) {
					retval = "";
					for (int i = 0; i < devices.length; i++) {
						retval += devices[i].getStatus(portName);
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
				retval = "";
				Device[] devices = findDevices("*");
				for (int i = 0; i < devices.length; i++) {
					retval += devices[i].getStatus("*");
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
					devices[0].setPort(portName, value);
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
		if (device.getAddress().equals("1")) {
			logger.info("Comando da BMC virtuale");
			receiveAuthenticatedRequest("set", "0.5:Out2", "100");
		}
	}
}
