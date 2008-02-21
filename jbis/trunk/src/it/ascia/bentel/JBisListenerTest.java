/**
 * Copyright (C) 2008 ASCIA S.r.l.
 */
package it.ascia.bentel;

import it.ascia.ais.AlarmReceiver;

import java.io.IOException;

import org.apache.log4j.PropertyConfigurator;

/**
 * Test per JBisListener.
 * 
 * @author arrigo
 */
public class JBisListenerTest implements AlarmReceiver {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String defaultPort = "/dev/ttyUSB0";
		JBisListener centralina;
	    // Inizializzazione logger
	    PropertyConfigurator.configure("conf/log4j.conf");
	 	if (args.length > 0) {
		    defaultPort = args[0];
		}
	 	try {
	 		centralina = new JBisListener(defaultPort, new JBisListenerTest());
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
