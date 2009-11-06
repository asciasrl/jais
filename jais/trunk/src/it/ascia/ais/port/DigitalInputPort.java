/**
 * 
 */
package it.ascia.ais.port;

import it.ascia.ais.Device;


/**
 * @author Sergio
 *
 */
public class DigitalInputPort extends BooleanPort {

	public DigitalInputPort(Device device, String portId) {
		super(device, portId);
	}

	public DigitalInputPort(Device device, String portId, String portName) {
		super(device, portId, portName);
	}

	public boolean writeValue(Object newValue) {
		logger.error("Cannot write value to digital input");
		return false;
	}

}
