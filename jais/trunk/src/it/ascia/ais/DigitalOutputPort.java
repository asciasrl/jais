/**
 * 
 */
package it.ascia.ais;


/**
 * @author Sergio
 *
 */
public class DigitalOutputPort extends DevicePort {

	public DigitalOutputPort(Device device, String portId) {
		super(device, portId);
	}

	public DigitalOutputPort(Device device, String portId, String portName) {
		super(device, portId, portName);
	}

	public String[] getTags() {
		return new String[] {"1","0","on","off","true","false"};
	}

	public boolean writeValue(String text) throws IllegalArgumentException {
		boolean v = false;
		if (text.equals("1") || text.toLowerCase().equals("on") || text.toLowerCase().equals("true")) {
			v = true;
		} else if (text.equals("0") || text.toLowerCase().equals("off") || text.toLowerCase().equals("false")) {
			v = false;
		} else {
			throw new IllegalArgumentException("Valore non valido: '"+text+"'");
		}
		return writeValue(new Boolean(v));
	}

}
