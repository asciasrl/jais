package it.ascia.dxp.msg;

import it.ascia.dxp.DXPMessage;

public class RispostaStatoIngressiMessage extends DXPMessage {

	public RispostaStatoIngressiMessage(int[] message) {
		load(message);
	}

	public String getDestination() {
		return null;
	}

	public String getSource() {
		return (new Integer(indirizzo)).toString();
	}

	public String toString() {
		StringBuffer s = new StringBuffer();
		s.append(super.toString());
		s.append(" Digitali:" + b2b(dato0));
		s.append(" Analogici:" + getShort());
		return s.toString();
	}

}
