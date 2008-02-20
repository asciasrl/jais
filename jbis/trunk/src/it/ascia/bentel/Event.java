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
	 * Timestamp, così come scritto nell'evento, ma riordinato.
	 * 
	 * <p>I byte sono, nell'ordine: anno - 2000, mese, giorno, ore, minuti.</p> 
	 */
	private byte[] timestamp;
	
	/**
	 * Costruttore a partire dai dati di log.
	 * 
	 * @param logData log (dati grezzi) 
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
		return getTimestamp() + "  " + getNumber();
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
