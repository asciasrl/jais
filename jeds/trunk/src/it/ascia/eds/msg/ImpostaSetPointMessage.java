/**
 * Copyright (C) 2008 ASCIA S.R.L.
 */
package it.ascia.eds.msg;

/**
 * Impostazione valore del set point.
 * 
 * Questo messaggio puo' essere inviato a un crono-termostato per cambiarne il
 * set point.
 * 
 * Codice EDS 202.
 */
public class ImpostaSetPointMessage extends PTPRequest 
	implements MessageInterface {

	public ImpostaSetPointMessage(int d, int m, double temperatura) {
		Destinatario = d & 0xFF;
		Mittente = m & 0xFF;
		TipoMessaggio = getMessageType();
		Byte1 = (int)temperatura;
		Byte2 = ((int)((temperatura - Byte1) * 16)) & 0x0f;
	}
	
	public ImpostaSetPointMessage(int[] message) {
		parseMessage(message);
	}
	
	/**
	 * Ritorna la temperatura di set point indicata nel messaggio.
	 */
	public double getSetPoint() {
		return Byte1 + (Byte2 & 0x0f) / 16.0;
	}

	public String getTipoMessaggio() {
		return "Impostazione set point cronotermostato";
	}

	public String getInformazioni()	{
		StringBuffer s = new StringBuffer();
		s.append("Mittente: "+Mittente+"\r\n");
		s.append("Destinatario: "+Destinatario+"\r\n");
		s.append("Temperatura: " + (Byte2 / 16.0) + Byte1 + "\r\n");
		return s.toString();
	}

	public int getMessageType() {
		return MSG_IMPOSTA_SET_POINT;
	}
	
}
