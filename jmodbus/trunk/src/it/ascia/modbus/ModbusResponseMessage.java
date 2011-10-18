package it.ascia.modbus;

import net.wimpi.modbus.io.ModbusTransaction;
import net.wimpi.modbus.msg.ModbusRequest;
import net.wimpi.modbus.msg.ModbusResponse;

public class ModbusResponseMessage extends ModbusMessage {

	private ModbusRequest req;

	public ModbusResponseMessage(ModbusTransaction trans) {
		msg = trans.getResponse();
		req = trans.getRequest();
	}
	
	public ModbusResponse getResponse() {
		return (ModbusResponse) msg;
	}

	public ModbusRequest getRequest() {
		return req;
	}

}
