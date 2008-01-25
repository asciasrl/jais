package it.ascia.eds.msg;

public class ImpostaParametroDimmerMessage extends PTPRequest
	implements MessageInterface {
	
	/**
	 * 54
	 */
	public static final int TIPO = 54;

/*	public ImpostaParametroDimmerMessage(int d, int m, int Tempo, int Uscita, int Percentuale, int Attivazione) {
		Destinatario = d & 0xFF;
		Mittente = m & 0xFF;
		TipoMessaggio = 54;
		//Byte1 = (Uscita & 0x07) + ((Tempo & 0x0F) << 3);
		//Byte2 = (Attivazione & 0x01) + ((Percentuale & 0x7F) << 1);
	}
*/	
	public ImpostaParametroDimmerMessage(int[] message) {
		parseMessage(message);
	}

	public String getTipoMessaggio() {
		return "Impostazione dimmer";
	}

	public String getInformazioni()	{
		StringBuffer s = new StringBuffer();
		s.append("Mittente: "+Mittente+"\r\n");
		s.append("Destinatario: "+Destinatario+"\r\n");
		switch (Byte1) {
			case 1:
				s.append("Soft time: "+Byte2+"\r\n");
				break;
			case 3:
				s.append("Ritardo variazione: "+Byte2+"\r\n");
				break;
			case 11:
				s.append("Minimo: "+Byte2+"\r\n");
				break;
			case 12:
				s.append("Massimo: "+Byte2+"\r\n");
				break;
			case 22:
				s.append("Sensibilita: "+Byte2+"\r\n");
				break;
			default:
				break;
		}
		return s.toString();
	}

	public boolean isAnsweredBy(PTPMessage m) {
		System.err.println("ImpostaParametroDimmerMessage.isAnsweredBy(): " +
				"non implementato.");
		if (m.getMessageType() == Message.MSG_ACKNOWLEDGE) {
			if ((getSender() == m.getRecipient()) &&
					(getRecipient() == m.getSender())) {
				answered = true;
			}
		}
		return answered;
	}

	public int getMessageType() {
		return MSG_IMPOSTA_PARAMETRO_DIMMER;
	}
	
}
