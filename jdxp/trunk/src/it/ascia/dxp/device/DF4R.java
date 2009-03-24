package it.ascia.dxp.device;

import it.ascia.ais.AISException;
import it.ascia.ais.Connector;
import it.ascia.dxp.DominoDevice;

public class DF4R extends DominoDevice {

	public DF4R(Connector connector, String address) throws AISException {
		super(connector, address);
		for (int i = 1; i <= 4; i++) {
			addPort("o"+address+"."+new Integer(i).toString());			
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

}
