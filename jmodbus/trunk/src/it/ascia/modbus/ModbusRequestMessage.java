package it.ascia.modbus;

import net.wimpi.modbus.msg.ModbusRequest;

public class ModbusRequestMessage extends ModbusMessage {

	public ModbusRequest getRequest() {
		return (ModbusRequest) msg;
	}

}
