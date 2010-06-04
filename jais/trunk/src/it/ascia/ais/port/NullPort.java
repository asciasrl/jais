/**
 * 
 */
package it.ascia.ais.port;

import it.ascia.ais.DevicePort;

/**
 * Questa porta ha sempre valore false. Viene usata solo per inviare comandi al device 
 * 
 * @author Sergio
 *
 */
public class NullPort extends DevicePort {

	/**
	 * Crea una porta virtuale, che serve solo da trigger per comandare il device
	 * @param device
	 * @param portId
	 */
	public NullPort(String portId) {
		super(portId);
	}
	
	/**
	 * @return sempre false
	 */
	public boolean isDirty() {
		return false;
	}

	/**
	 * @return sempre false
	 */	
	public boolean isExpired() {
		return false;
	}

	@Override
	protected Object normalize(Object newValue) throws IllegalArgumentException {
		return null;
	}
	
}
