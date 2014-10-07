package it.ascia.duemmegi.dxp;

import it.ascia.ais.Message;

public abstract class DXPMessage extends Message {

	public static final int ERRORE = 0x00;

	public static final int COMANDO_USCITE = 0x10;

	public static final int RICHIESTA_STATO_INGRESSO = 0x30;

	public static final int RICHIESTA_STATO_USCITE = 0x31;

	public static final int RISPOSTA_STATO_INGRESSO = 0xB0;

	public static final int RISPOSTA_STATO_USCITE = 0xB1;

	protected static int Start = 0x55;


	protected int funzione;
	protected int tipo;
	protected int indirizzo;
	protected int dato1;
	protected int dato0;

	/**
	 * Rappresentazione 'raw' del messaggio.
	 */
	protected int[] rawmessage;

	/**
	 * Carica i dati da un'array di interi.
	 * 
	 * ATTENZIONE: non verifica che il tipo sia coerente!
	 */
	protected void load(int[] message) {
		rawmessage = message;
		funzione = message[1];
		tipo = message[2];
		indirizzo = message[3];
		dato1 = message[4];
		dato0 = message[5];
	}

	/**
	 * Ritorna il contenuto del messaggio sotto forma di array di byte.
	 */
	public byte[] getBytesMessage() {
		byte message[] = new byte[7];
		message[0] = (new Integer(Start)).byteValue();
		message[1] = (new Integer(funzione)).byteValue();
		message[2] = (new Integer(tipo)).byteValue();
		message[3] = (new Integer(indirizzo)).byteValue();
		message[4] = (new Integer(dato1)).byteValue();
		message[5] = (new Integer(dato0)).byteValue();
		message[6] = (new Integer(checkSum())).byteValue();
		return message; 
	}

	public int getShort() {
		return (dato1 << 8) + dato0;
	}
		
	/**
	 * Calcola il checksum.
	 */
	public int checkSum() {
		return 0xff - ((Start+funzione+tipo+indirizzo+dato1+dato0) & 0xff); 
	}

	
	public String toString() {
		return indirizzo + " " + getMessageDescription()+" (FUN:"+b2h(funzione)+" TYP:"+b2h(tipo)+") ["+b2b(dato1)+":"+b2b(dato0)+" "+b2h(dato1)+":"+b2h(dato0)+"]";
	}

	/**
	 * Ritorna il contenuto del messaggio sotto forma di array di interi.
	 */
	public int[] getRawMessage() {
		int message[] = new int[7];
		message[0] = Start;
		message[1] = funzione;
		message[2] = tipo;
		message[3] = indirizzo;
		message[4] = dato1;
		message[5] = dato0;
		message[6] = checkSum();
		return message; 
	}

	/**
	 * Ritorna il messaggio riportandone i campi in esadecimale.
	 */
	public String toHexString()
	{
		StringBuffer s = new StringBuffer();
		if (rawmessage == null) {
			rawmessage = getRawMessage();
		}
		s.append("STX:"+b2h(rawmessage[0])+" ");
		s.append("FUN:"+b2h(rawmessage[1])+" ");
		s.append("TYP:"+b2h(rawmessage[2])+" ");
		s.append("ADD:"+b2h(rawmessage[3])+" ");
		s.append("DA1:"+b2h(rawmessage[4])+" ");
		s.append("DA0:"+b2h(rawmessage[5])+" ");
		s.append("CHK:"+b2h(rawmessage[6])+" ");
		return s.toString();
	}
	
	public int getMessageType() {
		return tipo;
	}



}
