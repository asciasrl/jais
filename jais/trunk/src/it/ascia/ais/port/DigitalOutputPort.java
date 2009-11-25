/**
 * 
 */
package it.ascia.ais.port;

/**
 * @author Sergio
 *
 */
public class DigitalOutputPort extends BooleanPort {

	public DigitalOutputPort(String portId) {
		super(portId);
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
