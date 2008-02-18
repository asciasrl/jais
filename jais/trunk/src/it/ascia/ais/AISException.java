/**
 * Copyright (C) 2008 ASCIA S.r.l.
 */
package it.ascia.ais;

/**
 * Eccezione sollevata dalle classi di AIS.
 * 
 * @author arrigo
 *
 */
public class AISException extends Exception {
	/**
	 * Costruttore.
	 * 
	 * @param arg0 messaggio di errore.
	 */
	public AISException(String arg0) {
		super(arg0);
	}
}
