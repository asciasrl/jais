

import it.ascia.ais.AISException;
import it.ascia.ais.Transport;
import it.ascia.ais.Connector;
import it.ascia.ais.HTTPServer;
import it.ascia.ais.SerialTransport;
import it.ascia.ais.TCPSerialTransport;
// import it.ascia.eds.ConfigurationFile;
import it.ascia.eds.EDSConnector;
import it.ascia.eds.EDSException;
import it.ascia.eds.device.BMC;
import it.ascia.eds.device.BMCChronoTerm;
import it.ascia.eds.device.BMCComputer;
import it.ascia.eds.device.BMCDimmer;
import it.ascia.eds.device.BMCStandardIO;


import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * @author arrigo
 * 
 */
public class BusTest extends MyController {
	static BMCComputer bmcComputer;
	static HTTPServer server;
	static BusTest busController;
	
	
	static void makeVirtualBMC(int address, EDSConnector connector) {
		// Lo creiamo noi il BMC!
		try {
			BMCStandardIO bmc = new BMCStandardIO(address, 88, "BMCFinto"); 
			connector.addDevice(bmc);
			bmc.makeSimulated();
		} catch (EDSException e) {
			System.err.println(e.getMessage());
			System.exit(-1);
		}
	}
	
	static void testBMCStandardIO(int address, Connector connector) {
		int porta, valore;
 		// Prova su BMC modello 88, indirizzo 3
 		BMCStandardIO bmc = 
 			(BMCStandardIO)connector.getDevices(String.valueOf(address))[0];
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
	
	static void testBMCDimmer(Connector connector) {
		String address = "5";
		int output = 0, value = 0;
 		// Prova su BMC modello 88, indirizzo 3
		BMCDimmer bmc = (BMCDimmer)connector.getDevices(address)[0];
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
 		 		System.out.println(bmc.getStatus("*", 0));
 			} // if output >= 0
		}
	}
	
	static void testBMCChronoTerm(Connector connector) {
		String address = "7";
		double setPoint = 0;
 		// Prova su BMC modello 127, indirizzo 7
		BMCChronoTerm bmc = (BMCChronoTerm)connector.getDevices(address)[0];
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
 		 		System.out.println(bmc.getStatus("*", 0));
 			} // if output >= 0
		}
		bmc.printStatus();
		bmc.updateStatus();
		bmc.printStatus();
	}
	
	/**
	 * @param args
	 *            porta seriale
	 * @throws AISException 
	 */
	public static void main(String[] args) throws AISException {

		// TODO inizializzazione da file di configurazione
		
	    // Inizializzazione logger
	    PropertyConfigurator.configure("conf/log4j.conf");

		busController = new BusTest(null /* "1" */);
		busController.loadPlugin("Default");
		// TODO caricare dati da files di configurazione
		// TODO fare EDSControllerPlugin 
		busController.loadPlugin("EDS");
		// TODO fare BentelControllerPlugin 
		busController.loadPlugin("Bentel");
		// TODO fare HTPSServerControllerPlugin 
		busController.loadPlugin("HTTPServer");

		//String defaultPort = "ascia.homeip.net";
		String defaultPort = "COM1";
		Integer tcpPort = null;
		int httpPort = 80;
		String documentRoot = "../aui";
		//ConfigurationFile cfgFile = null;
		Logger log = Logger.getLogger(BusTest.class);
	 	if (args.length > 0) {
	 		for (int i = 0; i < args.length; i++) {
				log.debug("Parametro "+i+"="+args[i]);
			}
		    if (args[0].contains(":")) {
		    	String[] s2 = args[0].split(":",2);
		    	defaultPort = s2[0];
		    	tcpPort = new Integer(Integer.parseInt(s2[1]));
		    	log.info("Connessione al BUS tramite TCP/IP su "+defaultPort+":"+tcpPort);
		    } else {
		    	defaultPort = args[0];
		    	log.info("Connessione al BUS tramite seriale "+defaultPort);
		    }
		    if (args.length > 1) {
		    	httpPort = Integer.parseInt(args[1]);
		    }
	    	log.info("Server HTTP su porta "+httpPort);
		    if (args.length > 2) {
		    	documentRoot = args[2];
		    }
	    	log.info("Cartella files http "+documentRoot);
		}
	 	EDSConnector eds = null;
	 	Transport transport = null;
	 	try {
	 		eds = new EDSConnector("EDSConnector0");
	 		if (tcpPort != null) {
	 			transport = new TCPSerialTransport(defaultPort, tcpPort.intValue());
	 			log.info("Connesso via socket a "+defaultPort+" porta "+tcpPort);
	 		} else {
	 			transport = new SerialTransport(defaultPort);
	 			log.info("Connesso via seriale a "+defaultPort);
	 		}
	 		eds.bindTransport(transport);
	 		//transport.bind(eds);
	 	} catch (EDSException e) {
	 		log.fatal(e.getMessage());
	 		System.exit(-1);
	 	}
		busController.registerConnector(eds);
		try {
			if (httpPort > 0) {
				server = new HTTPServer(httpPort, busController, documentRoot);
			}
		} catch (AISException e) {
			System.err.println(e.getMessage());
			System.exit(-1);
		}
	 	bmcComputer = new BMCComputer(0);
	 	eds.setBMCComputer(bmcComputer);
	 	// File di configurazione
//	 	try {
//			cfgFile = new ConfigurationFile("conf/tavola20071207.xml");
//		} catch (EDSException e) {
//			System.err.println(e.getMessage());
//			System.exit(-1);
//		}
		// cfgFile.createBMCs(transport);
		//System.out.println(cfgFile.getSystemName());
	 	// Discovery
	 	log.info("Discovery su "+eds.getName() + " tramite " + transport);
	 	for (int i = 0; i < 8; i++) {
	 		if ((i != 1) && (i != 4)) {
	 			// Evitiamo gli indirizzi non assegnati
	 			BMC bmc = bmcComputer.discoverBMC(i); 
	 			if (bmc != null) {
	 				log.info("Indirizzo "+i+" : "+bmc.getInfo());
	 			} else {
	 				log.debug("Nessun BMC all'indirizzo "+i);
	 			}
	 		}
	 	}
	 	// Dimmer avanzato
	 	bmcComputer.discoverBMC(255);
	 	// BMC virtuale
	 	/* for (int i = 64; i < 104; i++) {
	 		makeVirtualBMC(i);
	 	} */
	 	// testBMCStandardIO();
	 	// testBMCDimmer();
	 	// testBMCChronoTerm();
	 	/*
	 	try {
			busController.setDevicesListener();
		} catch (AISException e) {
			System.err.println(e.getMessage());
			System.exit(-1);
		}
		*/
	 	// La palla all'utente
		int dest = 1;
		while (dest > 0) {
			dest = Stdio.inputInteger("Indirizzo da pingare (0 per terminare):");
				if (dest > 0) {
					BMC bmc = bmcComputer.discoverBMC(dest); 
					if ( bmc != null) {
						System.out.println(bmc.getInfo());
					} else {
						System.out.println("Non trovato!");
					}
				}
		}
		if (server != null) server.close();
		transport.close();
		log.info("Termine programma");
	}
	
	public BusTest(String pin) {
		super(pin);
	}
}
