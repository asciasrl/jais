package it.ascia.eds.msg;

/**
 * 
 * @author sergio
 * TODO Distinguere fra dispositivi di input e termostato
 */
public class TemperatureMessage
	extends Message
	implements MessageInterface
	{

	public TemperatureMessage(int d, int m, int Attivazione, int Uscita, int Variazione)
	  throws Exception {
		Destinatario = d & 0xFF;
		Mittente = m & 0xFF;
		TipoMessaggio = 201;
		Byte1 = Uscita & 0x07 + ((Attivazione & 0x01) << 3);
		Byte2 = Variazione & 0x01;
	}

	public TemperatureMessage(int[] message) {
		parseMessage(message);
	}

	public String getTipoMessaggio() {
		return "Temperatura";
	}
	
	public String getInformazioni()	{
		StringBuffer s = new StringBuffer();
		s.append("Mittente: "+Mittente+"\r\n");
		s.append("Destinatario: "+Destinatario+"\r\n");
		if ((Byte2 & 0x80) > 0) {
			s.append("Inverno\r\n");
		} else {
			s.append("Estate\r\n");
		}
		s.append("Temperatura: "+(Byte1 & 0xFF) +","+((Byte2 & 0xF0) >> 4) +"\r\n");
		s.append("Altro:"+(Byte2 & 0x0F)+"\r\n");
		return s.toString();
	}
	
	public boolean isBroadcast() {
		return false;
	}
}
