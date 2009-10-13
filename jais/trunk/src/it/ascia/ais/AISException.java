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
public class AISException extends RuntimeException {
	/**
	 * Costruttore.
	 * 
	 * @param message messaggio di errore.
	 */
	public AISException(String message) {
		super(message);
	}

	public AISException(String message, Throwable cause) {
		super(message, cause);
	}
}
