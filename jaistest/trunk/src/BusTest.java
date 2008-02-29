

import it.ascia.ais.AISException;
import it.ascia.ais.Controller;
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
 * @author arrigo
 * 
 */
public class BusTest extends MyController {
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
 		BMCStandardIO bmc = (BMCStandardIO)bus.getDevice(address);
 		System.out.println();
 		System.out.println("Prova BMC Standard I/O");
 		System.out.println("Discovery...");
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
	
	static void testBMCDimmer() {
		int address = 5;
		int output = 0, value = 0;
 		// Prova su BMC modello 88, indirizzo 3
		BMCDimmer bmc = (BMCDimmer)bus.getDevice(address);
		System.out.println();
		System.out.println("Prova Dimmer");
		System.out.println("Discovery...");
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
	
	static void testBMCChronoTerm() {
		int address = 7;
		double setPoint = 0;
 		// Prova su BMC modello 127, indirizzo 7
		BMCChronoTerm bmc = (BMCChronoTerm)bus.getDevice(address);
		System.out.println();
		System.out.println("Prova Cronotermostato");
		while (setPoint >= 0.0) {
			bmc.printStatus();
 			setPoint = Stdio.inputDouble("set point (<0 esce): ");
 			if (setPoint >= 0.0) {
 				bmc.setSetPoint(setPoint);
 				bmc.printStatus();
 				bmc.updateStatus();
 		 		System.out.println("Stato del BMC dopo la richiesta: ");
 		 		bmc.printStatus();
 		 		System.out.println(bmc.getStatus("*"));
 			} // if output >= 0
		}
		bmc.printStatus();
		bmc.updateStatus();
		bmc.printStatus();
	}
	
	static void startServer() {
		busController = new BusTest(bus, "1");
		try {
			server = new HTTPServer(8080, busController, 
					"/home/arrigo/public_html/auiFixed");
		} catch (AISException e) {
			System.err.println(e.getMessage());
			System.exit(-1);
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
			cfgFile = new ConfigurationFile("conf/tavola20071207.xml");
		} catch (EDSException e) {
			System.err.println(e.getMessage());
			System.exit(-1);
		}
		cfgFile.createBMCs(bus);
		System.out.println(cfgFile.getSystemName());
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
	 	// Dimmer avanzato
	 	bmcComputer.discoverBMC(255);
	 	// BMC virtuale
	 	makeVirtualBMC(1);
	 	// testBMCStandardIO();
	 	// testBMCDimmer();
	 	// testBMCChronoTerm();
	 	// La palla all'utente
	 	System.out.println("Running ...");
		int dest = 1;
		while (dest > 0) {
			dest = Stdio.inputInteger("Indirizzo da pingare:");
				if (dest > 0) {
					BMC bmc = bmcComputer.discoverBMC(dest); 
					if ( bmc != null) {
						System.out.println(bmc.getInfo());
					} else {
						System.out.println("Non trovato!");
					}
				}
		}
		server.close();
		bus.close();
	}
	
	public BusTest(Bus bus, String pin) {
		super(bus, pin);
	}
}
