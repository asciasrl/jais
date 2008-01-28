/**
 * Copyright (C) 2008 ASCIA S.R.L.
 */
package it.ascia.eds.msg;

/**
 * Messaggio di "Acknowledge".
 * 
 * Questo messaggio viene mandato da alcuni BMC per confermare che un comando
 * e' stato ricevuto.
 * 
 * I campi Byte1 e Byte2 sono uguali a quelli del messaggio a cui si risponde.
 */
public class AcknowledgeMessage extends PTPMessage {

	public AcknowledgeMessage(byte d, byte m, byte b1, byte b2) {
		Destinatario = d;
		Mittente = m;
		TipoMessaggio = getMessageType();
		Byte1 = b1;
		Byte2 = b2;
	}

	public AcknowledgeMessage(int[] message) {
		parseMessage(message);
	}

	public String getTipoMessaggio() {
		return "Aknowledge";
	}

	public int getMessageType() {
		return MSG_ACKNOWLEDGE;
	}

	/**
	 * Confronta i campi Byte1 e Byte2 con i valori specificati.
	 */
	public boolean hasBytes(int byte1, int byte2) {
		return ((Byte1 == byte1) && (Byte2 == byte2));
	}

}
