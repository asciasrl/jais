package it.ascia.duemmegi.domino.device;

import it.ascia.ais.AISException;
import it.ascia.duemmegi.domino.DominoDevice;
import it.ascia.duemmegi.dxp.DXPMessage;

public class DFIR extends DominoDevice {

	public DFIR(String address) throws AISException {
		super(address);
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

	public void messageReceived(DXPMessage m) {
		// TODO Auto-generated method stub
		
	}

	public void messageSent(DXPMessage m) {
		// TODO Auto-generated method stub
		
	}

}
