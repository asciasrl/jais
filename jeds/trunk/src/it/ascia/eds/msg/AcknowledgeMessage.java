package it.ascia.eds.msg;

public class AcknowledgeMessage extends PTPMessage {

	public AcknowledgeMessage(byte d, byte m, byte b1, byte b2) {
		Destinatario = d;
		Mittente = m;
		TipoMessaggio = 6;
		Byte1 = b1;
		Byte2 = b2;
	}

	public AcknowledgeMessage(int[] message) {
		parseMessage(message);
	}

	public String getTipoMessaggio() {
		return "Aknowledge";
	}

	public int getMessageType() {
		return MSG_ACKNOWLEDGE;
	}

}
