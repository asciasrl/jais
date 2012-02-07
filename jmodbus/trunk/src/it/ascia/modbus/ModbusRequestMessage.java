package it.ascia.modbus;

import it.ascia.ais.AISException;
import it.ascia.ais.Message;
import it.ascia.ais.RequestMessage;
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
	public void setResponse(Message res) {
		if (ModbusResponseMessage.class.isInstance(res)) {
			setResponse((ModbusResponseMessage)res);
		} else {
			throw(new AISException("A rensponse to a ModbusRequestMessage can only be a ModbusResponseMessage, not a " + res.getClass()));
		}		
	}

}
