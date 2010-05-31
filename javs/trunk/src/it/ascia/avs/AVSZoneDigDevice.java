package it.ascia.avs;

import it.ascia.ais.AISException;
import it.ascia.ais.Device;
import it.ascia.ais.DevicePort;
import it.ascia.ais.port.DigitalInputPort;
import it.ascia.ais.port.DigitalOutputPort;

public class AVSZoneDigDevice extends Device {

	public AVSZoneDigDevice(String address) throws AISException {
		super(address);
		addPort(new DigitalInputPort("Stato"));
		addPort(new DigitalInputPort("Tamper"));
		addPort(new DigitalOutputPort("Bypass"));
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

	protected void setThreeState(DevicePort p, int stato) {
		if (stato == -1) {
			Boolean c = (Boolean) p.getCachedValue();
			if (c == null) {
				throw(new AISException("Cannot toggle null value"));
			}
			p.setValue(!c);			
		} else {
			p.setValue(stato == 1);
		}		
	}

	void setStato(int stato) {
		setThreeState(getPort("Stato"),stato);
	}

	void setTamper(int stato) {
		setThreeState(getPort("Tamper"),stato);
	}

	void setBypass(int stato) {
		setThreeState(getPort("Bypass"),stato);
	}


}
