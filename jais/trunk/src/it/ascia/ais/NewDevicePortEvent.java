package it.ascia.ais;

import java.util.EventObject;

public class NewDevicePortEvent extends EventObject {

	private static final long serialVersionUID = 1L;

	public NewDevicePortEvent(DevicePort source) {
		super(source);
	}

	public DevicePort getDevicePort() {
		return (DevicePort) getSource();
	}

}
