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
public class RispostaAssociazioneUscitaMessage extends PTPResponse {

	/**
	 * Costruisce il messaggio a partire dai dati ricevuti.
	 */
	public RispostaAssociazioneUscitaMessage(int[] message) {
		load(message);
	}
	
	/**
	 * Costruisce il messaggio come risposta alla richiesta specificata.
	 * 
	 * @param m messaggio a cui rispondere.
	 * @param velocita velocita' di risposta (0 - 15)
	 * @param attivazione se non si parla di dimmer, indica se l'uscita viene
	 * attivata o disattivata dal comando broadcast. Se si parla di dimmer,
	 * questo parametro viene ignorato.
	 * @param comandoBroadcast comando broadcast a cui l'uscita e' associata. Se
	 * 0, indica che non c'e' associazione.
	 */
	public 
		RispostaAssociazioneUscitaMessage(RichiestaAssociazioneUscitaMessage m,
				int velocita, boolean attivazione, int comandoBroadcast) {
		Destinatario = m.getSender();
		Mittente = m.getRecipient();
		TipoMessaggio = getMessageType();
		Byte1 = (velocita & 0x0f) << 3 | m.getUscita();
		// Il messaggio di richiesta puo' essere per un dimmer. In tal caso,
		// ignoriamo il parametro "attivazione"
		if (m.getCasella() > 3) { // E' un dimmer!
			Byte2 = m.getCasella() << 5;
		} else { // Non e' un dimmer
			Byte2 = m.getCasella() << 6;
			if (attivazione) {
				Byte2 = Byte2 | 1 << 5;
			}
		}
		Byte2 = Byte2 | (comandoBroadcast & 0x1f);
	}

	public String getMessageDescription() {
		return "Risposta Associazione Uscite a Comando Broadcast";
	}

	public String toString()	{
		StringBuffer s = new StringBuffer();
		s.append(super.toString());
		s.append(" Uscita:" + getUscita()+"/"+getUscitaT() + " Casella:"+getCasellaBMC()+"/"+getCasellaDimmer()+" ");
		s.append(" Gruppo:" + getComandoBroadcast()+" ");
		if (activatesBMC()) {
			s.append("Attiva");
		} else {
			s.append("Disattiva");
		}
		s.append(" Velocita':" + getVelocita());
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
	 * Ritorna il numero dell'uscita del termostato (0 - 15).
	 */
	public int getUscitaT() {
		return Byte1 & 15;
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

