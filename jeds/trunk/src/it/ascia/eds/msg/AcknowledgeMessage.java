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

	/**
	 * Costruttore a partire dai dati di un messaggio ACK ricevuto.
	 */
	public AcknowledgeMessage(int[] message) {
		parseMessage(message);
	}
	
	/**
	 * Costruttore a partire dal messaggio a cui rispondere.
	 * 
	 * Questo costruttore crea un messaggio di risposta a m.
	 * 
	 * @param m il messaggio a cui rispondere.
	 */
	public AcknowledgeMessage(PTPMessage m) {
		int rawMessage[] = m.getRawMessage();
		Destinatario = m.getSender();
		Mittente = m.getRecipient();
		TipoMessaggio = getMessageType();
		Byte1 = rawMessage[4];
		Byte2 = rawMessage[5];
	}

	public String getTipoMessaggio() {
		return "Acknowledge";
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
