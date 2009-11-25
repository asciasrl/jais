package it.ascia.ais.port;

/**
 * VirtualPort is a port that cannot be written, like the input port 
 * @author Sergio
 *
 */
public class DigitalVirtualPort extends DigitalInputPort {

	public DigitalVirtualPort(String portId) {
		super(portId);
	}

}
