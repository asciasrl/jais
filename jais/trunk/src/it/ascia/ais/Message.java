package it.ascia.ais;

public abstract class Message {

	public String getMessageDescription() {
		return getClass().getSimpleName();
	}
	
	public abstract String toString();
	
	/**
	 *  TODO rivedere Message
	 *  Eventuali metodi da aggiungere:
	 *  - getSource
	 *  - getDestination
	 *  - getValue
	 */
	
	/**
	 * Ritorna il contenuto del messaggio sotto forma di array di byte.
	 */
	public abstract byte[] getBytesMessage();

	/**
	 * Ritorna il codice del tipo di messaggio.
	 */
	public abstract int getMessageType();
	
	public abstract String getSource();
	
	public abstract String getDestination();
	
	/**
	 * Funzione di utilita': restituisce la rappresentazione esadecimale
	 * di un byte con 2 cifre e preceduta da "0x"
	 */
	public static String b2h(int i)
	{
		String s = "0x";
		if (i < 16) {
			s += "0";
		}
		s += Integer.toHexString(i);
		return s;
	}

	/**
	 * Funzione di utilita': restituisce la rappresentazione binaria
	 * di un byte con esattamente 8 bit
	 */
	public static String b2b(int i)
	{
		StringBuffer s = new StringBuffer();
		for (int j = 0; j < 8; j++) {
			s.append((i >> j) & 1);
		}
		return s.reverse().toString();
	}


}