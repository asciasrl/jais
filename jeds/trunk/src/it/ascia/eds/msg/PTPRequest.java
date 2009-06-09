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
	
	public abstract boolean isAnsweredBy(PTPMessage m);
	
}
