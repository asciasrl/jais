package it.ascia.eds.msg;

public class ComandoUscitaDimmerMessage 
	extends PTPMessage
	implements MessageInterface {

	/**
	 * 51
	 */
	public static final int TIPO = 51;

	public ComandoUscitaDimmerMessage(int d, int m, int Uscita, int Percentuale) {
		Destinatario = d & 0xFF;
		Mittente = m & 0xFF;
		TipoMessaggio = TIPO;
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
}
