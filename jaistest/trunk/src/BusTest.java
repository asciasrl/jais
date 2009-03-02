

import it.ascia.ais.AISException;
import it.ascia.ais.Connector;
import it.ascia.ais.HTTPServer;
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

	    // Inizializzazione logger
	    PropertyConfigurator.configure("conf/log4j.conf");
	    Logger log = Logger.getLogger(BusTest.class);
	    
		busController = new BusTest(null);
		busController.configure();
		
	 	// La palla all'utente
	 	EDSConnector eds = (EDSConnector) busController.getConnector("0");
	 	bmcComputer = eds.getBMCComputer();
		int dest = 1;
		while (dest > 0) {
			dest = Stdio.inputInteger("Indirizzo da pingare (0 per terminare):");
				if (dest > 0) {
					BMC bmc = bmcComputer.discoverBMC(dest); 
					if ( bmc != null) {
						System.out.println(bmc.getInfo());
						testBMCStandardIO(dest,bmcComputer.getConnector());
					} else {
						System.out.println("Non trovato!");
					}
				}
		}
		// TODO chiusura del controller
		log.info("Termine programma");
	}
	
	public BusTest(String pin) {
		super(pin);
	}
}
