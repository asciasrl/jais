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
public class ComandoBroadcastMessage extends BroadcastMessage {

	/**
	 * Costruttore
	 * 
	 * @param Numero Numero comando broadcast
	 * @param Attivazione Attivare o disattivare
	 * @param Modalita Modalit� di funzionamento (1=MODALITA� RISPARMIO DIMMER)
	 * @throws Exception
	 */
	public ComandoBroadcastMessage(int Numero, boolean Attivazione, int Modalita) {
		TipoMessaggio = EDSMessage.MSG_COMANDO_BROADCAST;
		Byte1 = (Attivazione ? 0 : 1) & 0x01 + ((Modalita & 0x7F) << 1); 
		Byte2 = Numero & 0x1F;
		Random r = new Random();
		Mittente = r.nextInt(255);
		Destinatario = r.nextInt(255);
	}
	
	public ComandoBroadcastMessage(int Numero, boolean Attivazione) {
		this(Numero,Attivazione,0);
	}

	public ComandoBroadcastMessage(int[] message) {
		load(message);
	}

	public String getMessageDescription() {
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
	
	/**
	 * @return codice pseudorandom del messaggio 
	 */
	public int getRandom() {
		return ((Mittente & 0xff) << 8) + (Destinatario & 0xff); 
	}
	
	public String toString()	{
		StringBuffer s = new StringBuffer();
		s.append(getRandom()+" "+getMessageDescription()+ " ["+b2h(Byte1)+":"+b2h(Byte2)+" "+Byte1+":"+Byte2+"]");
		s.append(" Gruppo "+ getCommandNumber());
		if (isActivation()) {
			s.append(" Attiva");
		} else {
			s.append(" Disattiva");
		}
		s.append(" Modalita: "+((Byte1 >> 1) & 0x7F));
		return s.toString();
	}

	public int getMessageType() {
		return MSG_COMANDO_BROADCAST;
	}
}
