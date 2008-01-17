package it.ascia.eds.msg;

public class RichiestaModelloMessage 
	extends Message
	implements MessageInterface {

	public RichiestaModelloMessage(int d, int m) {
		Destinatario = d & 0xFF;
		Mittente = m & 0xFF;
		TipoMessaggio = 0;
		Byte1 = 0;
		Byte2 = 0;
	}
	
	public RichiestaModelloMessage(int[] message) {
		parseMessage(message);
	}

	public String getTipoMessaggio() {
		return "Richiesta Modello e Revisione";
	}

	
}
