package it.ascia.avs;

import it.ascia.ais.AISException;
import it.ascia.ais.Device;
import it.ascia.ais.DevicePort;
import it.ascia.ais.port.DigitalInputPort;
import it.ascia.ais.port.DigitalOutputPort;

public class AVSZoneDigDevice extends Device {
	
	private int zone;

	public AVSZoneDigDevice(int zone, String address) throws AISException {
		super(address);
		this.zone = zone;
		addPort(new DigitalInputPort("Stato"));
		addPort(new DigitalInputPort("Tamper"));
		addPort(new DigitalOutputPort("Bypass"));
		addPort(new DigitalInputPort("Batteria"));
		addPort(new DigitalInputPort("Sopravvivenza"));
		addPort(new DigitalInputPort("Antimask"));
		addPort(new DigitalInputPort("AllarmiAvvenuti"));
	}

	@Override
	public boolean sendPortValue(String portId, Object newValue)
			throws AISException {
		if (portId.equals("Bypass") && Boolean.class.isInstance(newValue)) {
			return getConnector().sendMessage(AVSBypassZoneMessage.create(zone,(Boolean)newValue));
		} else {
			logger.warn("Cannot send value to port "+portId);
			return false;
		}
	}

	@Override
	public long updatePort(String portId) throws AISException {
		return 0;
	}

	void setStato(boolean stato) {
		getPort("Stato").setValue(stato);
	}

	void setTamper(boolean stato) {
		getPort("Tamper").setValue(stato);
	}

	void setBypass(boolean stato) {
		getPort("Bypass").setValue(stato);
	}

	void setBatteria(boolean stato) {
		getPort("Batteria").setValue(stato);
	}

	void setSopravvivenza(boolean stato) {
		getPort("Sopravvivenza").setValue(stato);
	}

	void setAntimask(boolean stato) {
		getPort("Antimask").setValue(stato);
	}

	void setAllarmiAvvenuti(boolean stato) {
		getPort("AllarmiAvvenuti").setValue(stato);
	}
}
