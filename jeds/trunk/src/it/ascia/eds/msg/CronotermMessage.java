/**
 * Copyright (C) 2007 ASCIA S.R.L.
 */
package it.ascia.eds.msg;

import it.ascia.ais.MessageInterface;

/**
 * Lettura del set point del cronotermostato.
 * 
 * Questo messaggio viene inviato dal cronotermostato quando il set point viene
 * cambiato (dall'utente o automaticamente), o come risposta a un
 * RichiestaSetPointMessage.
 * 
 * Codice EDS: 205.
 * 
 * @author sergio, arrigo
 * 
 * @see RichiestaSetPointMessage
 */
public class CronotermMessage extends PTPMessage implements MessageInterface {

	/*
	public CronotermMessage(int d, int m, int Attivazione, int Uscita, int Variazione)
	  throws Exception {
		Destinatario = d & 0xFF;
		Mittente = m & 0xFF;
		TipoMessaggio = 205;
		Byte1 = Uscita & 0x07 + ((Attivazione & 0x01) << 3);
		Byte2 = Variazione & 0x01;
	}
	 */
	
	public CronotermMessage(int[] message) {
		load(message);
	}

	public String getMessageDescription() {
		return "Set point cronotermostato";
	}
	
	/**
	 * Ritorna la temperatura di set point.
	 */
	public double getSetPoint() {
		return (Byte1 & 0xFF) + (Byte2 & 0x0F) / 10.0;
	}
	
	/**
	 * Ritorna true se il set-point e' impostato per l'inverno.
	 */
	public boolean isWinter() {
		return ((Byte2 & 0x80) > 0);
	}
	
	public String toString()	{
		StringBuffer s = new StringBuffer();
		s.append(super.toString());
		s.append(isWinter() ? " Inverno "  : " Estate ");
		s.append("Tset="+ getSetPoint() +"°C");
		return s.toString();
	}

	public int getMessageType() {
		return MSG_LETTURA_SET_POINT;
	}
}
