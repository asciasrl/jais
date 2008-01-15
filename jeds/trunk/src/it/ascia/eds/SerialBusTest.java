/**
 * Copyright (C) 2008 ASCIA S.R.L.
 * 
 */
package it.ascia.eds;

import it.ascia.eds.msg.EDSMessage;

import java.io.*;

/**
 * @author arrigo
 * 
 */
public class SerialBusTest {

	/**
	 * @param args
	 *            porta seriale
	 */
	public static void main(String[] args) {
	    String defaultPort = "/dev/ttyUSB0";
		Bus bus = null;
		
	 	if (args.length > 0) {
		    defaultPort = args[0];
		}
	 	try {
	 		bus = new SerialBus(defaultPort);
	 	} catch (Exception e) {
	 		System.err.println(e.getMessage());
	 		System.exit(-1);
	 	}
	 	try {
	 		BufferedReader stdin = 
	 				new BufferedReader(new InputStreamReader(System.in));
			System.out.println("Running ...");
			int dest = 1;
			while (dest > 0) {
				System.out.print("Indirizzo da contattare:");
				dest = Integer.parseInt(stdin.readLine());
				if (dest > 0) {
					bus.write(new EDSMessage());
					System.out.println("Contattato!");
				}
			}
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
		bus.close();
	}

}
