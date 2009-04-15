package it.ascia.eds.msg;

public class RichiestaRTCCMessage extends PTPRequest {

	public RichiestaRTCCMessage(int d, int m, int index) {
		Destinatario = d & 0xFF;
		Mittente = m & 0xFF;
		TipoMessaggio = getMessageType();
		Byte1 = index & 0x03;
		Byte2 = 0x00;		
	}
	
	public RichiestaRTCCMessage(int[] message) {
		load(message);
	}

	public String getMessageDescription() {
		return "Richiesta dati RTCC";
	}

	public int getMessageType() {
		return MSG_RICHIESTA_RTCC;
	}

	public boolean isAnsweredBy(PTPMessage m) {
		if (m.getMessageType() == EDSMessage.MSG_RISPOSTA_RTCC) {
			if (getSender() == m.getRecipient() && getRecipient() == m.getSender()) {
				return true;
			}
		}
		return false;
	}

}
