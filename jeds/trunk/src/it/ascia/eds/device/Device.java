package it.ascia.eds.device;

import it.ascia.eds.msg.Message;

/**
 * Generic device connected to the bus
 * 
 * @author arrigo
 *
 */
public interface Device {
	/** 
	 * Il device ha ricevuto un messaggio.
	 * 
	 * Questo metodo dovrebbe essere chiamato solo dal bus
	 * 
	 * @param m il messaggio ricevuto
	 */
	public void messageReceived(Message m);
	
	/** 
	 * Il device ha inviato un messaggio.
	 * 
	 * Questo metodo dovrebbe essere chiamato solo dal bus
	 * 
	 * @param m il messaggio ricevuto
	 */
	public void messageSent(Message m);
	
	/**
	 * Ritorna l'indirizzo del device sul bus.
	 */
	public int getAddress();
	
	/**
	 * Ritorna lo stato del BMC in formato utile per AUI.
	 * 
	 * @param port il nome della porta da restituire, o "*" per indicarle
	 * tutte.
	 */
	public abstract String getStatus(String port);
}
