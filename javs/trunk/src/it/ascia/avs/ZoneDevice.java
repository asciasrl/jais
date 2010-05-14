package it.ascia.avs;

import it.ascia.ais.AISException;
import it.ascia.ais.Device;
import it.ascia.ais.port.DigitalInputPort;

public class ZoneDevice extends Device {

	public ZoneDevice(String address) throws AISException {
		super(address);
		addPort(new DigitalInputPort("Stato"));
		addPort(new DigitalInputPort("Tamper"));
		addPort(new DigitalInputPort("Bypass"));
		/*
		addPort(new DigitalInputPort("Batteria"));
		addPort(new DigitalInputPort("Sopravv"));
		addPort(new DigitalInputPort("Antimask"));
		addPort(new DigitalInputPort("Allarme"));
		*/
	}

	@Override
	public boolean sendPortValue(String portId, Object newValue)
			throws AISException {
		return false;
	}

	@Override
	public long updatePort(String portId) throws AISException {
		return 0;
	}

}
