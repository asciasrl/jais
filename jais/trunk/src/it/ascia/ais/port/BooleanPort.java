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
	private Boolean normalize(String text) throws IllegalArgumentException {
		boolean v = false;
		if (text.equals("1") || text.toLowerCase().equals("on") || text.toLowerCase().equals("true")) {
			v = true;
		} else if (text.equals("0") || text.toLowerCase().equals("off") || text.toLowerCase().equals("false")) {
			v = false;
		} else {
			throw new IllegalArgumentException(getFullAddress() + " valore non valido: '"+text+"'");
		}
		return new Boolean(v);
	}

	public Object normalize(Object value) {
		if (value instanceof String) {
			return normalize((String)value);
		} else if (value instanceof Boolean) {
			return value;
		} else {
			return super.normalize(value);
		}
	}

}
