/**
 * Copyright (C) 2008 ASCIA S.r.l.
 */

import it.ascia.ais.AISException;
import it.ascia.ais.HTTPServer;
import it.ascia.eds.Bus;
import it.ascia.eds.ConfigurationFile;
import it.ascia.eds.EDSException;
import it.ascia.eds.TCPSerialBus;
import it.ascia.eds.device.BMC;
import it.ascia.eds.device.BMCChronoTerm;
import it.ascia.eds.device.BMCComputer;
import it.ascia.eds.device.BMCDimmer;
import it.ascia.eds.device.BMCStandardIO;


import org.apache.log4j.PropertyConfigurator;

/**
 * Controller per la dimostrazione di AUI e AIS nell'ufficio di WDB.
 *  
 * @author arrigo
 */
public class ControllerWDB extends MyController {
	static Bus bus;
	static BMCComputer bmcComputer;
	static HTTPServer server;
	static BusTest busController;
	
	
	static void makeVirtualBMC(int address) {
		// Lo creiamo noi il BMC!
		try {
			BMCStandardIO bmc = new BMCStandardIO(address, 88, bus, "BMCFinto"); 
			bus.addDevice(bmc);
			bmc.makeSimulated(busController);
		} catch (EDSException e) {
			System.err.println(e.getMessage());
			System.exit(-1);
		}
	}
	
	static void testBMCStandardIO(int address) {
		int porta, valore;
 		// Prova su BMC modello 88, indirizzo 3
 		BMCStandardIO bmc = 
 			(BMCStandardIO)bus.getDevices(String.valueOf(address))[0];
 		System.out.println();
 		System.out.println("Prova BMC Standard I/O");
 		System.out.println("Discovery collegamenti broadcast...");
 		bmcComputer.discoverBroadcastBindings(bmc);
 		porta = 0;
 		while ((porta >= 0) && (porta < 8)) {
 			porta = Stdio.inputInteger("Porta (<0 esce): ");
 			if (porta >= 0) {
 				valore = -1;
 				while ((valore != 0) && (valore != 1)) {
 					valore = Stdio.inputInteger("Valore (0 o 1): ");
 				}
 				bmc.setOutPort(porta, (valore == 1));
 				try {
 					Thread.sleep(500);
 				} catch (InterruptedException e) { }
 				bmc.printStatus();
 				bmc.updateStatus();
 				System.out.println("Stato del BMC aggiornato: ");
 				bmc.printStatus();
 			} // if porta >= 0
 		}
 			
	}
	
	static void testBMCDimmer(int address) {
		int output = 0, value = 0;
 		// Prova su BMC modello 88, indirizzo 3
		BMCDimmer bmc = (BMCDimmer)bus.getDevices(String.valueOf(address))[0];
		System.out.println();
		System.out.println("Prova Dimmer");
		System.out.println("Discovery collegamenti broadcast...");
 		bmcComputer.discoverBroadcastBindings(bmc);
		while ((output >= 0) && (output < 2)) {
			bmc.printStatus();
 			output = Stdio.inputInteger("output (<0 esce): ");
 			if (output >= 0) {
 				value = -101;
 				while ((value < -100) || (value > 100)) {
 					value = Stdio.inputInteger("Valore (0 - 100): ");
 				}
 				bmc.setOutputRealTime(output, value);
 				bmc.printStatus();
 				bmc.updateStatus();
 		 		System.out.println("Stato del BMC dopo la richiesta: ");
 		 		bmc.printStatus();
 		 		System.out.println(bmc.getStatus("*"));
 			} // if output >= 0
		}
	}
	
	static void startServer() {
		busController = new BusTest(null);
		busController.addConnector(bus);
		try {
			server = new HTTPServer(8080, busController, 
					"/home/arrigo/public_html/auiFixed");
		} catch (AISException e) {
			System.err.println(e.getMessage());
			System.exit(-1);
		}
	}

	/**
	 * Effettua il discovery automatico dei BMC sulla rete.
	 * 
	 * @param addressFrom primo indirizzo da provare.
	 * @param addressTo ultimo indirizzo da provare.
	 */
	private static void discovery(int addressFrom, int addressTo) {
		// Discovery
	 	System.out.println("Discovery:");
	 	for (int i = addressFrom; i <= addressTo; i++) {
 			System.out.print(i + ":");
 			BMC bmc = bmcComputer.discoverBMC(i); 
 			if (bmc != null) {
 				System.out.println(bmc.getInfo());
 			} else {
 				System.out.println();
 			}
	 	}
	}
	
	/**
	 * Permette all'utente di "pingare" BMC inserendone l'indirizzo.
	 */
	private static void pingManuale() {
		int dest = 1;
		while (dest > 0) {
			dest = Stdio.inputInteger("Indirizzo da pingare (<0 esce):");
			if (dest > 0) {
				BMC bmc = bmcComputer.discoverBMC(dest); 
				if ( bmc != null) {
					System.out.println(bmc.getInfo());
				} else {
					System.out.println("Non trovato!");
				}
			}
		} // Finche' si specifica un indirizzo valido
	}
	
	/**
	 * Chiede all'utente che cosa vuole fare.
	 */
	private static void userInteraction() {
		String choice = "";
		while (!choice.equals("Q")) {
			System.out.println("    Comandi possibili:");
			System.out.println("P  : permette di fare il \"ping\" di un BMC");
			System.out.println("D  : lancia un discovery automatico");
			System.out.println("B  : permette di controllare un " +
					"BMC Standard I/O");
			System.out.println("Q  : uscita.");
			choice = Stdio.inputString("Comando: ").toUpperCase();
			if (choice.equals("P")) {
				pingManuale();
			} else if (choice.equals("D")) {
				int addressFrom = 
					Stdio.inputInteger("Indirizzo da cui partire:");
				int addressTo = 
					Stdio.inputInteger("Indirizzo a cui fermarsi:");
				discovery(addressFrom, addressTo);
			} else if (choice.equals("B")) {
				int address = Stdio.inputInteger("Indirizzo BMC: ");
				testBMCStandardIO(address);
			}
		}
	}
	
	/**
	 * @param args
	 *            porta seriale
	 */
	public static void main(String[] args) {
	    String defaultPort = "ascia.homeip.net";
		// String defaultPort = "COM1";
	    // Inizializzazione logger
	    PropertyConfigurator.configure("conf/log4j.conf");
		ConfigurationFile cfgFile = null;
	 	if (args.length > 0) {
		    defaultPort = args[0];
		}
	 	try {
	 		bus = new TCPSerialBus(defaultPort, 2001, "0");
	 		// bus = new SerialBus(defaultPort, "0");
	 	} catch (EDSException e) {
	 		System.err.println(e.getMessage());
	 		System.exit(-1);
	 	}
	 	startServer();
	 	bmcComputer = new BMCComputer(0, bus);
	 	bus.setBMCComputer(bmcComputer);
	 	// File di configurazione
	 	try {
			cfgFile = new ConfigurationFile("conf/impianto_ufficio_wdb.xml");
		} catch (EDSException e) {
			System.err.println(e.getMessage());
			System.exit(-1);
		}
		cfgFile.createBMCs(bus);
		System.out.println(cfgFile.getSystemName());
	 	
	 	// BMC virtuale
	 	// makeVirtualBMC(1);
	 	// testBMCStandardIO();
	 	// testBMCDimmer();
	 	// testBMCChronoTerm();
	 	try {
			busController.setDevicesListener();
		} catch (AISException e) {
			System.err.println(e.getMessage());
			System.exit(-1);
		}
	 	// La palla all'utente
		userInteraction();
		server.close();
		bus.close();
	}
	
	public ControllerWDB(String pin) {
		super(pin);
	}
}