package it.ascia.duemmegi.domino.device;

import org.apache.commons.configuration.HierarchicalConfiguration;

import it.ascia.ais.AISException;
import it.ascia.ais.ConnectorInterface;

public class DF8IL extends DF4IV {

	protected int getNumInputs() {
		return 2;
	}

	protected int getNumVirtuals() {
		return 2;
	}

	public DF8IL(ConnectorInterface connector, HierarchicalConfiguration config) throws AISException {
		super(connector,config);
	}

}
