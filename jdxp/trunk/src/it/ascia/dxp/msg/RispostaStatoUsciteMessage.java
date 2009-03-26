package it.ascia.dxp.msg;

import it.ascia.dxp.DXPResponseMessage;

public class RispostaStatoUsciteMessage extends DXPResponseMessage {

	public RispostaStatoUsciteMessage(int[] message) {
		load(message);
	}

	public String toString() {
		StringBuffer s = new StringBuffer();
		s.append(super.toString());
		s.append(" Digitali:" + b2b(dato0));
		s.append(" Analogici:" + getShort());
		s.append(" Dimmer:" + dato0+"%");
		return s.toString();
	}

}
