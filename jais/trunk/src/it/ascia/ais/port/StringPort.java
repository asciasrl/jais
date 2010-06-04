package it.ascia.ais.port;

import it.ascia.ais.DevicePort;

public class StringPort extends DevicePort {

	public StringPort(String portId) {
		super(portId);
	}

	@Override
	protected Object normalize(Object newValue) throws IllegalArgumentException {
		if (newValue == null) {
			return null;
		} else if (String.class.isInstance(newValue)) {
			return newValue;
		} else {
			throw(new IllegalArgumentException("Value of "+getAddress()+" cannot be a "+newValue.getClass().getCanonicalName()));
		}
	}

}
