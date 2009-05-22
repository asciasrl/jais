package it.ascia.eds.msg;

public class RispostaRTCCMessage extends PTPResponse {

	public RispostaRTCCMessage(int[] message) {
		load(message);
	}

	public String getMessageDescription() {
		return "Risposta dati RTCC";
	}
	
	/**
	 * @return index 0:Ora e Minuti 1:Mese e Anno 2:Giorno e Secondi
	 */
	public int getIndex() {
		return Byte1 & 0x03;
	}
	
	public int getOre() {
		if (getIndex() == 0) {
			return (Byte1 >> 2) & 0x3F;
		} else {
			return -1;
		}
	}

	public int getMese() {
		if (getIndex() == 1) {
			return (Byte1 >> 2) & 0x3F;
		} else {
			return -1;
		}
	}
	
	public int getGiorno() {
		if (getIndex() == 2) {
			return (Byte1 >> 2) & 0x3F;
		} else {
			return -1;
		}
	}

	public int getMinuti() {
		if (getIndex() == 0) {
			return Byte2;
		} else {
			return -1;
		}
	}

	public int getAnno() {
		if (getIndex() == 1) {
			return Byte2;
		} else {
			return -1;
		}
	}

	public int getSecondi() {
		if (getIndex() == 2) {
			return Byte2;
		} else {
			return -1;
		}
	}

	public int getMessageType() {
		return MSG_RISPOSTA_RTCC;
	}
	
	public String toString() {
		StringBuffer s = new StringBuffer();
		s.append(super.toString());
		s.append(" index:"+getIndex());
		switch (getIndex()) {
		case 0:
			s.append(" ore:"+getOre());
			s.append(" minuti:"+getMinuti());
			break;
		case 1:
			s.append(" mese:"+getMese());
			s.append(" anno:"+getAnno());
			break;
		case 2:
			s.append(" giorno:"+getGiorno());
			s.append(" secondi:"+getSecondi());
			break;
		}
		return s.toString();
	}
	
	
	

}
