package it.ascia.dxp.device;

import it.ascia.ais.AISException;
import it.ascia.ais.Connector;
import it.ascia.ais.Message;
import it.ascia.dxp.DominoDevice;

public class DFIR extends DominoDevice {

	public DFIR(Connector connector, String address) throws AISException {
		super(connector, address);
		// TODO Auto-generated constructor stub
	}

	public String getInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	public long updatePort(String portId) throws AISException {
		// TODO Auto-generated method stub
		return 0;
	}

	public boolean sendPortValue(String portId, Object newValue)
			throws AISException {
		// TODO Auto-generated method stub
		return false;
	}

	public void messageReceived(Message m) {
		// TODO Auto-generated method stub
		
	}

	public void messageSent(Message m) {
		// TODO Auto-generated method stub
		
	}

}
