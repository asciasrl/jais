package it.ascia.duemmegi.fxpxt.msg;

import it.ascia.duemmegi.fxpxt.FXPXTResponseMessage;

public class RispostaStatoIngressiMessage extends FXPXTResponseMessage {

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

	public boolean getInputStatus(int i) {
		if (i <1 || i > 8) {
			throw(new IndexOutOfBoundsException("Porta di ingresso deve essere fra 1 e 8"));
		}
		int mask = 0x01 << (i - 1);
		return (dato0 & mask) > 0;
	}

}
