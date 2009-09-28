/**
 * Copyright (C) 2009 ASCIA S.R.L.
 */
package it.ascia.eds.msg;

/**
 * Codice EDS: 14
 */
public class ImpostaAssociazioneUscitaMessage extends PTPMessage {	
	
	// TODO Costruttore con parametri espliciti
	
	public ImpostaAssociazioneUscitaMessage(int[] message) {
		load(message);
	}

	public String getMessageDescription() {
		return "Imposta Associazione Uscita ad un Comando Broadcast";
	}
	
	/**
	 * Ritorna il numero dell'uscita che si vuole interrogare.
	 */
	public int getUscita() {
		return Byte1 & 0x07;
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

	/**
	 * Ritorna il numero massimo di tentativi di invio da effettuare.
	 * 
	 * Per il discovery non bisogna insistere.
	 */
	public int getMaxSendTries() {
		return 2;
	}

	public int getMessageType() {
		return MSG_IMPOSTA_ASSOCIAZIONE_BROADCAST;
	}

	/**
	 * Ritorna il numero dell'uscita del termostato (0 - 15).
	 */
	public int getUscitaT() {
		return Byte1 & 15;
	}

	/**
	 * Rileva se il comando e' di attivazione o disattivazione.
	 * 
	 * Questa opzione e' valida solo per uscite di tipo "tapparella", e va
	 * usata in alternativa a getCasellaDimmer().
	 * 
	 * @return true se il comando e' di attivazione.
	 */
	public boolean isActivation() {
		return ((Byte2 & 0x20) != 0);
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
	
	public String toString() {
		StringBuffer s = new StringBuffer();
		s.append(super.toString());
		s.append(" Uscita:" + getUscita()+"/"+getUscitaT() + " Casella:"+getCasellaBMC()+"/"+getCasellaDimmer()+" ");
		s.append(" Gruppo:" + getComandoBroadcast()+" ");
		if (isActivation()) {
			s.append("Attiva");
		} else {
			s.append("Disattiva");
		}
		s.append(" Velocita':" + getVelocita());
		return s.toString();
	}

}
