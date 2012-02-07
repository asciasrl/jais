package it.ascia.bentel;

import it.ascia.ais.AISException;
import it.ascia.ais.Device;
import it.ascia.ais.port.DigitalOutputPort;

public class OutputsDevice extends Device {

	public OutputsDevice(String address, int outputs)
			throws AISException {
		super(address);
		for (int i = 1; i <= outputs; i++) {
			addPort(new DigitalOutputPort("Out"+i));
		}
	}

	@Override
	public String getInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean sendPortValue(String portId, Object newValue)
			throws AISException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean updatePort(String portId) throws AISException {
		if (portId.startsWith("Out")) {
			return ((BentelKyoConnector) getConnector()).updateStatus();
		} else {
			logger.warn("Cannot update unknow port: "+portId);
		}
		return false;
	}

}
