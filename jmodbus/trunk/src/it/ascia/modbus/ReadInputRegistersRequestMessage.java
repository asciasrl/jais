package it.ascia.modbus;

import net.wimpi.modbus.msg.ReadInputRegistersRequest;

public class ReadInputRegistersRequestMessage extends ModbusRequestMessage {
	
	public ReadInputRegistersRequestMessage(int ref,int count,int unitid) {
		msg = new ReadInputRegistersRequest(ref, count);
		msg.setUnitID(unitid);
		msg.setHeadless();
	}

}
