package it.ascia.dxp.device;

import it.ascia.ais.AISException;
import it.ascia.ais.Connector;
import it.ascia.ais.BlindPort;

public class DFTP extends DF4R {

	public DFTP(Connector connector, String address) throws AISException {
		super(connector, address);
		addPort(new BlindPort(this, "blind1", "o"+address+".2", "o"+address+".1"));
		addPort(new BlindPort(this, "blind2", "o"+address+".4", "o"+address+".3"));		
	}

	/*
	public String getInfo() {
		// TODO Auto-generated method stub
		return null;
	}
	*/
	/*
	public long updatePort(String portId) throws AISException {
		// TODO Auto-generated method stub
		return 0;
	}
	*/
	/*
	public boolean writePort(String portId, Object newValue)
			throws AISException {
		// TODO Auto-generated method stub
		return false;
	}
	*/
	/*
	public void messageReceived(Message m) {
		// TODO Auto-generated method stub
		
	}
	*/

	/*
	public void messageSent(Message m) {
		// TODO Auto-generated method stub
		
	}
	*/

}
