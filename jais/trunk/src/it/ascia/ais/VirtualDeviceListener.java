/**
 * Copyright (C) 2008 ASCIA S.r.l.
 */
package it.ascia.ais;

/**
 * Un oggetto che puo' gestire le variazioni di stato di Device virtuali.
 *  
 * <p>Un esempio di Device virtuali possono essere i BMC Standard I/O</p>
 * @author Arrigo
 */
public interface VirtualDeviceListener {
	/**
	 * Segnala il cambiamento di una porta.
	 * 
	 * @param device il Device che sta facendo la segnalazione
	 * @param port il nome della porta
	 * @param newValue il nuovo valore assunto dalla porta
	 */
	public void statusChanged(Device device, String port, String newValue);
}
