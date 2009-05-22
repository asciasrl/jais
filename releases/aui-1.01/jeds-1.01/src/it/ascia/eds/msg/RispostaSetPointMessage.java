package it.ascia.eds.msg;

public class RispostaSetPointMessage extends PTPResponse {

	public RispostaSetPointMessage(int[] message) {
		load(message);
	}

	public int getMessageType() {
		return EDSMessage.MSG_RISPOSTA_SET_POINT;
	}

	public String getMessageDescription() {
		return "Risposta Set point cronotermostato";
	}

	public double getSetPoint() {
		return ImpostaSetPointMessage.temperatura(Byte1 & 0x3F,(Byte1 & 0x80) >> 7);
	}

	public int getOra() {
		return (Byte2 & 0xF8) >> 3;
	}

	public int getGiorno() {
		return Byte2 & 0x07;
	}
	
	public int getStagione() {
		return (Byte1 & 0x40) >> 6;
	}

	public String toString()	{
		StringBuffer s = new StringBuffer();
		s.append(super.toString());
		if (getOra() == 31) {
			s.append(" Manuale per "+getGiorno()+" ore");
		} else {
			s.append(" Stagione:"+ImpostaSetPointMessage.stagione(getStagione()));
			s.append(" Giorno:"+ImpostaSetPointMessage.giorno(getGiorno()));
			s.append(" Orario:"+ImpostaSetPointMessage.fasciaOraria(getOra()));
		}
		s.append(" Temperatura: " + getSetPoint()+" gradi C");
		return s.toString();
	}


}
