package it.ascia.eds.msg;

public class ComandoUscitaMessage extends PTPRequest
	implements MessageInterface {

	public ComandoUscitaMessage(int d, int m, int Tempo, int Uscita, int Percentuale, int Attivazione) {
		Destinatario = d & 0xFF;
		Mittente = m & 0xFF;
		TipoMessaggio = 21;
		Byte1 = (Uscita & 0x07) + ((Tempo & 0x0F) << 3);
		Byte2 = (Attivazione & 0x01) + ((Percentuale & 0x7F) << 1);
	}
	
	public ComandoUscitaMessage(int[] message) {
		parseMessage(message);
	}

	public String getTipoMessaggio() {
		return "Comando uscita";
	}

	public String getInformazioni()	{
		StringBuffer s = new StringBuffer();
		s.append("Mittente: "+Mittente+"\r\n");
		s.append("Destinatario: "+Destinatario+"\r\n");
		s.append("Uscita: "+(Byte1 & 0x07)+"\r\n");
		if ((Byte2 & 0x01) == 1) {
			s.append("Attivazione/Incremento\r\n");
		} else {
			s.append("Disattivazione/Decremento\r\n");
		}
		int tempo = (Byte1 >> 3) & 0x0F;
		s.append("Tempo attivazione: ");
		switch (tempo) {
		case 0:
			s.append("Soft speed\r\n");
			break;
		case 1:
			s.append("Istantaneo\r\n");
			break;
		default:
			s.append("Variazione 0-100% in "+(tempo/10)+"s\r\n");
			break;
		}

		int percentuale = ((Byte2 >> 1) & 0x7F);
		s.append("Percentuale: ");
		if (percentuale == 0) {
			s.append("modo rele'");
		} else if (percentuale > 100) {
			s.append("Valore precedente");
		} else {
			s.append("Accende al "+percentuale+"%\r\n");
		}
		s.append("\r\n");
		return s.toString();
	}

	public boolean isAnsweredBy(PTPMessage m) {
		boolean retval = false;
		if (AknowledgeMessage.class.isInstance(m)) {
			if ((getSender() == m.getRecipient()) &&
					(getRecipient() == m.getSender())) {
				retval = true;
			}
		}
		return retval;
	}
}
