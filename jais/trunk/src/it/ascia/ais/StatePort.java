package it.ascia.ais;

public class StatePort extends DevicePort {

	public StatePort(Device device, String portId, String portName, String[] tags) {
		super(device, portId, portName);
		setTags(tags);
	}

	public StatePort(Device device, String portId, String[] tags) {
		this(device, portId, null, tags);
	}

}