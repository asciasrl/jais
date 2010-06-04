/**
 * 
 */
package it.ascia.ais.port;

import it.ascia.ais.DevicePort;

/**
 * Store java.lang.Boolean
 * @author Sergio
 *
 */
public class BooleanPort extends DevicePort {

	/**
	 * @param device
	 * @param portId
	 */
	public BooleanPort(String portId) {
		super(portId);
	}
	
	public Object normalize(Object newValue) {
		if (newValue == null) {
			return null;		
		} else if (newValue instanceof Boolean) {
			return newValue;
		} else if (newValue instanceof Integer) {
			return ((Integer) newValue).intValue() > 0;
		} else if (newValue instanceof String) {
			boolean v = false;
			String textValue = (String) newValue;
			if (textValue.equals("1") || textValue.toLowerCase().equals("on") || textValue.toLowerCase().equals("true")) {
				v = true;
			} else if (textValue.equals("0") || textValue.toLowerCase().equals("off") || textValue.toLowerCase().equals("false")) {
				v = false;
			} else {
				throw new IllegalArgumentException(getAddress() + " invalid value: '"+textValue+"'");
			}
			return new Boolean(v);
		} else {
			throw(new IllegalArgumentException("Value of "+getAddress()+" cannot be a "+newValue.getClass().getCanonicalName()));
		}
	}

}
