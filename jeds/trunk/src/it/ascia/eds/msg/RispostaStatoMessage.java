package it.ascia.eds.msg;

public class RispostaStatoMessage extends Message {

	public RispostaStatoMessage(int d, int m, int Uscite, int Entrate) {
		Destinatario = d & 0xFF;
		Mittente = m & 0xFF;
		TipoMessaggio = 26;
		Byte1 = Uscite & 0xFF;
		Byte2 = Entrate & 0xFF;
	}

	public RispostaStatoMessage(int[] message) {
		parseMessage(message);
	}

	public String getTipoMessaggio() {
		return "Risposta a richiesta Stato";
	}

	public String getInformazioni()	{
		StringBuffer s = new StringBuffer();
		s.append("Mittente: "+Mittente+"\r\n");
		s.append("Destinatario: "+Destinatario+"\r\n");
		s.append("Uscite:");
		for (int i = 0; i <= 7; i++) {
			s.append((i+1)+"="+((Byte1 >> i) & 0x01)+"  ");
		} 
		s.append("\r\n");
		s.append("Entrate:");
		for (int i = 0; i <= 7; i++) {
			s.append((i+1)+"="+((Byte2 >> i) & 0x01)+"  ");
		} 
		s.append("\r\n");
		return s.toString();
	}

	public boolean isBroadcast() {
		return false;
	}
}
