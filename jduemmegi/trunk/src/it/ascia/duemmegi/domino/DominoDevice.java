package it.ascia.duemmegi.domino;

import it.ascia.ais.AISException;
import it.ascia.ais.Device;

public abstract class DominoDevice extends Device {

	public DominoDevice(String address) throws AISException {
		super(address);
	}


	//public abstract void messageReceived(DXPMessage m);

	//public abstract void messageSent(DXPMessage m);

}
