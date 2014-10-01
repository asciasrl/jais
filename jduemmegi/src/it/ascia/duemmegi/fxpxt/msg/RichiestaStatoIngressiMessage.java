package it.ascia.duemmegi.fxpxt.msg;

import it.ascia.duemmegi.fxpxt.FXPXTMessage;
import it.ascia.duemmegi.fxpxt.FXPXTRequestMessage;
import it.ascia.duemmegi.fxpxt.FXPXTResponseMessage;

public class RichiestaStatoIngressiMessage extends FXPXTRequestMessage {

	public RichiestaStatoIngressiMessage(int ind) {
		funzione = 0x82;
		indirizzo = ind & 0xFF;		
		tipo = RICHIESTA_STATO_INGRESSO;
		dato1 = 0x33;
		dato0 = 0x33;			
	}
	
	public RichiestaStatoIngressiMessage(int[] message) {
		load(message);
	}

	public RichiestaStatoIngressiMessage(String address) {
		this((new Integer(address)).intValue());
	}

	public boolean isAnsweredBy(FXPXTMessage m) {
		if (FXPXTResponseMessage.class.isInstance(m)
				&& m.getMessageType() == RISPOSTA_STATO_INGRESSO
				&& ((FXPXTResponseMessage) m).getSource().equals(getDestination())) {
			return true;
		} else {
			return false;
		}
	}

}
