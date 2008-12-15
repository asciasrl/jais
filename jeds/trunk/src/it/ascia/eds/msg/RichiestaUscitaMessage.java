/**
 * Copyright (C) 2008 ASCIA S.R.L.
 */
package it.ascia.eds.msg;

import it.ascia.ais.MessageInterface;

/**
 * Messaggio di richiesta lettura stato e configurazione uscita.
 */
public class RichiestaUscitaMessage extends PTPRequest
	implements MessageInterface {

	public RichiestaUscitaMessage(int d, int m, int uscita) {
		Destinatario = d & 0xFF;
		Mittente = m & 0xFF;
		TipoMessaggio = getMessageType();
		Byte1 = uscita & 0x07;
		Byte2 = 0;
	}
	
	public RichiestaUscitaMessage(int[] message) {
		load(message);
	}

	public String getMessageDescription() {
		return "Richiesta uscita";
	}
	
	public boolean isAnsweredBy(PTPMessage m) {
		if ((m.getMessageType() == EDSMessage.MSG_RISPOSTA_USCITA)) {
			if ((getSender() == m.getRecipient()) &&
					(getRecipient() == m.getSender())) {
				answered = true;
			}
		}
		return answered;
	}
	
	public int getMessageType() {
		return MSG_RICHIESTA_USCITA;
	}
}
