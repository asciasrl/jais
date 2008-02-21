/**
 * Copyright (C) 2007 ASCIA S.R.L.
 */
package it.ascia.eds.msg;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Messaggio EDS generico.
 * 
 * @author sergio, arrigo
 */
public abstract class Message
implements MessageInterface {
	/**
	 * Richiesta modello e revisione.
	 */
	public final static int MSG_RICHIESTA_MODELLO = 0;
	/**
	 * Risposta a richiesta modello e revisione.
	 */
	public final static int MSG_RISPOSTA_MODELLO = 1;
	/**
	 * Variazione di un ingresso, oppure impostazione degli stati del 
	 * termostato.
	 * 
	 * @see VariazioneIngressoMessage
	 */
	public final static int MSG_VARIAZIONE_INGRESSO = 4;
	/**
	 * Acknowledge.
	 */
	public final static int MSG_ACKNOWLEDGE = 6;
	/**
	 * Richiesta lettura associazione di un'uscita a un comando broadcast.
	 */
	public final static int MSG_RICHIESTA_ASSOCIAZIONE_BROADCAST = 15;
	/**
	 * Risposta a richiesta lettura associazione di un'uscita a un comando 
	 * broadcast.
	 */
	public final static int MSG_RISPOSTA_ASSOCIAZIONE_BROADCAST = 16;
	/**
	 * Messaggio broadcast di attivazione/disattivazione (attuazione multipla).
	 */
	public final static int MSG_COMANDO_BROADCAST = 17;
	/**
	 * Attivazione/disattivazione di un'uscita.
	 */
	public final static int MSG_COMANDO_USCITA = 21;
	/**
	 * Richiesta lettura stato dispositivo.
	 */
	public final static int MSG_RICHIESTA_STATO = 25;
	/**
	 * Risposta lettura stato dispositivo.
	 */
	public final static int MSG_RISPOSTA_STATO = 26;
	/**
	 * Cambio velocita' di comunicazione.
	 */
	public final static int MSG_CAMBIO_VELOCITA = 27;
	/**
	 * Apertura/chiusura programmazione.
	 * 
	 * @see ProgrammazioneMessage
	 */
	public final static int MSG_PROGRAMMAZIONE = 28;
	/**
	 * Richiesta ingresso IR. (?)
	 */
	public final static int MSG_RICHIESTA_INGRESSO_IR = 39;
	/**
	 * Risposta ingresso IR. (?)
	 */
	public final static int MSG_RISPOSTA_INGRESSO_IR = 40;
	/**
	 * Accensione uscita real time.
	 */
	public final static int MSG_COMANDO_USCITA_DIMMER = 51;
	/**
	 * Risposta lettura stato uscite dimmer.
	 */
	public final static int MSG_RISPOSTA_STATO_DIMMER = 53;
	/**
	 * Settaggio parametri di configurazione dimmer o sonda termica.
	 */
	public final static int MSG_IMPOSTA_PARAMETRO = 54;
	/**
	 * Richiesta parametri di configurazione dimmer o sonda termica.
	 */
	public final static int MSG_RICHIESTA_PARAMETRO = 55;
	/**
	 * Risposta a richiesta parametri di configurazione.
	 */
	public final static int MSG_RISPOSTA_PARAMETRO = 56;
	/**
	 * Richiesta dello stato di un cronotermostato.
	 *
	 * @see RichiestaStatoTermostatoMessage
	 */
	public final static int MSG_RICHIESTA_STATO_TERMOSTATO = 200;
	/**
	 * Lettura dello stato del cronotermostato (monitoraggio).
	 */
	public final static int MSG_TEMPERATURA = 201;
	/**
	 * Impostazione valore del setpoint del cronotermostato.
	 * 
	 * @see ImpostaSetPointMessage
	 */
	public final static int MSG_IMPOSTA_SET_POINT = 202;
	/**
	 * Richiesta del set point (monitoraggio).
	 * 
	 * @see RichiestaSetPointMessage
	 */
	public final static int MSG_RICHIESTA_SET_POINT = 204;
	/**
	 * Lettura del set point del cronotermostato (monitoraggio).
	 */
	public final static int MSG_LETTURA_SET_POINT = 205;
	/**
	 * Rappresentazione 'raw' del messaggio.
	 */
	protected int[] rawmessage;
	/**
	 * Byte di partenza.
	 */
	protected static int Stx = 2;
	/**
	 * Byte di chiusura.
	 */
	protected static int Etx = 3;

	protected int Destinatario;

	protected int Mittente;

	protected int TipoMessaggio;

	protected int Byte1;
	protected int Byte2;

	/**
	 * Ritorna il contenuto del messaggio sotto forma di array di interi.
	 */
	public int[] getRawMessage() {
		int message[] = new int[8];
		message[0] = Stx;
		message[1] = Destinatario;
		message[2] = Mittente;
		message[3] = TipoMessaggio;
		message[4] = Byte1;
		message[5] = Byte2;
		message[6] = checkSum();
		message[7] = Etx;
		return message; 
	}

	/**
	 * Ritorna l'indirizzo del destinatario del messaggio.
	 */
	public int getRecipient() {
		return Destinatario;
	}
	/**
	 * Ritorna l'indirizzo del mittente del messaggio.
	 */
	public int getSender() {
		return Mittente;
	}

	/**
	 * Ritorna il contenuto del messaggio sotto forma di array di byte.
	 */
	public byte[] getBytesMessage() {
		byte message[] = new byte[8];
		message[0] = (new Integer(Stx)).byteValue();
		message[1] = (new Integer(Destinatario)).byteValue();
		message[2] = (new Integer(Mittente)).byteValue();
		message[3] = (new Integer(TipoMessaggio)).byteValue();
		message[4] = (new Integer(Byte1)).byteValue();
		message[5] = (new Integer(Byte2)).byteValue();
		message[6] = (new Integer(checkSum())).byteValue();
		message[7] = (new Integer(Etx)).byteValue();
		return message; 
	}

	/**
	 * Invia se stesso.
	 * @param out OutputStream su cui inviarsi.
	 */
	public void write(OutputStream out) throws IOException {
		out.write(getBytesMessage());
	}

	/**
	 * Calcola il checksum.
	 */
	public int checkSum() {
		return (new Integer((Stx+Destinatario+Mittente+TipoMessaggio+Byte1+Byte2) & 0xff)).byteValue();
	}

	public String toString() {
		StringBuffer s = new StringBuffer();
		s.append(toHexString() + "\r\n");
		s.append("Tipo Messaggio: "+getTipoMessaggio()+"\r\n");
		s.append(getInformazioni());
		return s.toString();
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
		s.append("DST:"+b2h(rawmessage[1])+" ");
		s.append("MIT:"+b2h(rawmessage[2])+" ");
		s.append("TIP:"+b2h(rawmessage[3])+" ");
		s.append("BY1:"+b2h(rawmessage[4])+" ");
		s.append("BY2:"+b2h(rawmessage[5])+" ");
		s.append("CHK:"+b2h(rawmessage[6])+" ");
		s.append("ETX:"+b2h(rawmessage[7])+" ");
		return s.toString();
	}

	/**
	 * Funzione di utilita': restituisce la rappresentazione esadecimale
	 * di un byte.
	 */
	protected static String b2h(int i)
	{
		String s = "0x";
		if (i < 16) {
			s += "0";
		}
		s += Integer.toHexString(i);
		return s;
	}

	public String getTipoMessaggio() {
		return "Unknown ("+TipoMessaggio+")";
	}

	/**
	 * Ritorna una descrizione testuale che interpreta i campi del messaggio.
	 */
	public String getInformazioni()
	{
		StringBuffer s = new StringBuffer();
		s.append("Mittente: "+Mittente+"\r\n");
		s.append("Destinatario: "+Destinatario+"\r\n");
		s.append("Byte1: "+Byte1+"\r\n");
		s.append("Byte2: "+Byte2+"\r\n");
		return s.toString();
	}

	public Message() {  
	}

	/**
	 * Costruisce il messaggio a partire da un'array di interi.
	 */
	public Message(int[] message) {
		parseMessage(message);
	}

	/**
	 * Carica i dati da un'array di interi.
	 * 
	 * ATTENZIONE: non verifica che il tipo sia coerente!
	 */
	public void parseMessage(int[] message) {
		rawmessage = message;
		Destinatario = message[1];
		Mittente = message[2];
		TipoMessaggio = message[3];
		Byte1 = message[4];
		Byte2 = message[5];
	}

	/**
	 * Verifica se il messaggio e' di tipo broadcast.
	 * 
	 * @return true se il messaggio e' broadcast.
	 */
	public abstract boolean isBroadcast();
	
	/**
	 * Ritorna il tipo di messaggio.
	 */
	public abstract int getMessageType();
}
