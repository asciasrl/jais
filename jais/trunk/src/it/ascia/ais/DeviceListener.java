/**
 * Copyright (C) 2008 ASCIA S.r.l.
 */
package it.ascia.ais;

/**
 * Un oggetto che puo' gestire le variazioni di stato di Device.
 *  
 * @author Arrigo
 */
public interface DeviceListener {
	/**
	 * Segnala il cambiamento di una porta.
	 * 
	 * @param event l'evento che contiene le informazioni sul cambiamento.
	 */
	public void statusChanged(DeviceEvent event);
}
