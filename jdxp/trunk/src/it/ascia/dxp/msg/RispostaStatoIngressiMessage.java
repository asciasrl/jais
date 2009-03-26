package it.ascia.dxp.msg;

import it.ascia.dxp.DXPResponseMessage;

public class RispostaStatoIngressiMessage extends DXPResponseMessage {

	public RispostaStatoIngressiMessage(int[] message) {
		load(message);
	}

	public String toString() {
		StringBuffer s = new StringBuffer();
		s.append(super.toString());
		s.append(" Digitali:" + b2b(dato0));
		s.append(" Analogici:" + getShort());
		return s.toString();
	}

}
