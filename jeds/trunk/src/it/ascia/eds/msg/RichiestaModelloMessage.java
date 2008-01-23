package it.ascia.eds.msg;

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
		boolean retval = false;
		if (RispostaModelloMessage.class.isInstance(m)) {
			if ((getSender() == m.getRecipient()) &&
					(getRecipient() == m.getSender())) {
				retval = true;
			}
		}
		return retval;
	}
}
