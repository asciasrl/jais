/**
 * Copyright (C) 2008 ASCIA S.r.l.
 */
package it.ascia.bentel;

import java.io.IOException;

import org.apache.log4j.PropertyConfigurator;

/**
 * Test per JBisListener.
 * 
 * FIXME: JBisListener non funziona, quindi non funziona neanche questa classe.
 * @author arrigo
 */
public class JBisListenerTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String defaultPort = "COM1";
		JBisListener centralina;
	    // Inizializzazione logger
	    PropertyConfigurator.configure("conf/log4j.conf");
	 	if (args.length > 0) {
		    defaultPort = args[0];
		}
	 	try {
	 		centralina = new JBisListener(defaultPort/*, new JBisListenerTest()*/); // FIXME
	 		System.out.println("Premi ENTER per terminare.");
	 		System.in.read();
	 		centralina.close();
	 	} catch (IOException e) {
	 		System.err.println(e.getMessage());
	 		System.exit(-1);
	 	} catch (JBisException e) {
	 		System.err.println(e.getMessage());
	 		System.exit(-1);
	 	}
	}

	public void alarmReceived(String alarm) {
		System.err.println("Allarme: " + alarm);
	}

}
