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
	public boolean answered = false;
	
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
		if (m.getMessageType() == EDSMessage.MSG_ACKNOWLEDGE) {
			AcknowledgeMessage ack = (AcknowledgeMessage) m;
			if ((getSender() == ack.getRecipient()) &&
					(getRecipient() == ack.getSender()) &&
					(ack.Byte1 == Byte1) && (ack.Byte2 == Byte2)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Ritorna il numero massimo di tentativi di invio da effettuare.
	 */
	public int getMaxSendTries() {
		return 8;
	}

	
	public final boolean wantsReply() {
		return true;
	}
}
