package it.ascia.eds.msg;

public class RichiestaStatoMessage 
	extends EDSMessage
	implements EDSMessageInterface {

	public RichiestaStatoMessage(int d, int m, int Uscite) {
		Destinatario = d & 0xFF;
		Mittente = m & 0xFF;
		TipoMessaggio = 25;
		Byte1 = Uscite;
		Byte2 = 0;
	}
	
	public RichiestaStatoMessage(int[] message) {
		parseMessage(message);
	}

	public String getTipoMessaggio() {
		return "Richiesta Stato";
	}

	
}
