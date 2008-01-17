/**
 * Copyright (C) 2008 ASCIA S.R.L.
 * 
 */
package it.ascia.eds;

import it.ascia.eds.msg.Message;
import it.ascia.eds.device.BMC;
import it.ascia.eds.device.BMCComputer;

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
	 	bus.setBMCComputer(new BMCComputer(0, bus));
	 	// Discovery
	 	System.out.println("Discovery:");
	 	for (int i = 0; i < 11; i++) {
	 		System.out.print(i + ":");
	 		try {
	 			BMC bmc = bus.discoverBMC(i); 
	 			if (bmc != null) {
	 				System.out.println(bmc.getInfo());
	 			} else {
	 				System.out.println();
	 			}
	 		} catch (Exception e) {
	 		}
	 	}
	 	try {
	 		BufferedReader stdin = 
	 				new BufferedReader(new InputStreamReader(System.in));
			System.out.println("Running ...");
			int dest = 1;
			while (dest > 0) {
				System.out.print("Indirizzo da pingare:");
				try {
					dest = Integer.parseInt(stdin.readLine());
					if (dest > 0) {
						BMC bmc = bus.discoverBMC(dest); 
						if ( bmc != null) {
							System.out.println(bmc.getInfo());
						} else {
							System.out.println("Non trovato!");
						}
					}
				} catch (NumberFormatException e) {
			 		// Inserito un input invalido. Lo ignoriamo.
				}
			}
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}  catch (Exception e) {
			System.err.println(e.getMessage());
		}
		bus.close();
	}

}
