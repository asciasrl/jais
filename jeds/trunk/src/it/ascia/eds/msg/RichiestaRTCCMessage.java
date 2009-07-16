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
	
	public int getIndex() {
		return Byte1 & 0x03;
	}

	public String toString() {
		StringBuffer s = new StringBuffer();
		s.append(super.toString());
		s.append(" index:"+getIndex());
		switch (getIndex()) {
			case 0:
				s.append(" ore/minuti");
				break;
			case 1:
				s.append(" mese/anno");
				break;
			case 2:
				s.append(" giorno/secondi");
				break;
		}
		return s.toString();
	}

}
