/**
 * Copyright (C) 2008 ASCIA S.r.l.
 */
package it.ascia.bentel;

/**
 * Un evento letto dai log.
 * 
 * @author arrigo
 */
public class Event {
	/**
	 * Lunghezza del timestamp in byte.
	 */
	public static final int TIMESTAMP_LENGTH = 5;
	/**
	 * Numero dell'evento.
	 */
	private int number;
	/**
	 * Timestamp, cos� come scritto nell'evento, ma riordinato.
	 * 
	 * <p>I byte sono, nell'ordine: anno - 2000, mese, giorno, ore, minuti.</p> 
	 */
	private byte[] timestamp;
	
	/**
	 * Ritorna una rappresentazione testuale del numero di evento indicato.
	 * 
	 * @param n numero dell'evento.
	 */
	private static String numberToString(int n) {
		if (n < 0) {
			return "Numero non valido (" + n + ")";
		} else if (n <= 3) {
			return "Allarme area " + (n + 1);
		} else if (n <= 11) {
			return "Allarme zona " + (n - 3);
		} else if (n <= 15) {
			return "Inattivita' area " + (n - 11);
		} else if (n <= 19) {
			return "Negligenza area " + (n - 15);
		} else if (n <= 27) {
			return "Esclusione zona " + (n - 19);
		} else if (n <= 35) {
			return "Reinclusione zona " + (n - 27);
		} else if (n <= 59) {
			return "Riconosciuto codice " + (n - 35);
		} else if (n <= 187) {
			return "Riconosciuta chiave " + (n - 59);
		} else if (n <= 195) {
			return "Auto esclusione zona " + (n - 187);
		} else if (n <= 199) {
			return "Inserimento area " + (n - 195);
		} else if (n <= 203) {
			return "Disinserimento area " + (n - 199);
		} else if (n <= 207) {
			return "Richiesta inserimento area " + (n - 203);
		} else if (n <= 211) {
			return "Richiesta disinserimento area " + (n - 207);
		} else if (n <= 215) {
			return "Reset memoria area " + (n - 211);
		} else if (n <= 219) {
			return "Disinserimento sotto costrizione area " + (n - 215);
		} else if (n <= 227) {
			return "Chiamata fallita " + (n - 219);
		} else if (n <= 235) {
			return "Sabotaggio zona " + (n - 227);
		} else if (n <= 243) {
			return "Ripristino zona " + (n - 235);
		}
		// Questi sono trovati per confronto con il software ufficiale
		switch (n) {
		case 246:
			return "Sabotaggio sistema";
		case 254:
			return "Guasto linea telefonica";
		case 260:
			return "Ripristino linea telefonica";
		case 267:
			return "Perdita datario";
		case 32896:
			return "Programmazione";
		}
		return "Sconosciuto (" + n + ")";
	}
	
	/**
	 * Costruttore a partire dai dati di log.
	 * 
	 * @param logData evento ricevuto dalla centralina (dati grezzi) 
	 * @param address indirizzo del primo byte dentro data che descrive l'evento
	 */
	public Event(byte []logData, int address) {
		int tsAddress = address + 2;
		number = ((logData[address] & 0xff) << 8) + 
			(logData[address + 1] & 0xff);
		timestamp = new byte[TIMESTAMP_LENGTH];
		timestamp[0] = logData[tsAddress + 2]; // anno
		timestamp[1] = logData[tsAddress + 1]; // mese
		timestamp[2] = logData[tsAddress]; // giorno
		timestamp[3] = logData[tsAddress + 3]; // ore
		timestamp[4] = logData[tsAddress + 4]; // minuti
	}
	
	/**
	 * Ritorna il numero dell'evento.
	 * 
	 * @return il numero dell'evento.
	 */
	public int getNumber() {
		return number;
	}
	
	/**
	 * Verifica se l'evento e' un allarme.
	 * 
	 * @return true se l'evento e' un allarme.
	 */
	public boolean isAlarm() {
		return (getNumber() <= 11);
	}
	
	/**
	 * Ritorna una descrizione dell'allarme.
	 * 
	 * @return una descrizione dell'allarme.
	 */
	public String getAlarm() {
		int n = getNumber();
		if (n <= 3) {
			return "area " + (n + 1);
		} else if (n <= 11) {
			return "zona " + (n - 3);
		}
		return "questo non e' un allarme!";
	}
	
	/**
	 * Ritorna il timestamp, cosi' come specificato nel messaggio, 
	 * ma riordinato.
	 * 
	 * <p>ATTENZIONE: ritorna la stessa array contenuta all'interno 
	 * dell'oggetto!</p>
	 * 
	 * @return un'array di 5 byte. Il piu' significativo e' l'anno, il meno
	 * sono i minuti.
	 */
	public byte[] getRawTimestamp() {
		return timestamp;
	}
	
	/**
	 * Ritorna il timestamp sotto forma di stringa.
	 */
	public String getTimestamp() {
		String retval;
		retval = (timestamp[0] + 2000) + "-";
		if (timestamp[1] < 10) retval += "0";
		retval += timestamp[1] + "-";
		if (timestamp[2] < 10) retval += "0";
		retval += timestamp[2] + " ";
		if (timestamp[3] < 10) retval += "0";
		retval += timestamp[3] + ":";
		if (timestamp[4] < 10) retval += "0";
		retval += timestamp[4];
		return retval;
	}
	
	/**
	 * Ritorna una descrizione dell'evento in forma testuale.
	 */
	public String getInfo() {
		return getTimestamp() + "  " + numberToString(getNumber());
	}

	/**
	 * Confronta il timestamp di questo evento con quello di un altro.
	 *
	 * @param arg0 messaggio con cui fare il confronto.
	 * 
	 * @return 1 se questo messaggio e' piu' recente di arg0; 0 se ha lo stesso 
	 * timestamp; -1 se questo messaggio e' piu' vecchio di arg0.
	 */
	public int compareTimestamp(Event arg0) {
		byte otherTimestamp[] = arg0.getRawTimestamp();
		for (int i = 0; i < TIMESTAMP_LENGTH; i++) {
			if (timestamp[i] > otherTimestamp[i]) {
				return 1;
			} else if (timestamp[i] < otherTimestamp[i]) {
				return -1;
			}
		}
		return 0;
	}
}
