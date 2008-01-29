/**
 * Copyright (C) 2008 ASCIA S.R.L.
 */
package it.ascia.eds;

/**
 * @author arrigo
 *
 * Il controller del bus.
 */
public class BusController {
	/**
	 * Il cuore del controllore: riceve la richiesta e produce una risposta.
	 */
	public String receiveRequest(String command, String name, String value) {
		System.out.println("Comando: \"" + command + "\" \"" + name + "\" \"" +
				value + "\"");
		return "ERROR: Not implemented.";
	}
}
