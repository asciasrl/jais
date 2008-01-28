/**
 * Copyright (C) 2008 ASCIA S.R.L.
 */
package it.ascia.eds.msg;

/**
 * Messaggio di richiesta stato per cronotermostato.
 * 
 * Codice EDS: 200.
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
		parseMessage(message);
	}

	public String getTipoMessaggio() {
		return "Richiesta Stato termostato";
	}
	
	public boolean isAnsweredBy(PTPMessage m) {
		if (m.getMessageType() == Message.MSG_TEMPERATURA) {
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
		return MSG_RICHIESTA_STATO_TERMOSTATO;
	}
}
