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
	 * True se il messaggio ha ricevuto una risposta.
	 */
	private boolean isAnswered = false;
	
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
