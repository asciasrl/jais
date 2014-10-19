package it.ascia.duemmegi.domino.device;

import it.ascia.ais.AISException;
import it.ascia.ais.ConnectorInterface;
import it.ascia.ais.port.DigitalVirtualPort;
import it.ascia.duemmegi.domino.DominoDevice;

public class DFTPI extends DFTP {

	public DFTPI(String address, ConnectorInterface connector) throws AISException {		
		super("o" + (DominoDevice.getIntAddress(address)));
		String iAddr = "i" + DominoDevice.getIntAddress(address);
		connector.addDevice(iAddr, this);
		for (int i = 1; i <= 4; i++) {
			addPort(new DigitalVirtualPort(iAddr+"."+new Integer(i).toString()));
		}
	}

}
