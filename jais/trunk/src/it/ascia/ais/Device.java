/**
 * Copyright (C) 2008 ASCIA S.r.l.
 */
package it.ascia.ais;

/**
 * Device generico connesso a un Connector generico.
 * 
 * @author arrigo
 */
public interface Device {
	/**
	 * Ritorna l'indirizzo del Device, cosi' come visto da AUI.
	 */
	public String getAddress();
	
	/**
	 * Ritorna il Connector di questo device.
	 */
	public Connector getConnector();
	
	/**
	 * Ritorna lo stato del device cambiato rispetto a un certo istante,
	 * in formato utile per AUI.
	 * 
	 * <p>Lo stato deve essere aggiornato.</p>
	 * 
	 * @return lo stato del device che è cambiato al timestamp specificato, 
	 * oppure più tardi.
	 * 
	 * @param port il nome della porta da restituire, o "*" per indicarle
	 * tutte.
	 * 
	 * @param timeStamp il timestamp che "screma" i cambiamenti dello stato
	 * che ci interessano, nella forma ritornata da System.currentTimeMillis().
	 * Se posto a 0, richiede l'intero stato del sistema.
	 */
	public String getStatus(String port, long timestamp);
	
	/**
	 * Imposta il valore di una porta.
	 * 
	 * @param port il nome della porta
	 * @param value il valore da impostare
	 * 
	 * @throws un'eccezione se qualcosa va male.
	 */
	public void setPort(String port, String value) throws AISException;
	
	/**
     * Imposta un DeviceListener che ricevera' gli eventi di questo Device.
     * 
     * @param listener il DeviceListener che ricevera' gli eventi.
     */
    public void setDeviceListener(DeviceListener listener);
}
