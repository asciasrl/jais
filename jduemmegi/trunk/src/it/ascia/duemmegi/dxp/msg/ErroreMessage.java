package it.ascia.duemmegi.dxp.msg;

import it.ascia.duemmegi.dxp.DXPMessage;

public class ErroreMessage extends DXPMessage {

	public ErroreMessage(int[] message) {
		load(message);
	}

	public String getDestination() {
		return null;
	}

	public String getSource() {
		return null;
	}

	public String toString() {
		StringBuffer s = new StringBuffer();
		s.append(super.toString());
		switch (dato0) {
			case 0xF0:
				s.append(" Modulo guasto o scollegato");
				break;
			case 0xFF:
				s.append(" Errore di trasmissione");
				break;
			default:
				s.append(" Errore sconosciuto:"+b2h(dato0));
		}
		return s.toString();
	}
	
}
