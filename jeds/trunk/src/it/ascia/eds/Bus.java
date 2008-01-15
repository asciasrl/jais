/**
 * Copyright (C) 2008 ASCIA S.R.L.
 */
package it.ascia.eds;

import it.ascia.eds.msg.*;

/**
 * Interfaccia verso il bus EDS
 * 
 * @author arrigo
 *
 */
public interface Bus {
	/**
     * Invia un messaggio sul bus.
     * 
     * Eventuali errori di trasmissione vengono ignorati.
     * 
     * @param m the message to send
     */
    public void write(EDSMessage m);
    
    /**
     * Chiude la connessione al bus.
     */
    public void close();
}
