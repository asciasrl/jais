package it.ascia.duemmegi.domino.device;

import it.ascia.ais.AISException;
import it.ascia.ais.port.DigitalInputPort;
import it.ascia.duemmegi.DominoDevice;
import it.ascia.duemmegi.dxp.DXPMessage;

public class DFGSM2 extends DominoDevice {

	public DFGSM2(String address) throws AISException {
		super(address);
		int intAddress = new Integer(getDeviceAddress()).intValue();		
		for (int j = 0; j <= 3; j++) {
			for (int i = 1; i <= 4; i++) {
				addPort(new DigitalInputPort("i"+(intAddress+j)+"."+new Integer(i).toString()));
			}
		}		
	}

	public boolean updatePort(String portId) throws AISException {
		// TODO Auto-generated method stub
		logger.trace("updatePort non implementato");
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
