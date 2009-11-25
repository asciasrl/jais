package it.ascia.ais.port;

import it.ascia.ais.DevicePort;

public class StatePort extends DevicePort {

	public StatePort(String portId, String[] tags) {
		super(portId);
		setTags(tags);
	}

}