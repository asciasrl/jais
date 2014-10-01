package it.ascia.duemmegi.domino.device;

import it.ascia.ais.AISException;
import it.ascia.ais.Message;
import it.ascia.duemmegi.domino.DominoDevice;
import it.ascia.duemmegi.fxpxt.FXPXTMessage;

public class DFIR extends DominoDevice {

	public DFIR(String address) throws AISException {
		super(address);
		// TODO Auto-generated constructor stub
	}

	public String getInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean updatePort(String portId) throws AISException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean sendPortValue(String portId, Object newValue)
			throws AISException {
		// TODO Auto-generated method stub
		return false;
	}

	public void messageReceived(FXPXTMessage m) {
		// TODO Auto-generated method stub
		
	}

	public void messageSent(FXPXTMessage m) {
		// TODO Auto-generated method stub
		
	}

}
