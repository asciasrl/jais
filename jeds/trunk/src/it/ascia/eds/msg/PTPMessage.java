/**
 * Copyright (C) 2008 ASCIA S.R.L.
 */
package it.ascia.eds.msg;

/**
 * Messagio per il quale si aspetta come risposta un ACK (Acnowledge)
 * 
 * @author arrigo, sergio
 *
 */
public abstract class PTPMessage extends EDSMessage {
	/**
	 * Ritorna il numero massimo di tentativi di invio da effettuare.
	 * 
	 * Il messaggio viene re-inviato se non arriva risposta.
	 */
	public int getMaxSendTries() {
		return 4;
	}
		

	/**
	 * Time to wait for answer because of elaboration time by device
	 * @return milliseconds
	 * @since 20100514
	 */
	public double getRetryTimeout() {
		return 0;
	}
	
}
