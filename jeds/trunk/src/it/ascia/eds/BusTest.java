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
	static BufferedReader stdin;
	
	/**
	 * Java avra' tanti pregi, ma l'input da stdin e' difficile.
	 */
	static int inputInteger(String message) {
		int retval = 0;
		boolean entered = false;
		while (!entered) {
			try {
				System.out.print(message);
				retval = Integer.parseInt(stdin.readLine());
				entered = true;
			} catch (NumberFormatException e) {
				// Inserito un input invalido. Lo ignoriamo.
			} catch (IOException e) {
			}
		}
		return retval;
	}
	
	static void testBMCStandardIO() {
		int address = 3;
		int porta, valore;
 		// Prova su BMC modello 88, indirizzo 3
 		BMCStandardIO bmc = (BMCStandardIO)bus.getDevice(address);
 		System.out.println();
 		System.out.println("Prova BMC Standard I/O");
 		porta = 0;
 		while ((porta >= 0) && (porta < 8)) {
 			porta = inputInteger("Porta (<0 esce): ");
 			if (porta >= 0) {
 				valore = -1;
 				while ((valore != 0) && (valore != 1)) {
 					valore = inputInteger("Valore (0 o 1): ");
 				}
 				bmc.setOutPort(porta, (valore == 1));
 				bmc.updateStatus();
 				System.out.println("Stato del BMC aggiornato: ");
 				bmc.printStatus();
 				System.out.println(bmc.getStatus());
 			} // if porta >= 0
 		}
 			
	}
	
	static void testBMCDimmer() {
		int address = 5;
		int output = 0, value = 0;
 		// Prova su BMC modello 88, indirizzo 3
		BMCDimmer bmc = (BMCDimmer)bus.getDevice(address);
		System.out.println();
		System.out.println("Prova Dimmer");
		while ((output >= 0) && (output < 2)) {
 			output = inputInteger("output (<0 esce): ");
 			if (output >= 0) {
 				value = -1;
 				while ((value < 0) || (value > 100)) {
 					value = inputInteger("Valore (0 - 100): ");
 				}
 				bmc.setOutput(output, value);
 				bmc.updateStatus();
 		 		System.out.println("Stato del BMC dopo la richiesta: ");
 		 		bmc.printStatus();
 		 		System.out.println(bmc.getStatus());
 			} // if output >= 0
		}
	}
	
	/**
	 * @param args
	 *            porta seriale
	 */
	public static void main(String[] args) {
	    String defaultPort = "ascia.homeip.net";
 		stdin = new BufferedReader(new InputStreamReader(System.in));
		BMCComputer bmcComputer;
		ConfigurationFile cfgFile = null;
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
	 	//	  File di configurazione
	 	try {
			cfgFile = new ConfigurationFile("conf/tavola20071207.xml");
		} catch (EDSException e) {
			System.err.println(e.getMessage());
			System.exit(-1);
		}
		cfgFile.createBMCs(bus);
		System.out.println(cfgFile.getSystemName());
	 	testBMCStandardIO();
	 	testBMCDimmer();
	 	// La palla all'utente
	 	System.out.println("Running ...");
		int dest = 1;
		while (dest > 0) {
			dest = inputInteger("Indirizzo da pingare:");
				if (dest > 0) {
					BMC bmc = bmcComputer.discoverBMC(dest); 
					if ( bmc != null) {
						System.out.println(bmc.getInfo());
					} else {
						System.out.println("Non trovato!");
					}
				}
		}
		System.out.println("Chiusura bus...");
		bus.close();
	}
}
