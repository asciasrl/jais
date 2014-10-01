package it.ascia.duemmegi.domino.device;

import it.ascia.ais.AISException;
import it.ascia.ais.Connector;

public class DF8IL extends DF4IV {

	protected int getNumInputs() {
		return 2;
	}

	protected int getNumVirtuals() {
		return 2;
	}

	public DF8IL(Connector connector, String address) throws AISException {
		super(connector,address);
	}

}
