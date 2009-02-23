/**
 * Copyright (C) 2008 ASCIA S.r.l.
 */
package it.ascia.ais;

import java.util.HashMap;


/**
 * Device generico connesso a un Connector generico.
 * 
 * @author arrigo, sergio
 */
public abstract class Device {
	/**
	 * L'indirizzo sul transport.
	 */
	protected String address;

	/**
	 * I nomi delle porte del dispositivo.
	 */
	protected HashMap portsNames = new HashMap();

	/**
	 * Ritorna l'indirizzo del Device, cosi' come visto da AUI.
	 */
	public String getAddress() {
		return address;
	}
	
	/**
	 * Il bus a cui il dispositivo e' collegato.
	 */
	protected Connector connector;
	
	/**
	 * Ritorna il Connector di questo device.
	 */
	public Connector getConnector() {
		return connector;
	}
	
	/**
	 * Ritorna lo stato del device cambiato rispetto a un certo istante,
	 * in formato utile per AUI.
	 * 
	 * <p>Lo stato deve essere aggiornato.</p>
	 * 
	 * @return lo stato del device che e' cambiato al timestamp specificato, 
	 * oppure piu' tardi.
	 * 
	 * @param port il nome della porta da restituire, o "*" per indicarle
	 * tutte.
	 * 
	 * @param timeStamp il timestamp che "screma" i cambiamenti dello stato
	 * che ci interessano, nella forma ritornata da System.currentTimeMillis().
	 * Se posto a 0, richiede l'intero stato del sistema.
	 */
	public abstract String getStatus(String portId, long timestamp);
	
	public void addPort(String portId, String portName) {
		portsNames.put(portId, portName);		
	}

	public void addPort(String portId) {
		portsNames.put(portId, portId);		
	}
	
	/**
	 * Fornisce il nome descrittivo della porta del device
	 * 
	 * @param portId identificatore univoco della porta del device
	 * @return null Se la porta non esiste
	 */
	public String getPortName(String portId) {
		if (portsNames.containsKey(portId)) {
			return (String) portsNames.get(portId);
		}
		return null;
	}
	
	/**
	 * Imposta il nome descrittivo della porta del device
	 * 
	 * @param portId identificatore univoco della porta del device
	 * @param portName Nuovo nome per la porra
	 * @return false Se la porta non esiste
	 */
	public boolean setPortName(String portId, String portName) {
		if (portsNames.containsKey(portId)) {
			portsNames.put(portId, portName);
			return true;
		}
		return false;		
	}
	
	/**
	 * Imposta il valore di una porta.
	 * 
	 * @param portId il nome della porta
	 * @param value il valore da impostare
	 * 
	 * @throws un'eccezione se qualcosa va male.
	 */
	public abstract void poke(String portId, String value) throws AISException;
	
	/**
	 * Legge il valore di una porta.
	 * 
	 * @param portId il nome della porta
	 * 
	 * @throws un'eccezione se qualcosa va male.
	 */
	public abstract String peek(String portId) throws AISException;
    
    public void bindConnector(Connector connector) {
    	this.connector = connector;
    }
}
