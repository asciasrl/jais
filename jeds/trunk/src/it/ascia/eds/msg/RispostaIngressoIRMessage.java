package it.ascia.eds.msg;

public class RispostaIngressoIRMessage 
	extends Message
	implements MessageInterface {

	public RispostaIngressoIRMessage(int d, int m, int Address, int Command) {
		Destinatario = d & 0xFF;
		Mittente = m & 0xFF;
		TipoMessaggio = 40;
		Byte1 = Address;
		Byte2 = Command;
	}
	
	public RispostaIngressoIRMessage(int[] message) {
		parseMessage(message);
	}

	public String getTipoMessaggio() {
		return "Risposta codice ingresso IR";
	}

	public String getInformazioni()	{
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

	
}
