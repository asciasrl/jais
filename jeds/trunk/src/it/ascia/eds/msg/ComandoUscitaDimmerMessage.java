package it.ascia.eds.msg;

/**
 * Comando per impostare l'uscita di un dimmer.
 * 
 * Questo messaggio, pur essendo point-to-point, non richiede risposta.
 * 
 * Codice EDS: 51.
 */
public class ComandoUscitaDimmerMessage extends PTPMessage
	implements MessageInterface {
	
	public ComandoUscitaDimmerMessage(int d, int m, int Uscita, int Percentuale) {
		Destinatario = d & 0xFF;
		Mittente = m & 0xFF;
		TipoMessaggio = Message.MSG_COMANDO_USCITA_DIMMER;
		Byte1 = (Percentuale & 0x7f);
		Byte2 = (Uscita & 0x01);
	}
	
	public ComandoUscitaDimmerMessage(int[] message) {
		parseMessage(message);
	}

	public String getTipoMessaggio() {
		return "Comando uscita dimmer";
	}

	public String getInformazioni()	{
		StringBuffer s = new StringBuffer();
		s.append("Mittente: "+Mittente+"\r\n");
		s.append("Destinatario: "+Destinatario+"\r\n");
		s.append("Uscita "+(Byte2 + 1)+": "+Byte1+"%\r\n");
		return s.toString();
	}

	public int getMessageType() {
		return MSG_COMANDO_USCITA_DIMMER;
	}
}
