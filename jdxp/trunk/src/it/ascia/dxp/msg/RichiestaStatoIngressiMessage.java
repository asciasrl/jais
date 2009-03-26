package it.ascia.dxp.msg;

import it.ascia.dxp.DXPMessage;
import it.ascia.dxp.DXPRequestMessage;
import it.ascia.dxp.DXPResponseMessage;

public class RichiestaStatoIngressiMessage extends DXPRequestMessage {

	public RichiestaStatoIngressiMessage(int ind) {
		indirizzo = ind & 0xFF;
		tipo = RICHIESTA_STATO_INGRESSO;
		dato1 = 0x33;
		dato0 = 0x33;			
	}
	
	public RichiestaStatoIngressiMessage(int[] message) {
		load(message);
	}

	public boolean isAnsweredBy(DXPMessage m) {
		if (DXPResponseMessage.class.isInstance(m)
				&& m.getMessageType() == RISPOSTA_STATO_INGRESSO
				&& m.getSource().equals(getDestination())) {
			return true;
		} else {
			return false;
		}
	}

}
