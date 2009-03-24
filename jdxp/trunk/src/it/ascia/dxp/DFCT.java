package it.ascia.dxp;

import it.ascia.ais.AISException;
import it.ascia.ais.Connector;

public class DFCT extends DominoDevice {

	public DFCT(Connector connector, String address) throws AISException {
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

	public boolean writePort(String portId, Object newValue)
			throws AISException {
		// TODO Auto-generated method stub
		return false;
	}

}
