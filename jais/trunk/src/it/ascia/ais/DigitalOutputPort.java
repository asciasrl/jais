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
		this(device, portId,null);
	}

	public DigitalOutputPort(Device device, String portId, String portName) {
		super(device, portId, portName);
		setTags(new String[] {"1","0","on","off","true","false"});
	}

	public boolean writeValue(Object newValue) throws IllegalArgumentException {
		if (Boolean.class.isInstance(newValue)) {
			return super.writeValue(newValue);
		} else if (String.class.isInstance(newValue)) {
			return writeValue((String) newValue);
		}
		throw new IllegalArgumentException(getFullAddress() + " Tipo di valore non valido: "+newValue.getClass().getName());
	}

	public boolean writeValue(String text) throws IllegalArgumentException {
		boolean v = false;
		if (text.equals("1") || text.toLowerCase().equals("on") || text.toLowerCase().equals("true")) {
			v = true;
		} else if (text.equals("0") || text.toLowerCase().equals("off") || text.toLowerCase().equals("false")) {
			v = false;
		} else {
			throw new IllegalArgumentException(getFullAddress() + " valore non valido: '"+text+"'");
		}
		return writeValue(new Boolean(v));
	}

}
