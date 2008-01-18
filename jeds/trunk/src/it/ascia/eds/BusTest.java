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
public class BusTest {

	/**
	 * @param args
	 *            porta seriale
	 */
	public static void main(String[] args) {
	    String defaultPort = "ascia.homeip.net";
		Bus bus = null;
		BMCComputer bmcComputer;
	 	if (args.length > 0) {
		    defaultPort = args[0];
		}
	 	try {
	 		bus = new TCPSerialBus(defaultPort);
	 	} catch (EDSException e) {
	 		System.err.println(e.getMessage());
	 		System.exit(-1);
	 	}
	 	bmcComputer = new BMCComputer(0, bus);
	 	bus.setBMCComputer(bmcComputer);
	 	// Discovery
	 	System.out.println("Discovery:");
	 	for (int i = 0; i < 11; i++) {
	 		System.out.print(i + ":");
	 		try {
	 			BMC bmc = bmcComputer.discoverBMC(i); 
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
						BMC bmc = bmcComputer.discoverBMC(dest); 
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
