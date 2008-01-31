/**
 * Copyright (C) 2008 ASCIA S.R.L.
 */
package it.ascia.eds.msg;

/**
 * @author arrigo
 *
 */
public abstract class PTPMessage extends Message {
	/**
	 * Ritorna il numero massimo di tentativi di invio da effettuare.
	 * 
	 * Il messaggio viene re-inviato se non arriva risposta.
	 */
	public int getMaxSendTries() {
		return 4;
	}
	
	public final boolean isBroadcast() {
		return false;
	}
	
	/**
	 * Ritorna true se questo messaggio si aspetta una risposta.
	 * 
	 * @return true se questo e' un PTPRequest.
	 */
	public boolean wantsReply() {
		return false;
	}
}
