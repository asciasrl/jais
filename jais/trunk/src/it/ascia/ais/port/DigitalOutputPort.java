/**
 * 
 */
package it.ascia.ais.port;

import it.ascia.ais.Device;


/**
 * @author Sergio
 *
 */
public class DigitalOutputPort extends BooleanPort {

	public DigitalOutputPort(Device device, String portId) {
		this(device, portId,null);
	}

	public DigitalOutputPort(Device device, String portId, String portName) {
		super(device, portId, portName);
	}

	public boolean writeValue(Object newValue) throws IllegalArgumentException {
		if (Boolean.class.isInstance(newValue)) {
			return super.writeValue(newValue);
		} else if (String.class.isInstance(newValue)) {
			return writeValue((String) newValue);
		}
		throw new IllegalArgumentException(getFullAddress() + " Tipo di valore non valido: "+newValue.getClass().getName());
	}


}
