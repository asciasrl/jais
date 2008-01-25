/**
 * Copyright (C) 2008 ASCIA S.R.L.
 */
package it.ascia.eds.msg;

import java.util.Random;

/**
 * Comando broadcast.
 * 
 * Questo messaggio attiva le uscite configurate per rispondere a comandi
 * broadcast.
 * 
 * @author sergio, arrigo
 */
public class ComandoBroadcastMessage extends BroadcastMessage
	implements MessageInterface {

	/**
	 * Costruttore
	 * 
	 * @param Numero Numero comando broadcast
	 * @param Attivazione Attivare o disattivare
	 * @param Modalita Modalit� di funzionamento (1=MODALITA� RISPARMIO DIMMER)
	 * @throws Exception
	 */
	public ComandoBroadcastMessage(int Numero, boolean Attivazione, int Modalita)
	  throws Exception {
		Random r = new Random();
		Destinatario = r.nextInt() & 0xFF;
		Mittente = r.nextInt() & 0xFF;
		TipoMessaggio = Message.MSG_COMANDO_BROADCAST;
		Byte1 = (Attivazione ? 0 : 1) & 0x01 + ((Modalita & 0x7F) << 1); 
		Byte2 = Numero & 0x1F;
	}

	public ComandoBroadcastMessage(int[] message) {
		parseMessage(message);
	}

	public String getTipoMessaggio() {
		return "Messaggio Broadcast";
	}
	
	/**
	 * Ritorna true se e' un comando di attivazione.
	 */
	public boolean isActivation() {
		return ((Byte1 & 0x01) == 0);
	}
	
	/**
	 * Ritorna il numero del comando broadcast.
	 */
	public int getCommandNumber() {
		return (Byte2 & 0x1F);
	}
	
	public String getInformazioni()	{
		StringBuffer s = new StringBuffer();
		s.append("Timestamp: "+((Mittente & 0xFF) * 0x100 + (Destinatario & 0xFF)) +"\r\n");
		if (isActivation()) {
			s.append("Attivazione/Incremento\r\n");
		} else {
			s.append("Disattivazione/Decremento\r\n");
		}
		s.append("Numero comando: "+ getCommandNumber() +"\r\n");
		s.append("Modalita: "+((Byte1 >> 1) & 0x7F)+"\r\n");
		return s.toString();
	}

	public int getMessageType() {
		return MSG_COMANDO_BROADCAST;
	}
}
