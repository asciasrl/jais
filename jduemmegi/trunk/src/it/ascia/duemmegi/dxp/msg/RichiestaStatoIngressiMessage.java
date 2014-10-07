package it.ascia.duemmegi.dxp.msg;

import it.ascia.duemmegi.dxp.DXPMessage;
import it.ascia.duemmegi.dxp.DXPRequestMessage;
import it.ascia.duemmegi.dxp.DXPResponseMessage;

public class RichiestaStatoIngressiMessage extends DXPRequestMessage {

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

	public boolean isAnsweredBy(DXPMessage m) {
		if (DXPResponseMessage.class.isInstance(m)
				&& m.getMessageType() == RISPOSTA_STATO_INGRESSO
				&& ((DXPResponseMessage) m).getSource().equals(getDestination())) {
			return true;
		} else {
			return false;
		}
	}

}
