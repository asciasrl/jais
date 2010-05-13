package it.ascia.ais;

public abstract class Message implements Comparable {
	
	protected int priority = 0;
	
	private boolean isSent = false;

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
	 * Funzione di utilita': restituisce la rappresentazione esadecimale
	 * di un byte con 2 cifre e preceduta da "0x"
	 */
	public static String b2h(int i)
	{
		i = i & 0xFF;
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
	
	/**
	 * 
	 * @return
	 * @deprecated Never used
	 */
	public int getPriority() {
		return priority;
	}

	/**
	 * 
	 * @param priority
	 * @deprecated Never used
	 */
	public void setPriority(int priority) {
		this.priority = priority;
	}

	public int compareTo(Object o) {
		Message m = (Message) o;
		return this.priority - m.priority;
	}

	/**
	 * Connector MUST call this method when sending the message
	 * @param isSent is set to true
	 */
	public void setSent() {
		isSent = true;
	}

	/**
	 * @return the isSent
	 */
	public boolean isSent() {
		return isSent;
	}


}