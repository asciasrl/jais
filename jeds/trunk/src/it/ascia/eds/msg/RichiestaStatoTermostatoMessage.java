/**
 * Copyright (C) 2008 ASCIA S.R.L.
 */
package it.ascia.eds.msg;

import it.ascia.ais.MessageInterface;

/**
 * Messaggio di richiesta stato per cronotermostato o sonda termica.
 * 
 * <p>Codice EDS: 200.</p>
 */
public class RichiestaStatoTermostatoMessage extends PTPRequest
	implements MessageInterface {

	public RichiestaStatoTermostatoMessage(int d, int m) {
		Destinatario = d & 0xFF;
		Mittente = m & 0xFF;
		TipoMessaggio = getMessageType();
		Byte1 = 0;
		Byte2 = 0;
	}
	
	public RichiestaStatoTermostatoMessage(int[] message) {
		load(message);
	}

	public String getMessageDescription() {
		return "Richiesta Stato termostato";
	}
	
	public boolean isAnsweredBy(PTPMessage m) {
		if (m.getMessageType() == EDSMessage.MSG_TEMPERATURA) {
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
	 * <p>Per richiedere uno stato non bisogna insistere.</p>
	 */
	public int getMaxSendTries() {
		return 2;
	}

	public int getMessageType() {
		return MSG_RICHIESTA_STATO_TERMOSTATO;
	}
}
