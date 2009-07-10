package it.ascia.ais;

public class StatePort extends DevicePort {

	public StatePort(Device device, String portId) {
		super(device, portId, null);
	}

	public StatePort(Device device, String portId, String portName) {
		super(device, portId, portName);
		// TODO Auto-generated constructor stub
	}

	public StatePort(Device device, String portId, String portName, String[] tags) {
		super(device, portId, portName);
		setTags(tags);
	}

	public StatePort(Device device, String portId, String[] tags) {
		this(device, portId, null, tags);
	}

}