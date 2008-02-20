/**
 * Copyright (C) 2008 ASCIA S.r.l.
 */
package it.ascia.ais;

/**
 * Bus domotico.
 * 
 * @author arrigo
 */
public interface Bus {
	/**
     * Ritorna un Device a partire dall'indirizzo.
     * 
     * @param address l'indirizzo da cercare.
     * 
     * @return il Device oppure null se il Device non è nella lista.
     */
	Device getDevice(int deviceAddress);

	/**
     * Ritorna tutti i Device collegati.
     */
    public Device[] getDevices();
    
    /**
     * Ritorna il nome del bus.
     * 
     * <p>Questo nome e' parte dell'indirizzo dei Device collegati al bus.</p>
     */
    public String getName();
}
