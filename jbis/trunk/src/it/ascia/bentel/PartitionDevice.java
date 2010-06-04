package it.ascia.bentel;

import it.ascia.ais.AISException;
import it.ascia.ais.Device;
import it.ascia.ais.port.DigitalInputPort;
import it.ascia.ais.port.SlaveBitStatePort;
import it.ascia.ais.port.StringPort;

public class PartitionDevice extends Device {

	public PartitionDevice(String address)
			throws AISException {
		super(address);
		addPort(new StringPort("Description"));
		addPort(new DigitalInputPort("Alarm"));
		DigitalInputPort pAway = new DigitalInputPort("Away");
		addPort(pAway);
		DigitalInputPort pStay = new DigitalInputPort("Stay");
		addPort(pStay);
		DigitalInputPort pStay0 = new DigitalInputPort("Stay0");
		addPort(pStay0);
		DigitalInputPort pDisarmed = new DigitalInputPort("Disarmed");
		addPort(pDisarmed);
		SlaveBitStatePort p = new SlaveBitStatePort("Status");
		p.addStatus("Away",pAway);
		p.addStatus("Stay",pStay);
		p.addStatus("Stay0",pStay0);
		p.addStatus("Disarmed",pDisarmed);
		addPort(p);
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
			((BentelKyoConnector) getConnector()).updatePartitionsDescriptions();
		} else if (portId.equals("Alarm") || portId.equals("Tamper")) {
			((BentelKyoConnector) getConnector()).updateRealTime();
		} else if (portId.equals("Away") || portId.equals("Stay") || portId.equals("Stay0") || portId.equals("Disarmed")) {
			((BentelKyoConnector) getConnector()).updateStatus();
		} else {
			logger.warn("Cannot update unknow port: "+portId);
		}
		return 0;
	}

}
