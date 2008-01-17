/**
 * COPYRIGHT (C) 2008 ASCIA S.R.L.
 */
package it.ascia.eds.device;

import java.util.*;

import it.ascia.eds.msg.Message;
import it.ascia.eds.Bus;

/**
 * Il rappresentante di questo computer sul bus EDS.
 * 
 * Le sue funzioni sono:
 *   * tenere un log di tutti i messaggi ricevuti
 *   * spedire messaggi sul bus
 * 
 * Ci deve essere un solo oggetto di questa classe per ciascun bus.
 * 
 * @author arrigo
 */
public class BMCComputer implements Device {
	
	/**
	 * Il nostro indirizzo sul bus.
	 */
	private int address;
	/**
	 * Il nostro bus.
	 */
	private Bus bus;
	/**
	 * Queue dei messaggi ricevuti.
	 */
	private LinkedList inbox;
	/**
	 * Queue dei messaggi in uscita (cioè che devono essere inviati).
	 */
	private LinkedList outbox;
	
	/**
	 * Costruttore.
	 * 
	 * @param bus il bus a cui siamo collegati
	 * @param address l'indirizzo di questo device sul bus
	 */
	public BMCComputer(int address, Bus bus) {
		this.address = address;
		this.bus = bus;
		inbox = new LinkedList();
		outbox = new LinkedList();
	}
	
	/* (non-Javadoc)
	 * @see it.ascia.eds.device.Device#getAddress()
	 */
	public int getAddress() {
		return this.address;
	}

	/* (non-Javadoc)
	 * @see it.ascia.eds.device.Device#receiveMessage(it.ascia.eds.msg.Message)
	 */
	public void receiveMessage(Message m) {
		inbox.addLast(m);
	}
	
	/**
	 * Restituisce il primo messaggio nella coda "inbox".
	 * 
	 * @return il messaggio oppure null se la coda è vuota.
	 */
	public Message getNextMessage() {
		Message retval;
		try {
			retval = (Message) inbox.removeFirst();
		} catch (NoSuchElementException e) {
			retval = null;
		}
		return retval;
	}
}
