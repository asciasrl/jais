package it.ascia.ais.port;

public class AnalogInputPort extends IntegerPort {

	public AnalogInputPort(String portId) {
		super(portId);
	}
	
	@Override
	public boolean writeValue(Object newValue) {
		logger.error("Cannot write value to input port "+getAddress());
		return false;
	}

}
