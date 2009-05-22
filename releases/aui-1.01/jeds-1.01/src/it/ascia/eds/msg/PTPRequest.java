/**
 * Copyright (C) 2008 ASCIA S.R.L.
 */
package it.ascia.eds.msg;

/**
 * @author arrigo
 *
 * Messaggi Point-to-point che si aspettano una risposta.
 */
public abstract class PTPRequest extends PTPMessage {
	/**
	 * True se il messaggio ha ricevuto una risposta.
	 */
	private boolean isAnswered = false;
	
	/**
	 * Ritorna il numero massimo di tentativi di invio da effettuare.
	 */
	public int getMaxSendTries() {
		return 2;
	}

	/**
	 * @param isAnswered the isAnswered to set
	 */
	public void setAnswered(boolean isAnswered) {
		this.isAnswered = isAnswered;
	}

	/**
	 * @return the isAnswered
	 */
	public boolean isAnswered() {
		return isAnswered;
	}
}
