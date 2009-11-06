package it.ascia.ais.port;

import it.ascia.ais.Device;
import it.ascia.ais.DevicePort;

/**
 * Store java.util.Date value
 * @author Sergio
 *
 */
public class DatePort extends DevicePort {

	public DatePort(Device device, String portId) {
		super(device, portId);
	}

	public DatePort(Device device, String portId, String portName) {
		super(device, portId, portName);
	}

}
