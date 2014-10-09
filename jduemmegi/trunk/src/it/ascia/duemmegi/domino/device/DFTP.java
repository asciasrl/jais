package it.ascia.duemmegi.domino.device;

import org.apache.commons.configuration.HierarchicalConfiguration;

import it.ascia.ais.AISException;
import it.ascia.ais.port.BlindPort;

public class DFTP extends DF4R {

	public DFTP(HierarchicalConfiguration config) throws AISException {
		super(config);
		addPort(new BlindPort("blind1", getPort("o"+getDeviceAddress()+".2"), getPort("o"+config+".1")));
		addPort(new BlindPort("blind2", getPort("o"+getDeviceAddress()+".4"), getPort("o"+config+".3")));		
	}

}
