/**
 * Copyright (C) 2008 ASCIA S.R.L.
 */
package it.ascia.eds.msg;

/**
 * Messaggio di richiesta set point del cronotermostato.
 * 
 * <p>Codice EDS: 204.</p>
 */
public class RichiestaSetPointMessage extends PTPRequest {

	public RichiestaSetPointMessage(int d, int m) {
		Destinatario = d & 0xFF;
		Mittente = m & 0xFF;
		TipoMessaggio = getMessageType();
		Byte1 = 248; // TODO perche' ?  Lo invia cosi' edsconfig
		Byte2 = 0;
	}
	
	public RichiestaSetPointMessage(int[] message) {
		load(message);
	}

	public String getMessageDescription() {
		return "Richiesta set point cronotermostato";
	}
	
	public boolean isAnsweredBy(PTPMessage m) {
		if (m.getMessageType() == EDSMessage.MSG_LETTURA_SET_POINT
				|| m.getMessageType() == EDSMessage.MSG_RISPOSTA_SET_POINT) {
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
		return MSG_RICHIESTA_SET_POINT;
	}
}
