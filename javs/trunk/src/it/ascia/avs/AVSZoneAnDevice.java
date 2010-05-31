package it.ascia.avs;

import it.ascia.ais.AISException;
import it.ascia.ais.Device;
import it.ascia.ais.port.AnalogInputPort;

public class AVSZoneAnDevice extends Device {

	public AVSZoneAnDevice(String address) throws AISException {
		super(address);
		addPort(new AnalogInputPort("Value"));
	}

	@Override
	public boolean sendPortValue(String portId, Object newValue)
			throws AISException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public long updatePort(String portId) throws AISException {
		// TODO Auto-generated method stub
		return 0;
	}

}
