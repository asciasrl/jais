package it.ascia.dxp.device;

import it.ascia.ais.AISException;
import it.ascia.ais.Connector;
import it.ascia.ais.Message;
import it.ascia.dxp.DominoDevice;

public class DFGSM2 extends DominoDevice {

	public DFGSM2(Connector connector, String address) throws AISException {
		super(connector, address);
		int intAddress = new Integer(address).intValue();		
		for (int j = 0; j <= 3; j++) {
			for (int i = 1; i <= 4; i++) {
				addPort("i"+(intAddress+j)+"."+new Integer(i).toString());
			}
		}		
	}

	public String getInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	public long updatePort(String portId) throws AISException {
		// TODO Auto-generated method stub
		logger.trace("updatePort non implementato");
		return 0;
	}

	public boolean writePort(String portId, Object newValue)
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
