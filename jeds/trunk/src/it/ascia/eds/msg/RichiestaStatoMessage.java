package it.ascia.eds.msg;

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
		parseMessage(message);
	}

	public String getTipoMessaggio() {
		return "Richiesta Stato";
	}
	
	public boolean isAnsweredBy(PTPMessage m) {
		if ((RispostaStatoMessage.class.isInstance(m)) ||
				(RispostaStatoDimmerMessage.class.isInstance(m))) {
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
}
