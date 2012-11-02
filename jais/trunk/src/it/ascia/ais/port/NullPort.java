/**
 * 
 */
package it.ascia.ais.port;

import it.ascia.ais.DevicePortChangeEvent;

/**
 * Questa porta ha sempre valore false. Viene usata per inviare comandi al device o per registrare eventi
 * 
 * @author Sergio
 *
 */
public class NullPort extends BooleanPort {

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
	public Object getCachedValue() {
		return null;
	}

	/**
	 * Genera un evento di varaizione fittizia 
	 */
	public void setValue(Object newValue, long duration) {
		DevicePortChangeEvent evt = new DevicePortChangeEvent(this, null,true);
		fireDevicePortChangeEvent(evt);		
	}


}
