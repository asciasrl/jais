/**
 * 
 */
package it.ascia.ais.port;

/**
 * Questa porta ha sempre valore false. Viene usata solo per inviare comandi al device 
 * 
 * @author Sergio
 *
 */
public class TriggerPort extends BooleanPort {

	/**
	 * Crea una porta virtuale, che serve solo da trigger per comandare il device
	 * @param device
	 * @param portId
	 */
	public TriggerPort(String portId) {
		super(portId);
		setCachedValue(new Boolean(false));
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
	
}
