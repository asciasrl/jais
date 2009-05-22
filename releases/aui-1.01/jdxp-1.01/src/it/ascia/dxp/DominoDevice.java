package it.ascia.dxp;

import it.ascia.ais.AISException;
import it.ascia.ais.Connector;
import it.ascia.ais.Device;
import it.ascia.ais.Message;

public abstract class DominoDevice extends Device {

	public DominoDevice(Connector connector, String address)
			throws AISException {
		super(connector, address);
	}

	public abstract void messageReceived(Message m);

	public abstract void messageSent(Message m);
	
}
