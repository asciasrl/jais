

import it.ascia.ais.AISException;
import it.ascia.ais.Connector;
import it.ascia.ais.Controller;
import it.ascia.ais.Device;


import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * @author arrigo
 * 
 */
public class BusTest {
	//static BMCComputer bmcComputer;
	static Controller busController;
	
	
	/*
	static void makeVirtualBMC(String address, EDSConnector connector) throws AISException {
		BMCStandardIO bmc = new BMCStandardIO(connector,address, 88, "BMCFinto"); 
		bmc.makeSimulated();
	}
	*/
	
	/*
	static void testBMCStandardIO(int address, Connector connector) throws AISException {
		int porta, valore;
 		// Prova su BMC modello 88, indirizzo 3
 		BMCStandardIO bmc = 
 			(BMCStandardIO)connector.getDevices(String.valueOf(address))[0];
 		System.out.println();
 		System.out.println("Prova BMC Standard I/O");
 		//System.out.println("Discovery...");
 		//bmcComputer.discoverBroadcastBindings(bmc);
 		porta = 0;
 		while ((porta >= 0) && (porta < 8)) {
 			porta = Stdio.inputInteger("Porta (<0 esce): ");
 			if (porta >= 0) {
 	 			System.out.println("Stato del BMC: ");
 				bmc.printStatus();
 				valore = -1;
 				while ((valore != 0) && (valore != 1)) {
 					valore = Stdio.inputInteger("Valore (0 o 1): ");
 				}
 				bmc.setOutPort(porta, (valore == 1));
 	 			System.out.println("Stato del BMC: ");
 				bmc.printStatus();
 				//bmc.printStatus();
 				//bmc.updateStatus();
 				//bmc.printStatus();
 				//bmc.printStatus();
 			} // if porta >= 0
 		}
 			
	}
	*/
	
	/*
	static void testBMCDimmer(Connector connector) throws AISException {
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
	*/
	
	/*
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
	*/
	
	/**
	 * @param args
	 *            porta seriale
	 * @throws AISException 
	 */
	public static void main(String[] args) throws AISException {

	    // Inizializzazione logger
	    PropertyConfigurator.configure("conf/log4j.conf");
	    Logger log = Logger.getLogger(BusTest.class);
	    
	    // Inizializzazione: tutto in base al file di configurazione
		busController = new Controller();
		busController.configure();
		busController.start();
		
	 	// La palla all'utente
		while (true) {
			String dest = Stdio.inputString("Indirizzo dispositivo (invio per uscire):");
			if (dest.equals("")) {
				break;
			} else {				
				Device[] devices = null;
				try {
					devices = busController.findDevices(dest);
				} catch (Exception e) {
					devices = new Device[0];
				}					
				if ( devices.length == 0) {
					System.out.println("Device non trovato!");
				} else if (devices.length > 1 ){
					for (int i = 0; i < devices.length; i++) {
						Device device = devices[i];							
						System.out.println(device.getFullAddress());
					}
				} else {
					Device d = devices[0];
					while (d != null) {
			 			System.out.println(d.getInfo());
						String portId = Stdio.inputString("Porta (invio per uscire): ");
			 			if (portId.equals("")) {
			 				d = null;
			 			} else {
				 			String pn = d.getPortName(portId);
				 			if (pn == null) {
				 				System.out.println("Il device "+d.getFullAddress()+" hon ha la porta "+portId);
				 			} else {
				 				String newValue = "";
				 				newValue = Stdio.inputString("Nuovo valore (invio esce): ");
					 			if (! newValue.equals("")) {
					 				d.poke(portId, newValue);
					 			}
				 			}
			 			}
					}
				}
			}
		}
		busController.stop();
		log.info("Termine programma");
	}
	
}
