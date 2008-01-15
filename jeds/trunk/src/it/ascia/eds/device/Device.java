package it.ascia.eds.device;

import it.ascia.eds.msg.EDSMessage;

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
	public void receiveMessage(EDSMessage m);
}
