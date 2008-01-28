package it.ascia.eds.msg;

/**
 * Imposta l'uscita di un BMC Standard I/O oppure dimmer.
 * 
 * Questo messaggio puo' essere inviato anche dal cronotermostato.
 * 
 * Codice EDS: 21.
 * 
 * @author sergio, arrigo
 */
public class ComandoUscitaMessage extends PTPRequest
	implements MessageInterface {

	public ComandoUscitaMessage(int d, int m, int Tempo, int Uscita, int Percentuale, int Attivazione) {
		Destinatario = d & 0xFF;
		Mittente = m & 0xFF;
		TipoMessaggio = Message.MSG_COMANDO_USCITA; // 21;
		Byte1 = (Uscita & 0x07) + ((Tempo & 0x0F) << 3);
		Byte2 = (Attivazione & 0x01) + ((Percentuale & 0x7F) << 1);
	}
	
	public ComandoUscitaMessage(int[] message) {
		parseMessage(message);
	}

	public String getTipoMessaggio() {
		return "Comando uscita";
	}
	
	/**
	 * Ritorna il numero dell'uscita interessata dal comando.
	 */
	public int getOutputPortNumber() {
		return (Byte1 & 0x07);
	}
	
	/**
	 * Ritorna la percentuale di luce richiesta, se il comando e' per un dimmer.
	 */
	public int getPercentage() {
		return ((Byte2 >> 1) & 0x7F);
	}
	
	/**
	 * Verifica se il comando e' di attivazione/incremento o 
	 * disattivazione/decremento.
	 * 
	 * @return true se e' un'attivazione/incremento.
	 */
	public boolean isActivation() {
		return ((Byte2 & 0x01) == 1);
	}

	public String getInformazioni()	{
		StringBuffer s = new StringBuffer();
		s.append("Mittente: "+Mittente+"\r\n");
		s.append("Destinatario: "+Destinatario+"\r\n");
		s.append("Uscita: "+ getOutputPortNumber() +"\r\n");
		if (isActivation()) {
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

		int percentuale = getPercentage();
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

	public int getMessageType() {
		return MSG_COMANDO_USCITA;
	}
}
