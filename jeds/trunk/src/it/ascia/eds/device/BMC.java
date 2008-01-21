/**
 * Copyright (C) 2008 ASCIA S.R.L.
 */
package it.ascia.eds.device;

import it.ascia.eds.*;
import it.ascia.eds.msg.Message;

/**
 * Un BMC.
 * 
 * I BMC devono poter aggiornare il proprio stato. Questo deve avvenire inviando
 * messaggi sul bus che abbiano come mittente il BMCComputer.
 * 
 * @author arrigo
 */
public abstract class BMC implements Device {
	/**
	 * Il bus a cui il BMC e' collegato
	 */
	protected Bus bus;
	/**
	 * L'indirizzo sul bus
	 */
	protected int address;
	/**
	 * Il modello di questo BMC
	 */
	protected int model;
	/**
	 * Il nome che AUI da' a questo BMC
	 */
	protected String name;		
	
	/**
	 * @param address l'indirizzo di questo BMC
	 * @param model il modello di questo BMC
	 */
	public BMC(int address, int model, Bus bus, String name) {
		this.bus = bus;
		this.address = address;
		this.model = model;
		this.name = name;
	}
	
	/**
	 * Ritorna l'indirizzo di questo BMC
	 */
	public int getAddress() {
		return this.address;
	}
	
	/** 
	 * Il BMC ha inviato o ricevuto un messaggio.
	 * 
	 * Questo metodo deve leggere il contenuto del messaggio e aggiornare lo 
	 * stato interno.
	 * 
	 * Dovrebbe essere chiamato solo dal bus. Deve essere chiamato sia per i
	 * messaggi inviati, sia per quelli ricevuti.
	 * 
	 * @param m il messaggio ricevuto
	 */
	public abstract void receiveMessage(Message m);
	
	/**
	 * Ritorna una descrizione del BMC.
	 */
	public abstract String getInfo();
	
	/**
	 * Ritorna lo stato del BMC in formato utile per AUI.
	 */
	public abstract String getStatus();
	
	/**
	 * Aggiorna la rappresentazione interna delle porte.
	 * 
	 * Manda un messaggio al BMC mettendo come mittente il bmcComputer. Quando 
	 * arrivera' la risposta, receiveMessage() aggiornera' le informazioni.
	 */
	public abstract void updateStatus();
	
	/**
	 * Stampa una descrizione dello stato del BMC (facoltativa).
	 * 
	 * Questa funzione ha senso solo se implementata dalle sottoclassi.
	 */
	public void printStatus() {
		System.out.println("printStatus() non implementata");
	}
}
