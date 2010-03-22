package it.ascia.bentel;

import it.ascia.ais.AISException;
import it.ascia.ais.Device;
import it.ascia.ais.DevicePort;
import it.ascia.ais.port.DigitalInputPort;
import it.ascia.ais.port.DigitalOutputPort;

public class ZoneDevice extends Device {

	public ZoneDevice(String address) throws AISException {
		super(address);
		DevicePort p;
		p = new LabelPort("Description");
		p.setCacheRetention(10 * 60 * 1000);
		addPort(p);
		p = new DigitalInputPort("Alarm");
		p.setCacheRetention(100);
		addPort(p);
		addPort(new DigitalInputPort("AlarmMemory"));
		p = new DigitalInputPort("Tamper");
		p.setCacheRetention(100);		
		addPort(p);
		addPort(new DigitalInputPort("TamperMemory"));
		addPort(new DigitalOutputPort("Bypassed"));
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
	public long updatePort(String portId) throws AISException {
		if (portId.equals("Description")) {
			((BentelKyoConnector) getConnector()).updateZonesDescriptions();
		} else if (portId.equals("Alarm") || portId.equals("Tamper")) {
			((BentelKyoConnector) getConnector()).updateRealTime();
		} else if (portId.equals("AlarmMemory") || portId.equals("TamperMemory") || portId.equals("Bypassed")) {
			((BentelKyoConnector) getConnector()).updateStatus();
		} else {
			logger.warn("Cannot update unknow port: "+portId);
		}
		return 0;
	}

}
