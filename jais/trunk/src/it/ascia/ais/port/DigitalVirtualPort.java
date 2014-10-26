package it.ascia.ais.port;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * VirtualPort is a port that cannot be written, like the input port 
 * @author Sergio
 *
 */
public class DigitalVirtualPort extends DigitalInputPort implements PropertyChangeListener {

	private DigitalOutputPort outputPort;

	public DigitalVirtualPort(String portId) {
		super(portId);
	}

	/**
	 * Bind the value of this port to an output port
	 * @param portId this port
	 * @param outputPort the port that drive the value of this
	 */
	public DigitalVirtualPort(String portId, DigitalOutputPort outputPort) {
		super(portId);
		this.outputPort = outputPort;
		outputPort.addPropertyChangeListener(this);
	}

	@Override
	public void propertyChange(PropertyChangeEvent arg0) {
		setValue(outputPort.getCachedValue());
	}

}
