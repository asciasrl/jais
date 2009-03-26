package it.ascia.dxp;

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

	protected int getShort() {
		return (dato1 << 8) + dato0;
	}
		
	/**
	 * Calcola il checksum.
	 */
	public int checkSum() {
		return (Start+funzione+tipo+indirizzo+dato1+dato0) & 0xff; 
	}

	
	public String toString() {
		return indirizzo + " " + getMessageDescription()+" ("+b2h(funzione)+","+b2h(tipo)+") ["+b2b(dato1)+":"+b2b(dato0)+" "+b2h(dato1)+":"+b2h(dato0)+"]";
	}
	
	public int getMessageType() {
		return tipo;
	}



}
