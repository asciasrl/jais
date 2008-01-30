/**
 * Copyright (C) 2008 ASCIA S.R.L.
 */
package it.ascia.eds.msg;

/**
 * Lettura dello stato del cronotermostato.
 *
 * Questo messaggio viene mandato dal cronotermostato quando cambia qualcosa,
 * oppure quando viene richiesto un aggiornamento dello stato attraverso l'invio
 * di un messaggio RichiestaStatoTermostatoMessage.
 * 
 * Codice EDS: 201.
 */
public class TemperatureMessage	extends PTPMessage implements MessageInterface {
	/**
	 * Modalita' antigelo.
	 */
	public static final int MODE_ANTI_FREEZE = 0;
	/**
	 * Modalita' crono.
	 */
	public static final int MODE_CHRONO = 1;
	/**
	 * Modalita' manuale.
	 */
	public static final int MODE_MANUAL = 2;
	/**
	 * Modalita' temporizzato.
	 */
	public static final int MODE_TIME = 3;

	public TemperatureMessage(int[] message) {
		parseMessage(message);
	}

	public String getTipoMessaggio() {
		return "Lettura dello stato del cronotermostato";
	}
	
	/**
	 * Ritorna la temperatura.
	 */
	public double getTemperature() {
		return (Byte1 & 0xff) + ((Byte2 & 0xF0) >> 4) / 10.0;
	}
	
	/**
	 * Ritorna true se e' abilitato l'allarme minima temperatura.
	 */
	public boolean getAlarm() {
		return ((Byte2 & 0x08) != 0); 
	}
	
	/**
	 * Ritorna true se la temperatura e' superiore al set-point.
	 */
	public boolean tempOverSetPoint() {
		return ((Byte2 & 0x04) != 0); 
	}
	
	/**
	 * Ritorna la modalita' di funzionamento.
	 * 
	 * Questo numero deve essere confrontato con gli attributi statici MODE_*.
	 */
	public int getMode() {
		return (Byte2 & 0x03);
	}
	
	public String getInformazioni()	{
		StringBuffer s = new StringBuffer();
		s.append("Mittente: "+Mittente+"\r\n");
		s.append("Destinatario: "+Destinatario+"\r\n");
		s.append("Allarme minima temp.: " + getAlarm() + "\r\n");
		s.append("Temperatura: "+ getTemperature());
		if (tempOverSetPoint()) {
			s.append(" sopra");
		} else {
			s.append(" sotto");
		}
		s.append(" al set point\r\n");
		s.append("Modalita': " + getMode() + "\r\n");
		return s.toString();
	}

	public int getMessageType() {
		return MSG_TEMPERATURA;
	}
}
