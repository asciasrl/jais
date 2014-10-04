/**
 * Copyright (C) 2014 ASCIA S.R.L.
 * @author sergio@ascia.it
 */
package it.ascia.eds.msg;

import it.ascia.ais.AISException;
import it.ascia.ais.RequestMessage;
import it.ascia.ais.ResponseMessage;

/**
 * Messaggi Point-to-point che si aspettano una risposta ACK
 */
public abstract class PTPCommand extends PTPRequest implements RequestMessage {
	
	public void setResponse(ResponseMessage res) {
		if (AcknowledgeMessage.class.isInstance(res)) {
			setResponse((PTPResponse)res);
		} else {
			throw(new AISException("A rensponse to a PTPCommand can only be a AcknowledgeMessage, not a " + res.getClass()));
		}		
		
	}
	
	/**
	 * Verifica se un messaggio risponde a questo.
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
	
}
