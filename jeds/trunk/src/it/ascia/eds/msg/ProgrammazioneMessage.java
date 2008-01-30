/**
 * Copyright (C) 2008 ASCIA S.R.L.
 */
package it.ascia.eds.msg;

import java.util.Random;

/**
 * Comando broadcast di inizio o chiusura programmazione.
 * 
 * @author sergio, arrigo
 */
public class ProgrammazioneMessage extends BroadcastMessage
	implements MessageInterface {
	/**
	 * Non cambia protocollo.
	 */
	public static final int PROTOCOL_PREVIOUS = 0;
	/**
	 * Protocollo V1.
	 */
	public static final int PROTOCOL_V1 = 2;
	/**
	 * Protocollo V2.
	 */
	public static final int PROTOCOL_V2 = 3;
	
	/**
	 * Costruttore.
	 * 
	 * @param apertura true per aprire la programmazione, false per chiuderla
	 * @param protocollo (vedi le costanti statiche di questa classe)
	 */
	public ProgrammazioneMessage(boolean apertura, int protocollo)
	  throws Exception {
		Random r = new Random();
		Destinatario = r.nextInt() & 0xFF;
		Mittente = r.nextInt() & 0xFF;
		TipoMessaggio = getMessageType();
		Byte1 = (apertura ? 1 : 0); 
		Byte2 = protocollo;
	}

	public ProgrammazioneMessage(int[] message) {
		parseMessage(message);
	}

	public String getTipoMessaggio() {
		return "Messaggio di apertura/chiusura programmazione";
	}
	
	/**
	 * Ritorna true se e' un comando di apertura programmazione.
	 */
	public boolean isStarting() {
		return ((Byte1 & 0x01) == 0);
	}
	
	/**
	 * Ritorna il numero del protocollo.
	 */
	public int getProtocol() {
		return Byte2;
	}
	
	public String getInformazioni()	{
		StringBuffer s = new StringBuffer();
		s.append("Timestamp: "+((Mittente & 0xFF) * 0x100 + (Destinatario & 0xFF)) +"\r\n");
		if (isStarting()) {
			s.append("Apertura programmazione\r\n");
		} else {
			s.append("Chiusura programmazione\r\n");
		}
		s.append("Protocollo: "+ getProtocol() +"\r\n");
		return s.toString();
	}

	public int getMessageType() {
		return MSG_PROGRAMMAZIONE;
	}
}
