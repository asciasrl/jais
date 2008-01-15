package it.ascia.eds.msg;

/**
 * 
 * @author sergio
 * TODO Distinguere fra dispositivi di input e termostato
 */
public class VariazioneIngressoMessage
	extends EDSMessage
	implements EDSMessageInterface
	{

	public VariazioneIngressoMessage(int d, int m, int Attivazione, int Uscita, int Variazione) {
		Destinatario = d & 0xFF;
		Mittente = m & 0xFF;
		TipoMessaggio = 4;
		Byte1 = Uscita & 0x07 + ((Attivazione & 0x01) << 3);
		Byte2 = Variazione & 0x01;
	}

	public VariazioneIngressoMessage(int[] message) {
		parseMessage(message);
	}

	public String getTipoMessaggio() {
		return "Variazione di un ingresso";
	}
	
	public String getInformazioni()	{
		StringBuffer s = new StringBuffer();
		s.append("Mittente: "+Mittente+"\r\n");
		s.append("Destinatario: "+Destinatario+"\r\n");
		if ((Byte1 & 0x08) == 0) {
			s.append("Attivazione/Incremento\r\n");
		} else {
			s.append("Disattivazione/Decremento\r\n");
		}
		s.append("Numero uscita: "+((Byte1 & 0x07) + 1)+"\r\n");
		s.append("Variazione: "+(Byte2 & 0x01)+"\r\n");
		return s.toString();
	}
	
	
}
