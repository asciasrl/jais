package it.ascia.modbus;

import java.io.IOException;

import net.wimpi.modbus.Modbus;
import net.wimpi.modbus.io.BytesOutputStream;
import it.ascia.ais.Message;

public abstract class ModbusMessage extends Message {

	net.wimpi.modbus.msg.ModbusMessageImpl msg = null;
	
	@Override
	public String toString() {
		return msg.getHexMessage();
	}

	@Override
	public byte[] getBytesMessage() {
		BytesOutputStream m_ByteOut =
		      new BytesOutputStream(Modbus.MAX_MESSAGE_LENGTH);
		try {
			msg.writeData(m_ByteOut);
		} catch (IOException e) {
		}
		return m_ByteOut.toByteArray();
	}

}
