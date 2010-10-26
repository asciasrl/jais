package it.ascia.avs;

import it.ascia.ais.AISException;
import it.ascia.ais.Device;

public class AVSInseritoreDevice extends Device {

	public AVSInseritoreDevice(String address) throws AISException {
		super(address);
		// TODO Auto-generated constructor stub
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
