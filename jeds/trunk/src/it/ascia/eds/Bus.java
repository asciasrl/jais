/**
 * Copyright (C) 2008 ASCIA S.R.L.
 */
package it.ascia.eds;

import it.ascia.eds.device.BMC;
import it.ascia.eds.device.BMCComputer;
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
    public void write(Message m);
    
    /**
     * Chiude la connessione al bus.
     */
    public void close();
    
    /**
     * Imposta il BMCComputer del bus.
     */
    public void setBMCComputer(BMCComputer bmcComputer);
    
    /**
     * "Scopre" il BMC indicato inviandogli un messaggio di richiesta modello.
     * 
     * Se il BMC e' gia' in lista, vengono utilizzate le informazioni gia' note.
     * 
     * Se il BMC non era gia' in lista, allora viene inserito.
     * 
     * @param address l'indirizzo del BMC da "scoprire"
     * 
     * @return L'oggetto BMC (della sottoclasse giusta), o null se nessun BMC
     * ha risposto al ping.
     *  
     * @throws un'Exception se non esiste un BMCComputer sul bus.
     */
    public BMC discoverBMC(int address) throws Exception;
}
