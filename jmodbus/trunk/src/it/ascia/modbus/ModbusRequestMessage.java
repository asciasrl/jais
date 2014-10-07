package it.ascia.modbus;

import it.ascia.ais.AISException;
import it.ascia.ais.RequestMessage;
import it.ascia.ais.ResponseMessage;
import net.wimpi.modbus.msg.ModbusRequest;

public class ModbusRequestMessage extends ModbusMessage implements RequestMessage {

	private ModbusResponseMessage response;

	public ModbusRequest getRequest() {
		return (ModbusRequest) msg;
	}

	/**
	 * @param res Response from device
	 */
	private void setResponse(ModbusResponseMessage res) {
		response = res;		
	}
	
	public ModbusResponseMessage getResponse() {
		return response;
	}

	@Override
	public void setResponse(ResponseMessage res) {
		if (ModbusResponseMessage.class.isInstance(res)) {
			setResponse((ModbusResponseMessage)res);
		} else {
			throw(new AISException("A rensponse to a ModbusRequestMessage can only be a ModbusResponseMessage, not a " + res.getClass()));
		}		
	}

	@Override
	public boolean isAnsweredBy(ResponseMessage m) {
		// FIXME Auto-generated method stub
		return false;
	}

	@Override
	public void setAnswered(boolean b) {
		// FIXME Auto-generated method stub
		
	}

	@Override
	public boolean isAnswered() {
		// FIXME Auto-generated method stub
		return false;
	}

}
