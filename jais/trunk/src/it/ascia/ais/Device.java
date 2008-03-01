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
	 * Ritorna lo stato del device in formato utile per AUI.
	 * 
	 * <p>Lo stato deve essere aggiornato.</p>
	 * 
	 * @param port il nome della porta da restituire, o "*" per indicarle
	 * tutte.
	 */
	public String getStatus(String port);
	
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
