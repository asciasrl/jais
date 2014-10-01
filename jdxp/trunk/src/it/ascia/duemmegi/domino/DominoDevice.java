package it.ascia.duemmegi.domino;

import it.ascia.ais.AISException;
import it.ascia.ais.Device;
import it.ascia.duemmegi.fxpxt.FXPXTMessage;

public abstract class DominoDevice extends Device {

	public DominoDevice(String address)
			throws AISException {
		super(address);
	}
	
	public abstract void messageReceived(FXPXTMessage m);

	public abstract void messageSent(FXPXTMessage m);

}
