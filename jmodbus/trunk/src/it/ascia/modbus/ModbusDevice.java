package it.ascia.modbus;

import net.wimpi.modbus.msg.ModbusResponse;
import net.wimpi.modbus.msg.ReadInputRegistersResponse;
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
		ReadInputRegistersRequestMessage m = new ReadInputRegistersRequestMessage(p.getPhysicalAddress(),p.getWords(),unitId);
		if (getConnector().sendMessage(m)) {
	    	ModbusResponse res = m.getResponse().getModbusResponse();
	    	p.setValue((ReadInputRegistersResponse)res);
	    	return true;
		} else {
			return false;
		}
	}
	
	@Override
	public boolean sendPortValue(String portId, Object newValue)
			throws AISException {
		logger.error("sendPortValue metodo non implementato");
		return false;
	}



}
