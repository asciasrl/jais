/**
 * 
 */
package it.ascia.ais.port;

/**
 * @author Sergio
 *
 */
public class DigitalInputPort extends BooleanPort {

	public DigitalInputPort(String portId) {
		super(portId);
	}

	public boolean writeValue(Object newValue) {
		logger.error("Cannot write value to digital input");
		return false;
	}

}
