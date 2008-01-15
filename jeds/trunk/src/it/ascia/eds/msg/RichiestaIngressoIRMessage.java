package it.ascia.eds.msg;

public class RichiestaIngressoIRMessage 
	extends EDSMessage
	implements EDSMessageInterface {

	public RichiestaIngressoIRMessage(int d, int m, int Uscita) {
		Destinatario = d & 0xFF;
		Mittente = m & 0xFF;
		TipoMessaggio = 39;
		Byte1 = Uscita;
		Byte2 = 0;
	}
	
	public RichiestaIngressoIRMessage(int[] message) {
		parseMessage(message);
	}

	public String getTipoMessaggio() {
		return "Richiesta codice ingresso IR";
	}

	public String getInformazioni()	{
		StringBuffer s = new StringBuffer();
		s.append("Mittente: "+Mittente+"\r\n");
		s.append("Destinatario: "+Destinatario+"\r\n");
		s.append("Ingresso IR: "+(Byte1 & 0xFF) + "\r\n");
		return s.toString();
	}

	
}
