package it.ascia.eds.msg;	

import it.ascia.ais.MessageInterface;

/**
 * Imposta l'uscita di un BMC Standard I/O oppure dimmer.
 * 
 * <p>Questo messaggio agisce "a basso livello", ignorando timer o
 * programmazioni particolari del BMC: imposta l'uscita e basta. Se si vuole
 * che il BMC tenga conto di timer & co, allora bisogna inviare un messaggio di
 * tipo "variazione ingresso" anziché questo.</p>
 * 
 * <p>Questo messaggio puo' essere inviato anche dal cronotermostato.</p>
 * 
 * Codice EDS: 21.
 * 
 * @author sergio, arrigo
 */
public class ComandoUscitaMessage extends PTPRequest
	implements MessageInterface {

	/**
	 * Costruttore per messaggio per dimmer.
	 * 
	 * @param d indirizzo destinatario.
	 * @param m indirizzo mittente.
	 * @param Tempo tempo di accensione (vedi protocollo).
	 * @param Uscita numero dell'uscita da attivare.
	 * @param Percentuale percentuale di luminosità richiesta.
	 * @param Attivazione 1 per accendere, 0 per spegnere.
	 */
	public ComandoUscitaMessage(int d, int m, int Tempo, int Uscita, 
			int Percentuale, int Attivazione) {
		Destinatario = d & 0xFF;
		Mittente = m & 0xFF;
		TipoMessaggio = EDSMessage.MSG_COMANDO_USCITA; // 21;
		Byte1 = (Uscita & 0x07) + ((Tempo & 0x0F) << 3);
		Byte2 = (Attivazione & 0x01) + ((Percentuale & 0x7F) << 1);
	}
	
	/**
	 * Costruttore per messaggio per BMCStandardIO.
	 * 
	 * @param d indirizzo destinatario.
	 * @param m indirizzo mittente.
	 * @param Uscita numero dell'uscita da attivare.
	 * @param Attivazione 1 per accendere, 0 per spegnere.
	 */
	public ComandoUscitaMessage(int d, int m, int Uscita, boolean Attivazione) {
		Destinatario = d & 0xFF;
		Mittente = m & 0xFF;
		TipoMessaggio = EDSMessage.MSG_COMANDO_USCITA; // 21;
		Byte1 = (Uscita & 0x07);
		if (Attivazione) {
			Byte2 = 1; 
		} else {
			Byte2 = 0;
		}
	}
	
	public ComandoUscitaMessage(int[] message) {
		load(message);
	}

	public String getMessageDescription() {
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

	public String toString()	{
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
