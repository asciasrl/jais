package it.ascia.ais.port;

import it.ascia.ais.AISException;

public class SeasonPort extends StatePort {

	public SeasonPort(String portId) {
		this(portId,new String[] {"winter","summer"});
	}

	public SeasonPort(String portId, String[] tags) {
		super(portId, tags);
	}
	
}
