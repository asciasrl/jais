package it.ascia.dxp.device;

import it.ascia.ais.AISException;
import it.ascia.ais.port.BlindPort;

public class DFTP extends DF4R {

	public DFTP(String address) throws AISException {
		super(address);
		addPort(new BlindPort("blind1", getPort("o"+address+".2"), getPort("o"+address+".1")));
		addPort(new BlindPort("blind2", getPort("o"+address+".4"), getPort("o"+address+".3")));		
	}

}
