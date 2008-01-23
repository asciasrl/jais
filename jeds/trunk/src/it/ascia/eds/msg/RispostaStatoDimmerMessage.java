package it.ascia.eds.msg;

public class RispostaStatoDimmerMessage extends PTPMessage {

	public RispostaStatoDimmerMessage(int d, int m, int Modello, int Versione) {
		Destinatario = d & 0xFF;
		Mittente = m & 0xFF;
		TipoMessaggio = 1;
		Byte1 = Modello & 0xFF;
		Byte2 = Versione & 0xFF;
	}

	public RispostaStatoDimmerMessage(int[] message) {
		parseMessage(message);
	}

	public String getTipoMessaggio() {
		return "Risposta Stato uscite Dimmer";
	}

	public String getInformazioni()	{
		StringBuffer s = new StringBuffer();
		s.append("Mittente: "+Mittente+"\r\n");
		s.append("Destinatario: "+Destinatario+"\r\n");
		s.append("Uscita 1: "+(Byte1 & 0x7F) +"%\r\n");
		s.append("Uscita 2: "+(Byte2 & 0x7F) +"%\r\n");
		return s.toString();
	}
	
	/**
	 * Ritorna lo stato delle uscite.
	 * 
	 * @returns un'array di 2 interi, anche se il BMC ha meno porte. I valori vanno da 0 a 100. 
	 */
	public int[] getOutputs() {
		int retval[] = {(Byte1 & 0x7F), (Byte2 & 0x7F)};
		return retval;
	}
}
