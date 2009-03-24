package it.ascia.dxp.msg;

import it.ascia.dxp.DXPMessage;

public class RichiestaStatoUsciteMessage extends DXPMessage {

	public RichiestaStatoUsciteMessage(int[] message) {
		load(message);
	}

	public RichiestaStatoUsciteMessage(int ind) {
		indirizzo = ind & 0xFF;
		tipo = DXPMessage.RICHIESTA_STATO_USCITE;
		dato1 = 0x33;
		dato0 = 0x33;			
	}

	public String getDestination() {
		return (new Integer(indirizzo)).toString();
	}

	public String getSource() {
		return null;
	}

}
