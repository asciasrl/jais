package it.ascia.dxp.msg;

import it.ascia.dxp.DXPMessage;

public class RichiestaStatoIngressiMessage extends DXPMessage {

	public RichiestaStatoIngressiMessage(int ind) {
		indirizzo = ind & 0xFF;
		tipo = DXPMessage.RICHIESTA_STATO_INGRESSO;
		dato1 = 0x33;
		dato0 = 0x33;			
	}
	
	public RichiestaStatoIngressiMessage(int[] message) {
		load(message);
	}

	public String getDestination() {
		return (new Integer(indirizzo)).toString();
	}

	public String getSource() {
		return null;
	}

}
