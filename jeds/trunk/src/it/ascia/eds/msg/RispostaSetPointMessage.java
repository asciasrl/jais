package it.ascia.eds.msg;

public class RispostaSetPointMessage extends CronotermMessage {

	public RispostaSetPointMessage(int[] message) {
		super(message);
	}
	
	public int getMessageType() {
		return EDSMessage.MSG_RISPOSTA_SET_POINT;
	}

	public String getMessageDescription() {
		return "Set point cronotermostato";
	}

	/**
	 * Ritorna la temperatura di set point.
	 */
	public double getSetPoint() {
		return (Byte1 & 0x7F) + ((Byte1 & 0x80) >> 7) / 2.0;
	}

}
