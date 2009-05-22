/**
 * Copyright (C) 2008 ASCIA S.R.L.
 */
package it.ascia.eds.msg;

/**
 * Messaggio di richiesta stato per cronotermostato o sonda termica.
 * 
 * <p>Codice EDS: 200.</p>
 */
public class RichiestaStatoTermostatoMessage extends PTPRequest {

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
		if (m.getMessageType() == EDSMessage.MSG_RISPOSTA_STATO_TERMOSTATO) {
			if ((getSender() == m.getRecipient()) &&
					(getRecipient() == m.getSender())) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Ritorna il numero massimo di tentativi di invio da effettuare.
	 * 
	 * <p>Per richiedere uno stato non bisogna insistere.</p>
	 */
	public int getMaxSendTries() {
		return 3;
	}

	public int getMessageType() {
		return MSG_RICHIESTA_STATO_TERMOSTATO;
	}
}
