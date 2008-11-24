package it.ascia.eds.msg;

import it.ascia.ais.MessageInterface;

/**
 * Un messaggio di richiesta modello e revisione.
 * 
 * @author sergio
 */
public class RichiestaModelloMessage extends PTPRequest
	implements MessageInterface {

	/**
	 * Costruttore.
	 * @param d indirizzo del destinatario
	 * @param m indirizzo del mittente
	 */
	public RichiestaModelloMessage(int d, int m) {
		Destinatario = d & 0xFF;
		Mittente = m & 0xFF;
		TipoMessaggio = 0;
		Byte1 = 0;
		Byte2 = 0;
	}
	
	public RichiestaModelloMessage(int[] message) {
		parseMessage(message);
	}

	public String getTipoMessaggio() {
		return "Richiesta Modello e Revisione";
	}

	public boolean isAnsweredBy(PTPMessage m) {
		if (m.getMessageType() == EDSMessage.MSG_RISPOSTA_MODELLO) {
			if ((getSender() == m.getRecipient()) &&
					(getRecipient() == m.getSender())) {
				answered = true;
			}
		}
		return wasAnswered();
	}
	
	/**
	 * Ritorna il numero massimo di tentativi di invio da effettuare.
	 * 
	 * Per il discovery non bisogna insistere.
	 */
	public int getMaxSendTries() {
		return 2;
	}

	public int getMessageType() {
		return MSG_RICHIESTA_MODELLO;
	}
}
