/**
 * Copyright (C) 2008 ASCIA S.R.L.
 * 
 */
package it.ascia.eds;

import it.ascia.eds.device.BMC;
import it.ascia.eds.device.BMCComputer;
import it.ascia.eds.device.BMCDimmer;
import it.ascia.eds.device.BMCStandardIO;

import java.io.*;

/**
 * @author arrigo
 * 
 */
public class BusTest {
	static Bus bus;
	
	static void testBMCStandardIO() {
		int address = 3;
 		// Prova su BMC modello 88, indirizzo 3
 		BMCStandardIO bmc = (BMCStandardIO)bus.getDevice(address);
 		System.out.println("Stato del BMC prima della richiesta: ");
 		bmc.printStatus();
 		bmc.updateStatus();
 		System.out.println("Stato del BMC dopo la richiesta: ");
 		bmc.printStatus();	
	}
	
	static void testBMCDimmer() {
		int address = 5;
		int output = 0, value = 0;
 		// Prova su BMC modello 88, indirizzo 3
 		BMCDimmer bmc = (BMCDimmer)bus.getDevice(address);
 		System.out.println("Stato del BMC prima della richiesta: ");
 		bmc.printStatus();
 		bmc.updateStatus();
 		System.out.println("Stato del BMC dopo la richiesta: ");
 		bmc.printStatus();	
 		System.out.println("Imposto l'uscita " + output + " a " + value + ":");
 		bmc.setOutput(output, value);
 		System.out.print("Premi RETURN per continuare");
 		try {
 			new BufferedReader(new InputStreamReader(System.in)).readLine();
 		} catch (IOException e) {
 		}
	}
	
	/**
	 * @param args
	 *            porta seriale
	 */
	public static void main(String[] args) {
	    String defaultPort = "ascia.homeip.net";
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
	 	for (int i = 0; i < 8; i++) {
	 		if ((i != 1) && (i != 4)) {
	 			// Evitiamo gli indirizzi non assegnati
	 			System.out.print(i + ":");
	 			BMC bmc = bmcComputer.discoverBMC(i); 
	 			if (bmc != null) {
	 				System.out.println(bmc.getInfo());
	 			} else {
	 				System.out.println();
	 			}
	 		}
	 	}
	 	testBMCStandardIO();
	 	testBMCDimmer();
	 	// La palla all'utente
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
		}
		System.out.println("Chiusura bus...");
		bus.close();
	}
}
