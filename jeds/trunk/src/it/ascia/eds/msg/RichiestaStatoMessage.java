/**
 * Copyright (C) 2008 ASCIA S.R.L.
 */
package it.ascia.eds.msg;

import it.ascia.ais.MessageInterface;

/**
 * Messaggio di richiesta stato.
 */
public class RichiestaStatoMessage extends PTPRequest
	implements MessageInterface {

	public RichiestaStatoMessage(int d, int m, int Uscite) {
		Destinatario = d & 0xFF;
		Mittente = m & 0xFF;
		TipoMessaggio = 25;
		Byte1 = Uscite;
		Byte2 = 0;
	}
	
	public RichiestaStatoMessage(int[] message) {
		load(message);
	}

	public String getMessageDescription() {
		return "Richiesta stato";
	}
	
	public boolean isAnsweredBy(PTPMessage m) {
		if ((m.getMessageType() == EDSMessage.MSG_RISPOSTA_STATO) ||
				(m.getMessageType() == EDSMessage.MSG_RISPOSTA_STATO_DIMMER)) {
			if ((getSender() == m.getRecipient()) &&
					(getRecipient() == m.getSender())) {
				answered = true;
			}
		}
		return answered;
	}
	
	/**
	 * Ritorna il numero massimo di tentativi di invio da effettuare.
	 * 
	 * Per richiedere uno stato non bisogna insistere.
	 */
	public int getMaxSendTries() {
		return 2;
	}

	public int getMessageType() {
		return MSG_RICHIESTA_STATO;
	}
}
