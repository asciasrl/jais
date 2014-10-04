package it.ascia.dxp.msg;

import it.ascia.dxp.DXPMessage;
import it.ascia.dxp.DXPRequestMessage;
import it.ascia.dxp.DXPResponseMessage;

public class RichiestaStatoUsciteMessage extends DXPRequestMessage {

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

	public boolean isAnsweredBy(DXPMessage m) {
		if (DXPResponseMessage.class.isInstance(m)
				&& m.getMessageType() == RISPOSTA_STATO_USCITE
				&& ((DXPResponseMessage) m).getSource().equals(getDestination())) {
			return true;
		} else {
			return false;
		}
	}

}
