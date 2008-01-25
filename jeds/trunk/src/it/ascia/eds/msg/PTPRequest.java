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
	protected boolean answered = false;
	
	/**
	 * Verifica se un messaggio risponde a questo.
	 * 
	 * Il risultato viene deve essere ritornato e impostato nell'attributo
	 * answered, che viene letto da wasAnswered().
	 * 
	 * @return true se m e' la risposta a questo messaggio.
	 */
	public abstract boolean isAnsweredBy(PTPMessage m);
	
	/**
	 * Controlla se il messaggio ha ricevuto una risposta.
	 * 
	 * Le risposte devono prima essere verificate dal metodo checkAnswer().
	 * 
	 * @return true se e' stata ricevuta una risposta a questo messaggio.
	 */
	public final boolean wasAnswered() {
		return answered;
	}
	
	public final boolean wantsReply() {
		return true;
	}
}
