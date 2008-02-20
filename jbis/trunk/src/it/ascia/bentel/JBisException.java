/**
 * Copyright (C) 2008 ASCIA S.r.l.
 */
package it.ascia.bentel;

import it.ascia.ais.AISException;

/**
 * Eccezione di JBis.
 * @author arrigo
 */
public class JBisException extends AISException {
	/**
	 * Costruttore.
	 * @param arg0 messaggio di errore
	 */
	public JBisException(String arg0) {
		super(arg0);
	}
}
