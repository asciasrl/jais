/**
 * Copyright (C) 2008 ASCIA S.R.L.
 */
package it.ascia.eds.msg;

/**
 * Richiede a quale gruppo di attuazione multipla appartiene un'uscita.
 * 
 * Sul transport possono essere lanciati fino a 31 comandi broadcast diversi.
 * Alcuni BMC possono rispondere a questi comandi broadcast, attivando o 
 * spegnendo le proprie uscite.
 * 
 * Le uscite hanno delle "caselle", che sono possibilità di programmazione.
 * Dentro ciascuna casella si scrive:
 *   - se l'uscita deve accendersi o spegnersi;
 *   - qual e' il comando broadcast a cui deve rispondere.
 *   
 * Il discovery delle associazioni a comandi broadcast deve avvenire richiedendo
 * tutte le caselle per tutte le uscite di un BMC.
 * 
 * Codice EDS: 15.
 */
public class RichiestaAssociazioneUscitaMessage extends PTPRequest {	
	/**
	 * Costruttore.
	 * 
	 * @param d indirizzo destinatario.
	 * @param m indirizzo mittente.
	 * @param uscita numero dell'uscita da verificare.
	 * @param casella numero della casella da verificare (0 - 7).
	 */
	public RichiestaAssociazioneUscitaMessage(int d, int m, int uscita, 
			int casella) {
		Destinatario = d & 0xFF;
		Mittente = m & 0xFF;
		TipoMessaggio = 15;
		Byte1 = uscita & 7;
		Byte2 = casella & 7;
	}
	
	public RichiestaAssociazioneUscitaMessage(int[] message) {
		load(message);
	}

	public String getMessageDescription() {
		return "Richiesta Associazione Uscita ad un Comando Broadcast";
	}
	
	/**
	 * Ritorna il numero dell'uscita che si vuole interrogare.
	 */
	public int getUscita() {
		return Byte1 & 7;
	}
	
	/**
	 * Ritorna il numero della casella sull'uscita che si vuole interrogare.
	 */
	public int getCasella() {
		return Byte2 & 7;
	}
	
	/*
	 * ATTENZIONE: sembra che il campo "casella" della risposta sia sempre 0.
	 * Per questo motivo, non viene considerato qui.
	 */
	public boolean isAnsweredBy(PTPMessage m) {
		if (m.getMessageType() == EDSMessage.MSG_RISPOSTA_ASSOCIAZIONE_BROADCAST) {
			RispostaAssociazioneUscitaMessage r = 
				(RispostaAssociazioneUscitaMessage) m;
			if ((getSender() == r.getRecipient()) &&
					(getRecipient() == r.getSender()) 
					 /* && (getUscita() == r.getUscita()) */ ) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Ritorna il numero massimo di tentativi di invio da effettuare.
	 * 
	 * Per il discovery non bisogna insistere.
	 */
	public int getMaxSendTries() {
		return 2;
	}

	public int getMessageType() {
		return MSG_RICHIESTA_ASSOCIAZIONE_BROADCAST;
	}
	
	public String toString() {
		StringBuffer s = new StringBuffer();
		s.append(super.toString());
		s.append(" Uscita:" + getUscita() + " Casella:"+getCasella());
		return s.toString();		
	}
}
