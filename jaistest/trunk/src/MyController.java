

import org.apache.log4j.Logger;

import it.ascia.ais.AlarmReceiver;
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
public class MyController implements Controller, DeviceListener, AlarmReceiver {
	/**
	 * Il nostro interprete di indirizzi.
	 */
	private BusAddressParser addressParser;
	/**
	 * Il nostro connector.
	 */
	private Connector connector;
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
	public MyController(Connector connector, String pin) {
		this.logger = Logger.getLogger(getClass());
		addressParser = new BusAddressParser();
		addressParser.registerBus(connector);
		this.connector = connector;
		this.pin = pin;
	}

	/**
	 * Associa se stesso a tutti i device del Connector.
	 */
	public void setDevicesListener() {
		Device devices[] = connector.getDevices();
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
			Device[] devices = connector.getDevices();
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

	/**
	 * Reagisce a un allarme.
	 * 
	 * <p>Questo metodo viene invocato dall'oggetto centralina.</p>
	 */
	public void alarmReceived(String alarm) {
		// Esempio: accendiamo un dimmer
		logger.info("Ricevuto allarme");
		receiveAuthenticatedRequest("set", "0.5:Out1", "100");
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
