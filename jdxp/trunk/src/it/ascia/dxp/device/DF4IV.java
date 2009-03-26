package it.ascia.dxp.device;

import it.ascia.ais.AISException;
import it.ascia.ais.Connector;
import it.ascia.ais.Message;
import it.ascia.dxp.DominoDevice;

public class DF4IV extends DominoDevice {

	public DF4IV(Connector connector, String address) throws AISException {
		super(connector, address);
		int intAddress = new Integer(address).intValue();
		for (int i = 1; i <= 4; i++) {
			addPort("i"+intAddress+"."+new Integer(i).toString());			
		}		
		for (int j = 1; j <= 3; j++) {
			for (int i = 1; i <= 4; i++) {
				addPort("v"+(intAddress+j)+"."+new Integer(i).toString());
			}
		}		
	}

	public String getInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	public long updatePort(String portId) throws AISException {
		// TODO Auto-generated method stub
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
