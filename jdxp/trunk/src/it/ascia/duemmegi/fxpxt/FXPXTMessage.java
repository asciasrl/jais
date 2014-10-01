package it.ascia.duemmegi.fxpxt;

import it.ascia.ais.Message;

public abstract class FXPXTMessage extends Message {

	protected int indirizzo;
	protected int codice;
	protected int bytes;
	protected int[] dati;
	protected int checksumH;
	protected int checksumL;

	final static int READ_RAM = 0x7F;
	final static int WRITE_RAM = 0x7E;

	final static int READ_EEPROM = 0x7D;
	final static int WRITE_EEPROM = 0x7C;

	final static int READ_OUTPUTS = 0x7B;
	final static int READ_INPUTS = 0x7A;

	final static int WRITE_OUTPUT = 0x79;
	final static int WRITE_VIRTUAL = 0x78;

	final static int READ_ID = 0x70;
	
	/**
	 * Rappresentazione 'raw' del messaggio.
	 */
	protected int[] rawmessage;

	/**
	 * Carica i dati da un'array di interi.
	 * 
	 * ATTENZIONE: non verifica il checksum
	 */
	protected void load(int[] message) {
		rawmessage = message;
		indirizzo = message[0];
		codice = message[1];
		bytes = message[2];
		dati = new int[bytes];
		for (int i = 0; i < bytes; i++) {
			dati[i] = message[i + 3];
		}
		checksumH = message[3 + bytes];
		checksumL = message[4 + bytes];
	}

	/**
	 * Ritorna il contenuto del messaggio sotto forma di array di byte.
	 */
	public byte[] getBytesMessage() {
		byte message[] = new byte[5+bytes];
		message[0] = (new Integer(indirizzo)).byteValue();
		message[1] = (new Integer(codice)).byteValue();
		message[2] = (new Integer(bytes)).byteValue();
		for (int i = 0; i < bytes; i++) {
			message[i + 3] = (new Integer(dati[i])).byteValue(); 
		}
		message[3 + bytes] = (new Integer(checksumH)).byteValue();
		message[4 + bytes] = (new Integer(checksumL)).byteValue();
		return message; 
	}

	/**
	 * Calcola il checksum.
	 */
	private int calculateChecksum() {
		int tmp = 0;
		tmp = indirizzo + codice + bytes;
		for (int i = 0; i < bytes; i++) {
			tmp += dati[i];
		}
		return 0xffff - (tmp & 0xffff); 
	}
	
	/**
	 * Set the checksum bytes of the message
	 */
	protected void setChecksum() {
		int checksum = calculateChecksum();
		checksumH = (checksum & 0xff00) >> 8;
		checksumL = checksum & 0x00ff;		
	}
	
	/**
	 * 
	 * @return true if the checksum fields are valid 
	 */
	boolean testChecksum() {
		int checksum = calculateChecksum();
		return ((checksumH == ((checksum & 0xff00) >> 8)) && (checksumL == (checksum & 0x00ff)));				
	}

	
	public String toString() {
		StringBuffer s = new StringBuffer();
		s.append(indirizzo + " " + getMessageDescription());
		for (int i = 0; i < bytes; i++) {
			s.append(b2h(dati[i]));			
		}
		return s.toString();
	}
	
}
