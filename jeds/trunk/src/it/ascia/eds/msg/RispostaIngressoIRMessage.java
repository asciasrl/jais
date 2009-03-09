package it.ascia.eds.msg;

public class RispostaIngressoIRMessage extends PTPMessage {

	public RispostaIngressoIRMessage(int d, int m, int Address, int Command) {
		Destinatario = d & 0xFF;
		Mittente = m & 0xFF;
		TipoMessaggio = 40;
		Byte1 = Address;
		Byte2 = Command;
	}
	
	public RispostaIngressoIRMessage(int[] message) {
		load(message);
	}

	public String getMessageDescription() {
		return "Risposta codice ingresso IR";
	}

	public String toString()	{
		StringBuffer s = new StringBuffer();
		s.append("Mittente: "+Mittente+"\r\n");
		s.append("Destinatario: "+Destinatario+"\r\n");
		if (Byte1 == 255 && Byte2 == 255) {
			s.append("Codice IR RC5: non definito\r\n");
		} else {
			s.append("Codice IR RC5: addr="+(Byte1 & 0xFF) + " command="+(Byte2 & 0xFF) + "\r\n");
		}
		return s.toString();
	}

	public int getMessageType() {
		return MSG_RISPOSTA_INGRESSO_IR;
	}
}
