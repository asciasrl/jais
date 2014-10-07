package it.ascia.modbus;

import it.ascia.ais.AISException;
import it.ascia.ais.RequestMessage;
import it.ascia.ais.ResponseMessage;
import net.wimpi.modbus.io.ModbusTransaction;
import net.wimpi.modbus.msg.ModbusRequest;
import net.wimpi.modbus.msg.ModbusResponse;

public class ModbusResponseMessage extends ModbusMessage implements ResponseMessage {

	private ModbusRequest req;

	public ModbusResponseMessage(ModbusTransaction trans) {
		msg = trans.getResponse();
		req = trans.getRequest();
	}
	
	public ModbusResponse getModbusResponse() {
		return (ModbusResponse) msg;
	}

	public ModbusRequest getModbusRequest() {
		return req;
	}

	@Override
	public void setRequest(RequestMessage requestMessage) {
		// TODO Auto-generated method stub
		throw(new AISException("Request cannot be set to " + requestMessage));		
	}

	@Override
	public RequestMessage getRequest() {
		return null;
	}

	@Override
	public boolean isResponseTo(RequestMessage requestMessage) {
		return false;
	}

}
