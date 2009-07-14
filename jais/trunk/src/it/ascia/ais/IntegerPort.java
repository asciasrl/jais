package it.ascia.ais;

/**
 * Store java.lang.Integer value 
 * @author Sergio
 *
 */
public class IntegerPort extends DevicePort {

	public IntegerPort(Device device, String portId) {
		super(device, portId);
	}

	public IntegerPort(Device device, String portId, String portName) {
		super(device, portId, portName);
	}

}
