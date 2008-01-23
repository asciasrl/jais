package it.ascia.eds.msg;

import java.util.Random;

/**
 * 
 * @author sergio
 */
public class ComandoBroadcastMessage
	extends BroadcastMessage
	implements MessageInterface
	{

	/**
	 * 
	 * @param Numero Numero comando broadcast
	 * @param Attivazione Attivare o disattivare
	 * @param Modalita Modalit� di funzionamento (1=MODALITA� RISPARMIO DIMMER)
	 * @throws Exception
	 */
	public ComandoBroadcastMessage(int Numero, boolean Attivazione, int Modalita)
	  throws Exception {
		Random r = new Random();
		Destinatario = r.nextInt() & 0xFF;
		Mittente = r.nextInt() & 0xFF;
		TipoMessaggio = 17;
		Byte1 = (Attivazione ? 0 : 1) & 0x01 + ((Modalita & 0x7F) << 1); 
		Byte2 = Numero & 0x1F;
	}

	public ComandoBroadcastMessage(int[] message) {
		parseMessage(message);
	}

	public String getTipoMessaggio() {
		return "Messaggio Broadcast";
	}
	
	public String getInformazioni()	{
		StringBuffer s = new StringBuffer();
		s.append("Timestamp: "+((Mittente & 0xFF) * 0x100 + (Destinatario & 0xFF)) +"\r\n");
		if ((Byte1 & 0x01) == 0) {
			s.append("Attivazione/Incremento\r\n");
		} else {
			s.append("Disattivazione/Decremento\r\n");
		}
		s.append("Numero comando: "+(Byte2 & 0x1F)+"\r\n");
		s.append("Modalita: "+((Byte1 >> 1) & 0x7F)+"\r\n");
		return s.toString();
	}
}
