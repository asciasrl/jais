/**
 * Copyright (C) 2008 ASCIA S.r.l.
 */
package it.ascia.ais;

/**
 * Questa interfaccia deve essere implementata da controllori per bus domotici.
 * 
 * @author arrigo
 */
public interface BusController {

	/**
	 * Il cuore del controllore: riceve la richiesta e produce una risposta.
	 * 
	 * @param command comando
	 * @param name indirizzo del/dei device interessati
	 * @param value parametri del comando (puo' essere null)
	 * @param pin pin
	 */
	public String receiveRequest(String command, String name, String value,
			String pin);
}
