package it.ascia.modbus;

import it.ascia.ais.AISException;
import it.ascia.ais.Device;

public class ModbusDevice extends Device {
	
	private int unitId;
	
	public ModbusDevice(int unitId) throws AISException {
		super((new Integer(unitId)).toString());
		this.unitId = unitId;
	}

	@Override
	public boolean updatePort(String portId) throws AISException {
		ModbusPort p = (ModbusPort) getPort(portId);
		if (ModbusInt32Port.class.isInstance(p)) {
			ReadInputRegistersRequestMessage m = new ReadInputRegistersRequestMessage(p.getPhysicalAddress(),2,unitId);
			return getConnector().sendMessage(m);
		} else {
			return false;
		}
	}
	
	@Override
	public boolean sendPortValue(String portId, Object newValue)
			throws AISException {
		// TODO Auto-generated method stub
		return false;
	}



}
