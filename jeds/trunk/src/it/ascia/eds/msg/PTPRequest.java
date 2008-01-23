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
	 * Verifica se un messaggio risponde a questo.
	 * 
	 * Questo metodo ha senso solo per messaggi di tipo ACK, cioe' non-broadcast.
	 * 
	 * @return true se m e' una risposta a this.
	 */
	public abstract boolean isAnsweredBy(PTPMessage m);
}
