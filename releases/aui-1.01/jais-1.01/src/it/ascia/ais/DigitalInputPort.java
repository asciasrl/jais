/**
 * 
 */
package it.ascia.ais;


/**
 * @author Sergio
 *
 */
public class DigitalInputPort extends DevicePort {

	public DigitalInputPort(Device device, String portId) {
		super(device, portId);
	}

	public DigitalInputPort(Device device, String portId, String portName) {
		super(device, portId, portName);
	}

	public boolean writeValue(Object newValue) {
		return false;
	}

}
