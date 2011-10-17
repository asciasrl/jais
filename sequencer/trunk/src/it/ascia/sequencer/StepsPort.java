package it.ascia.sequencer;

import java.util.List;

import it.ascia.ais.DevicePort;

public class StepsPort extends DevicePort {

	public StepsPort(String portId) {
		super(portId);
	}

	@Override
	public long update() {
		return 0;
	}
	
	@Override
	public boolean isDirty() {
		return getCachedValue() == null;
	}

	@Override
	public boolean isExpired() {
		return getCachedValue() == null;
	}
	
	
	@Override
	protected Object normalize(Object newValue) throws IllegalArgumentException {
		if (List.class.isInstance(newValue)) {
			return newValue;
		} else {
			throw(new IllegalArgumentException(newValue.toString()));
		}
	}

}
