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
	 * Questo metodo riconosce risposte di tipo "Acknowledge". Se le sottoclassi
	 * richiedono messaggi di risposta diversi, devono riscrivere questo metodo.
	 * 
	 * @return true se m e' la risposta a questo messaggio.
	 */
	public boolean isAnsweredBy(PTPMessage m) {
		if (m.getMessageType() == Message.MSG_ACKNOWLEDGE) {
			AcknowledgeMessage ack = (AcknowledgeMessage) m;
			if ((getSender() == ack.getRecipient()) &&
					(getRecipient() == ack.getSender()) &&
					ack.hasBytes(Byte1, Byte2)) {
				answered = true;
			}
		}
		return answered;
	}

	
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
