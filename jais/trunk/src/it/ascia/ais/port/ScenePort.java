/**
 * 
 */
package it.ascia.ais.port;

import it.ascia.ais.Device;

/**
 * Questa porta ha sempre valore false. Viene usata solo per attivare scenari 
 * 
 * @author Sergio
 *
 */
public class ScenePort extends TriggerPort {

	public ScenePort(Device device, String portId) {
		super(device, portId);
	}
	
}
