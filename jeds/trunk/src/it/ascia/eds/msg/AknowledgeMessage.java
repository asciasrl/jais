package it.ascia.eds.msg;

public class AknowledgeMessage extends Message {

	public AknowledgeMessage(byte d, byte m, byte b1, byte b2) {
		Destinatario = d;
		Mittente = m;
		TipoMessaggio = 6;
		Byte1 = b1;
		Byte2 = b2;
	}

	public AknowledgeMessage(int[] message) {
		parseMessage(message);
	}

	public String getTipoMessaggio() {
		return "Aknowledge";
	}
	
}
