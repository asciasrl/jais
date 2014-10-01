package it.ascia.duemmegi.fxpxt.msg;

import it.ascia.duemmegi.fxpxt.FXPXTMessage;
import it.ascia.duemmegi.fxpxt.FXPXTRequestMessage;
import it.ascia.duemmegi.fxpxt.FXPXTResponseMessage;

public class RichiestaStatoUsciteMessage extends FXPXTRequestMessage {

	public RichiestaStatoUsciteMessage(int[] message) {
		load(message);
	}

	public RichiestaStatoUsciteMessage(int ind) {
		funzione = 0x82;
		indirizzo = ind & 0xFF;
		tipo = RICHIESTA_STATO_USCITE;
		dato1 = 0x33;
		dato0 = 0x33;			
	}

	public RichiestaStatoUsciteMessage(String address) {
		this((new Integer(address)).intValue());
	}

	public boolean isAnsweredBy(FXPXTMessage m) {
		if (FXPXTResponseMessage.class.isInstance(m)
				&& m.getMessageType() == RISPOSTA_STATO_USCITE
				&& ((FXPXTResponseMessage) m).getSource().equals(getDestination())) {
			return true;
		} else {
			return false;
		}
	}

}
