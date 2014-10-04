package it.ascia.eds.msg;

public class ImpostaRTCCMessage extends PTPCommand {

	public String getMessageDescription() {
		return "Impostazione RTCC";
	}

	public int getMessageType() {
		return MSG_IMPOSTA_RTCC;
	}	

	public int getIndex() {
		return Byte1 & 0x03;
	}
	
	/**
	 * Messaggio di impostazione dati real time clock and calendar
	 * @param d Destinatario
	 * @param m Mittente
	 * @param index 0:Ora e Minuti 1:Mese e Anno 2:Giorno e Secondi
	 * @param omg Ora/Mese/Giorno
	 * @param mas Minuti/Anno/Secondi
	 */
	public ImpostaRTCCMessage(int d, int m, int index, int omg, int mas) {
		Destinatario = d & 0xFF;
		Mittente = m & 0xFF;
		TipoMessaggio = getMessageType();
		Byte1 = ((omg & 0x3F) << 2) + (index & 0x03);
		Byte2 = mas & 0xFF;				
	}

	public ImpostaRTCCMessage(int[] message) {
		load(message);
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
