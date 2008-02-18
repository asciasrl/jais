package it.ascia.eds.device;

import it.ascia.ais.AISException;
import it.ascia.eds.msg.Message;

/**
 * Oggetto generico connesso al bus EDS.
 * 
 * @author arrigo
 */
public interface Device extends it.ascia.ais.Device {
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
}
