package it.ascia.modbus;

import net.wimpi.modbus.msg.ModbusResponse;

import it.ascia.ais.AISException;
import it.ascia.ais.Device;

public abstract class ModbusDevice extends Device {
	
	private int unitId;
	
	public ModbusDevice(int unitId) throws AISException {
		super((new Integer(unitId)).toString());
		this.unitId = unitId;
	}

	@Override
	public long updatePort(String portId) throws AISException {
		ModbusPort p = (ModbusPort) getPort(portId);
		if (ModbusInt32Port.class.isInstance(p)) {
			ReadInputRegistersRequestMessage m = new ReadInputRegistersRequestMessage(p.getPhysicalAddress(),2,unitId);
			getConnector().sendMessage(m);
		}
		return 0;
	}
	
	@Override
	public boolean sendPortValue(String portId, Object newValue)
			throws AISException {
		// TODO Auto-generated method stub
		return false;
	}



}
