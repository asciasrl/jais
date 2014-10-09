package it.ascia.duemmegi.domino;

import org.apache.commons.configuration.HierarchicalConfiguration;

import it.ascia.ais.AISException;
import it.ascia.ais.Device;

public abstract class DominoDevice extends Device {

	public DominoDevice(HierarchicalConfiguration config)
			throws AISException {
		super(config);
	}

	//public abstract void messageReceived(DXPMessage m);

	//public abstract void messageSent(DXPMessage m);

}
