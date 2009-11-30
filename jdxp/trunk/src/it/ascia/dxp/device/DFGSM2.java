package it.ascia.dxp.device;

import it.ascia.ais.AISException;
import it.ascia.ais.Message;
import it.ascia.ais.port.DigitalInputPort;

import it.ascia.dxp.DXPMessage;
import it.ascia.dxp.DominoDevice;

public class DFGSM2 extends DominoDevice {

	public DFGSM2(String address) throws AISException {
		super(address);
		int intAddress = new Integer(address).intValue();		
		for (int j = 0; j <= 3; j++) {
			for (int i = 1; i <= 4; i++) {
				addPort(new DigitalInputPort("i"+(intAddress+j)+"."+new Integer(i).toString()));
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
