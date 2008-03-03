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
     * Ritorna tutti i Device collegati che rispondono a un certo indirizzo.
     * 
     * <p>Questa funzione deve gestire anche wildcard.</p>
     * 
     * @param deviceAddress l'indirizzo da cercare.
     * 
     * @return un'array di Device, eventualmente di lunghezza zero.
     */
    public Device[] getDevices(String deviceAddress);
    
    /**
     * Ritorna il nome del Connector, nella forma "tipo.numero".
     * 
     * <p>Questo nome e' parte dell'indirizzo dei Device collegati al 
     * Connector.</p>
     */
    public String getName();
}
