package it.ascia.dxp.device;

import it.ascia.ais.AISException;
import it.ascia.ais.Message;
import it.ascia.dxp.DXPMessage;
import it.ascia.dxp.DominoDevice;

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

	public void messageReceived(DXPMessage m) {
		// TODO Auto-generated method stub
		
	}

	public void messageSent(DXPMessage m) {
		// TODO Auto-generated method stub
		
	}

}
