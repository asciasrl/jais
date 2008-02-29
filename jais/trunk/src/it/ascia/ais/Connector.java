/**
 * Copyright (C) 2008 ASCIA S.r.l.
 */
package it.ascia.ais;

/**
 * Connector domotico.
 * 
 * <p>Un Connector e' un'interfaccia per JAIS verso un sistema. Ad esempio un
 * bus domotico o una centrale d'allarme.</p>
 * 
 * <p>Un Connector permette l'accesso a un insieme di {@link Device}.</p>
 * 
 * <p>A livello di protocollo, un connector e' identificato da un nome,
 * nella forma "tipo.numero".</p>
 * 
 * @author arrigo
 */
public interface Connector {
	/**
     * Ritorna un Device a partire dall'indirizzo.
     * 
     * @param address l'indirizzo da cercare.
     * 
     * @return il Device oppure null se il Device non e' nella lista.
     */
	Device getDevice(int deviceAddress);

	/**
     * Ritorna tutti i Device collegati.
     */
    public Device[] getDevices();
    
    /**
     * Ritorna il nome del Connector, nella forma "tipo.numero".
     * 
     * <p>Questo nome e' parte dell'indirizzo dei Device collegati al 
     * Connector.</p>
     */
    public String getName();
}
