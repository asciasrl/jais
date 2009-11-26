package it.ascia.eds.device;

import it.ascia.ais.AISException;
import it.ascia.ais.Device;
import it.ascia.ais.DevicePort;
import it.ascia.ais.port.TriggerPort;
import it.ascia.eds.msg.ComandoBroadcastMessage;

public class EDSGroup extends Device {
	
	static final String ACTIVATE = "Attivazione";
	static final String DISACTIVATE = "Disattivazione";
	
	private int group = -1;

	public EDSGroup(String address) throws AISException {
		super(address);
		try {
			group = Integer.parseInt(getAddress().getDeviceAddress().substring(5));
		} catch (NumberFormatException e) {
			logger.error("Invalid group number ("+address+") :",e);
		}
		if (group < 0 || group > 31) {
			throw(new IllegalArgumentException("Invalid address: "+address));
		}
		addPort(new TriggerPort(ACTIVATE));
		addPort(new TriggerPort(DISACTIVATE));
	}

	public DevicePort getPort(String portId) throws AISException {
		if (portId.startsWith("A")) {
			return super.getPort(ACTIVATE);
		} else if (portId.startsWith("D")) {
			return super.getPort(DISACTIVATE);
		}
		return null;
	}
	
	@Override
	public boolean sendPortValue(String portId, Object newValue)
			throws AISException {
		boolean v;
		if (portId.equals(ACTIVATE)) {
			v = true;
		} else if (portId.equals(DISACTIVATE)) {
			v = false;
		} else {
			throw new IllegalArgumentException("Invalid port: "+portId);					
		}
		return getConnector().sendMessage(new ComandoBroadcastMessage(group,v));
	}

	@Override
	public long updatePort(String portId) throws AISException {
		// TODO Auto-generated method stub
		return 0;
	}

}
