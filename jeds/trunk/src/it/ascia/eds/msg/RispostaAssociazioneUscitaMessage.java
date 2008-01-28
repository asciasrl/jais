package it.ascia.eds.msg;

/**
 * Risposta alla richiesta di associazione uscita a comando broadcast.
 * 
 * Questo messaggio puo' essere inviato da BMC standard o da dimmer. I dimmer
 * hanno piu' caselle dei BMC: 16 anziche' 8.
 * 
 * ATTENZIONE: sembra che il campo "casella" sia sempre 0.
 * 
 * Codice EDS: 16.
 */
public class RispostaAssociazioneUscitaMessage extends PTPMessage {

	/* Da scrivere se serve
	public RispostaAssociazioneUscitaMessage(int d, int m, int Modello, int Versione) {
		Destinatario = d & 0xFF;
		Mittente = m & 0xFF;
		TipoMessaggio = 1;
		Byte1 = Modello & 0xFF;
		Byte2 = Versione & 0xFF;
	} */

	public RispostaAssociazioneUscitaMessage(int[] message) {
		parseMessage(message);
	}

	public String getTipoMessaggio() {
		return "Risposta Associazione Uscite a Comando Broadcast";
	}

	public String getInformazioni()	{
		StringBuffer s = new StringBuffer();
		s.append("Mittente: "+Mittente+"\r\n");
		s.append("Destinatario: "+Destinatario+"\r\n");
		s.append("Comando broadcast: " + getComandoBroadcast() + "\r\n");
		s.append("Uscita': " + getUscita() + "\r\n");
		s.append("Velocita': " + getVelocita() + "\r\n");
		s.append("BMC\r\n casella: " + getCasellaBMC() + "\r\n");
		s.append(" attivazione: " + activatesBMC() + "\r\n");
		s.append("Dimmer:\r\n casella: " + getCasellaDimmer() + "\r\n");
		return s.toString();
	}
	
	/**
	 * Ritorna la velocita' di attuazione.
	 * 
	 * @return un codice numerico strano, si veda la documentazione del 
	 * messaggio 21.
	 */
	public int getVelocita() {
		return (Byte1 >> 3) & 0x0f;
	}
	
	/**
	 * Ritorna il numero dell'uscita (0 - 7).
	 */
	public int getUscita() {
		return Byte1 & 7;
	}
	
	/**
	 * Ritorna il numero della casella di attivazione per BMC.
	 * 
	 * Questo metodo va usato in alternativa a getCasellaDimmer().
	 * 
	 * @return un valore tra 0 e 3.
	 */
	public int getCasellaBMC() {
		return (Byte2 >> 6) & 3;
	}
	
	/**
	 * Rileva se il comando e' di attivazione o disattivazione.
	 * 
	 * Questa opzione e' valida solo per uscite di tipo "tapparella", e va
	 * usata in alternativa a getCasellaDimmer().
	 * 
	 * @return true se il comando e' di attivazione.
	 */
	public boolean activatesBMC() {
		return ((Byte2 & 0x20) != 0);
	}
	
	/**
	 * Ritorna il numero della casella di attivazione per dimmer.
	 * 
	 * Questo metodo va usato in alternativa a getCasellaBMC() e activatesBMC().
	 * 
	 * @return un valore tra 0 e 7.
	 */
	public int getCasellaDimmer() {
		return (Byte2 >> 5) & 7;
	}
	
	/**
	 * Ritorna il numero del comando broadcast associato.
	 * 
	 * @return un numero da 1 a 31, oppure 0 se nessun comando broadcast e' 
	 * associato.
	 */
	public int getComandoBroadcast() {
		return (Byte2 & 0x1f);
	}

	public int getMessageType() {
		return MSG_RISPOSTA_ASSOCIAZIONE_BROADCAST;
	}
	
}

