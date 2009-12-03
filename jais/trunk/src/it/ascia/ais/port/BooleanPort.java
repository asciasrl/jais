/**
 * 
 */
package it.ascia.ais.port;

import it.ascia.ais.DevicePort;

/**
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
	
	/**
	 * Imposta il valore della porta convertendo il testo fornito in valore Boolean 
	 */	
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

	public boolean writeValue(Object value) throws IllegalArgumentException {
		if (value instanceof String) {
			return writeValue((String)value);
		} else {
			return super.writeValue(value);
		}
	}

}
