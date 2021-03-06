/**
 * Copyright (C) 2008 ASCIA S.R.L.
 */
package it.ascia.eds.msg;

import java.security.InvalidParameterException;

/**
 * Impostazione valore del set point.
 * 
 * Questo messaggio puo' essere inviato a un crono-termostato per cambiarne il
 * set point.
 * 
 * Lo stesso codice di messaggio viene utilizzato sia dal cronotermostato Home Innovation che da quello di World Data Bus, ma con un formato diverso
 * 
 * Codice EDS 202.
 */
public class ImpostaSetPointMessage extends PTPCommand {

	/**
	 * Elenco giorni della settimana 
	 */
	public static final String[] giorni = new String[] {"Dom","Lun","Mar","Mer","Gio","Ven","Sab"};

	/**
	 * Elenco stagioni 
	 */
	public static final String[] stagioni = new String[] {"Inverno","Estate"};

	/**
	 * Imposta la temperatura del Cronotermostato WDB
	 * @param d Destinatario
	 * @param m Mittente
	 * @param temperatura 5-60 gradi centigradi
	 * @param decimale 0=temperatura,0  1=temperatura,5 
	 * @param stagione
	 * @param ora
	 * @param giorno
	 */
	public ImpostaSetPointMessage(int d, int m, int temperatura, int decimale, int stagione, int giorno, int ora) {
		Destinatario = d & 0xFF;
		Mittente = m & 0xFF;
		TipoMessaggio = getMessageType();
		Byte1 = (temperatura & 0x3F) + ((stagione & 0x01) << 6) + ((decimale & 0x01) << 7);
		Byte2 = (giorno & 0x07) + ((ora & 0x1F) << 3);
	}
	
	/**
	 * Imposta la temperatura del Cronotermostato Home Innovation
	 * @param d Destinatario
	 * @param m Mittente
	 * @param t Temperatura con 1 decimale
	 */
	public ImpostaSetPointMessage(int d, int m, double t) {
		Destinatario = d & 0xFF;
		Mittente = m & 0xFF;
		TipoMessaggio = MSG_IMPOSTA_SET_POINT;
		int decimali = (new Double(t*10)).intValue() - (new Double(t)).intValue() * 10; 
		Byte1 = (new Double(t*10)).intValue() & 0xFF;
		Byte2 = decimali & 0x0F; 
	}


	/**
	 * Imposta il setPoint manuale per uno specifico tempo
	 * @param d Destinatario
	 * @param m Mittente
	 * @param temperatura 5-60 gradi centigradi
	 * @param decimale 0=temperatura,0  1=temperatura,5 
	 * @param durata Ore
	 */
	public ImpostaSetPointMessage(int d, int m, int temperatura, int decimale, int durata) {
		this(d, m, temperatura, decimale, 0, durata, 31);
	}

	public ImpostaSetPointMessage(int d, int m,
			double temperatura, String sStagione, String sGiorno, int ora) {
		this(d,m,temperatura,stagione(sStagione),giorno(sGiorno),ora);
	}

	/**
	 * Imposta manualmente il setpoint corrente
	 * @param d
	 * @param m
	 * @param temperatura
	 * @param durata
	 */
	public ImpostaSetPointMessage(int d, int m,
			double temperatura, int durata) {
		this(d,m,temperatura,0,durata,31);
	}

	public ImpostaSetPointMessage(int d, int m, 
			double temperatura,	int stagione, int giorno, int ora) {
		this(d,m,temperatura(temperatura),decimale(temperatura),stagione,giorno,ora);
	}

	public ImpostaSetPointMessage(int[] message) {
		load(message);
	}
	
	public static int stagione(String stagione) {
		for ( int n = 0; n < stagioni.length; n++ ) {
			if ( stagioni[n].equals(stagione)) {
				return n;
			}
		}
		throw(new InvalidParameterException("Stagione non valida: "+stagione));
	}
	
	public static int giorno(String giorno) {
		for ( int n = 0; n < giorni.length; n++ ) {
			if ( giorni[n].equals(giorno)) {
				return n;
			}
		}
		throw(new InvalidParameterException("Giorno non valido: "+giorno));
	}

	/**
	 * Ritorna la temperatura di set point indicata nel messaggio.
	 */
	public double getSetPoint() {
		return temperatura(Byte1 & 0x3F,(Byte1 & 0x80) >> 7);
	}

	public static int temperatura(double t) {
		return (new Double(t)).intValue();
	}

	public static int decimale(double t) {
		int decimali = (new Double(t*10)).intValue() - (new Double(t)).intValue() * 10; 
		return decimali >= 5 ? 1 : 0;
	}
	
	public static double temperatura(int t, int d) {
		return (double)t + (double)d * 0.5;
	}

	public int getOra() {
		return (Byte2 & 0xF8) >> 3;
	}

	public static String ora(int ora) {
		return (ora < 10 ? "0" : "") + ora;
	}

	public static String fasciaOraria(int ora) {
		if (ora <= 23 && ora >= 0) {
			return ora + ":00-"+ora+":59";
		} else if (ora == 31){
			return "manuale";
		} else {
			throw(new InvalidParameterException("Ora non valida: "+ora));
		}
	}

	public int getGiorno() {
		return Byte2 & 0x07;
	}

	public static String giorno(int giorno) {
		return giorni[giorno];
	}
	
	public int getStagione() {
		return (Byte1 & 0x40) >> 6;
	}

	public static String stagione(int stagione) {
		try {
			return stagioni[stagione];
		} catch(IndexOutOfBoundsException e) {
			return null;
		}
	}
	

	public String getMessageDescription() {
		return "Imposta set point cronotermostato";
	}

	public String toString()	{
		StringBuffer s = new StringBuffer();
		s.append(super.toString());
		if (getOra() == 31) {
			s.append(" Manuale per "+getGiorno()+" ore");
		} else {
			s.append(" Stagione:"+stagione(getStagione()));
			s.append(" Giorno:"+giorno(getGiorno()));
			s.append(" Orario:"+fasciaOraria(getOra()));
		}
		s.append(" Temperatura: " + getSetPoint()+" gradi C");
		return s.toString();
	}

	public int getMessageType() {
		return MSG_IMPOSTA_SET_POINT;
	}
	
}
