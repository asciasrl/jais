/**
 * 
 */
package it.ascia.ais;

/**
 * @author Sergio
 *
 */
public class BooleanPort extends DevicePort {

	/**
	 * @param device
	 * @param portId
	 */
	public BooleanPort(Device device, String portId) {
		super(device, portId);
	}

	/**
	 * @param device
	 * @param portId
	 * @param portName
	 */
	public BooleanPort(Device device, String portId, String portName) {
		super(device, portId, portName);
		setTags(new String[] {"1","0","on","off","true","false"});
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

}
