package it.ascia.duemmegi.domino.device;

import it.ascia.ais.AISException;
import it.ascia.ais.port.BlindPort;

public class DFTP extends DF4R {

	public DFTP(String address) throws AISException {
		super(address);
		addPort(new BlindPort("blind1", getPort("o"+intAddress+".2"), getPort("o"+intAddress+".1")));
		addPort(new BlindPort("blind2", getPort("o"+intAddress+".4"), getPort("o"+intAddress+".3")));		
	}

}
