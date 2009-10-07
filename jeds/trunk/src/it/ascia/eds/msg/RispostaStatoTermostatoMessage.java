/**
 * Copyright (C) 2008 ASCIA S.R.L.
 */
package it.ascia.eds.msg;

/**
 * Lettura dello stato del cronotermostato o della sonda termica.
 *
 * <p>Questo messaggio viene mandato dal cronotermostato o dalla sonda termica
 * quando cambia qualcosa, oppure quando viene richiesto un aggiornamento dello 
 * stato attraverso l'invio di un messaggio RichiestaStatoTermostatoMessage.</p>
 * 
 * <p>Codice EDS: 201.</p>
 */
public class RispostaStatoTermostatoMessage	extends PTPResponse {
	/**
	 * Modalita' antigelo (cronotermostato).
	 */
	public static final int MODE_ANTI_FREEZE = 0;
	/**
	 * Modalita' OFF (sonda termica).
	 */
	public static final int MODE_OFF = 0;
	/**
	 * Modalita' crono.
	 */
	public static final int MODE_CHRONO = 1;
	/**
	 * Modalita' manuale.
	 */
	public static final int MODE_MANUAL = 2;
	/**
	 * Modalita' temporizzato (cronotermostato).
	 */
	public static final int MODE_TIME = 3;

	public RispostaStatoTermostatoMessage(int[] message) {
		load(message);
	}

	public String getMessageDescription() {
		return "Risposta stato termostato";
	}
	
	/**
	 * Ritorna la temperatura del cronotermostato.
	 */
	public double getChronoTermTemperature() {
		return (Byte1 & 0xff) + ((Byte2 & 0xF0) >> 4) / 10.0;
	}
	
	/**
	 * Ritorna la temperatura della sonda termica.
	 */
	public double getSensorTemperature() {
		double temp = (Byte1 & 0x7f) + ((Byte2 & 0xF0) >> 4) / 10.0;
		if ((Byte1 & 0x80) == 0) {
			return temp;
		} else {
			return -temp;
		}
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
	public boolean isOn() {
		return ((Byte2 & 0x04) == 1); 
	}
	
	/**
	 * Ritorna la modalita' di funzionamento.
	 * 
	 * Questo numero deve essere confrontato con gli attributi statici MODE_*.
	 */
	public int getMode() {
		return (Byte2 & 0x03);
	}
	
	public String getModeDescription() {
		switch (getMode()) {
			case 0:
				return "Antigelo";
			case 1:
				return "Chrono";
			case 2:
				return "Manuale";
			case 3:
				return "Cronotermostato";
			default:
				return "modo sconosciuto";
		}	
	}
	
	public String toString()	{
		StringBuffer s = new StringBuffer();
		s.append(super.toString());
		s.append(getAlarm() ? " Antigelo":"");
		s.append("T="+ getChronoTermTemperature()+"\u00B0C");
		s.append(isOn()?" ON ":" OFF ");
		s.append(getModeDescription());
		s.append(" allarme:"+(getAlarm() ? "ON" : "OFF"));
		return s.toString();
	}

	public int getMessageType() {
		return MSG_RISPOSTA_STATO_TERMOSTATO;
	}
}
