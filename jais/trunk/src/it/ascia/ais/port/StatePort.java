package it.ascia.ais.port;

import it.ascia.ais.Device;
import it.ascia.ais.DevicePort;

public class StatePort extends DevicePort {

	public StatePort(Device device, String portId, String portName, String[] tags) {
		super(device, portId, portName);
		setTags(tags);
	}

	public StatePort(Device device, String portId, String[] tags) {
		this(device, portId, null, tags);
	}

}