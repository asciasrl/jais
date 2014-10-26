package it.ascia.duemmegi.domino.device;

import it.ascia.ais.AISException;
import it.ascia.ais.ConnectorInterface;
import it.ascia.ais.port.DigitalVirtualPort;

public class DF8IL extends DF4IV {

	protected int getNumInputs() {
		return 2;
	}

	protected int getNumVirtuals() {
		return 2;
	}

	public DF8IL(String address, ConnectorInterface connector) throws AISException {
		super(address,connector);
		for (int j = getNumInputs() ; j < (getNumInputs() + getNumVirtuals()); j++) {
			connector.addDevice("i" + (intAddress + j), this);
			for (int i = 1; i <= 4; i++) {
				addPort(new DigitalVirtualPort("o"+(intAddress+j)+"."+new Integer(i).toString()));
			}
		}
	}

}
