

import it.ascia.ais.AISException;
import it.ascia.ais.BusController;
import it.ascia.ais.Device;
import it.ascia.ais.HTTPServer;
import it.ascia.bentel.JBisException;
import it.ascia.bentel.JBisListener;
import it.ascia.eds.ConfigurationFile;
import it.ascia.eds.EDSException;
import it.ascia.eds.TCPSerialBus;
import it.ascia.eds.device.BMC;
import it.ascia.eds.device.BMCComputer;
import it.ascia.eds.device.BMCStandardIO;

import java.io.*;

import org.apache.log4j.PropertyConfigurator;

/**
 * @author arrigo
 * 
 */
public class AisTest {
	static it.ascia.eds.Bus bus;
	static BufferedReader stdin;
	static BMCComputer bmcComputer;
	static HTTPServer server;
	static BusController busController;
	static JBisListener alarm;
	
	static void makeVirtualBMC(int address) {
		// Lo creiamo noi il BMC!
		try {
			BMCStandardIO bmc = new BMCStandardIO(address, 88, bus, "BMCFinto"); 
			bus.addDevice(bmc);
			bmc.makeSimulated(busController);
		} catch (AISException e) {
			System.err.println(e.getMessage());
			System.exit(-1);
		}
	}
	
	static void startServer() {
		busController = new BusController(bus);
		try {
			server = new HTTPServer(8080, busController, 
					"/home/arrigo/public_html/auiFixed");
		} catch (AISException e) {
			System.err.println(e.getMessage());
			System.exit(-1);
		}
	}
	
	static void testDevice(int address) {
		String porta, valore;
 		// Prova su BMC modello 88, indirizzo 3
 		Device bmc = bus.getDevice(address);
 		System.out.println();
 		System.out.println("Prova BMC Standard I/O");
 		porta = "0";
 		try {
 			while ((porta != null) && !porta.equals("")) {
 				System.out.println("Nome della porta (\"\" esce): ");
 				porta = stdin.readLine();
 				if ((porta != null) && !porta.equals("")) {
 					System.out.println("Valore: ");
 					valore = stdin.readLine();
 					try {
 						bmc.setPort(porta, valore);
 					} catch (AISException e) {
 						System.err.println(e.getMessage());
 					}
 				}
 			} // While porta valida
 		} catch (IOException e) {
 			System.err.println(e.getMessage());
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
 		stdin = new BufferedReader(new InputStreamReader(System.in));
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
	 	try {
	 		alarm = new JBisListener("/dev/ttyUSB0", busController);
	 		// bus = new SerialBus(defaultPort, "0");
	 	} catch (JBisException e) {
	 		System.err.println(e.getMessage());
	 		System.exit(-1);
	 	}
	 	bmcComputer = new BMCComputer(0, bus);
	 	bus.setBMCComputer(bmcComputer);
	 	// File di configurazione
	 	try {
			cfgFile = new ConfigurationFile("../jeds/conf/tavola20071207.xml");
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
	 	testDevice(1);
		alarm.close();
		server.close();
		bus.close();
	}
}
