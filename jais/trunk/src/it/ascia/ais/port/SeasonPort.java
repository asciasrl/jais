package it.ascia.ais.port;

import it.ascia.ais.Device;

public class SeasonPort extends StatePort {

	public SeasonPort(Device device, String portId) {
		this(device, portId,null);
	}

	public SeasonPort(Device device, String portId, String portName) {
		this(device,portId,portName,new String[] {"winter","summer"});
	}

	public SeasonPort(Device device, String portId, String portName,
			String[] tags) {
		super(device, portId, portName, tags);
		// TODO Auto-generated constructor stub
	}

}
