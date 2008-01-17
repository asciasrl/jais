/**
 * Copyright (C) 2008 ASCIA S.R.L.
 */
package it.ascia.eds.device;

import it.ascia.eds.*;
import it.ascia.eds.msg.Message;

/**
 * Un BMC
 * 
 * @author arrigo
 */
public class BMC implements Device {
	/// Il bus a cui il BMC è collegato
	private Bus myBus;
	/// L'indirizzo sul bus
	private int address;
	
	/**
	 * @param address l'indirizzo di questo BMC
	 */
	public BMC(int address) {
		this.address = address;
	}
	
	/**
	 * Ritorna l'indirizzo di questo BMC
	 */
	public int getAddress() {
		return this.address;
	}
	
	/** 
	 * Il BMC ha ricevuto un messaggio.
	 * 
	 * Questo metodo dovrebbe essere chiamato solo dal bus
	 * 
	 * @param m il messaggio ricevuto
	 */
	public void receiveMessage(Message m) {
	 // TODO
	}
}
