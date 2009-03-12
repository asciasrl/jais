

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
			String dest = Stdio.inputString("Indirizzo dispositivo (invio per terminare programma):");
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
			 			System.out.println(d.getStatus());
						String portId = Stdio.inputString("Porta di "+d.getFullAddress()+" (invio per cambiare device): ");
			 			if (portId.equals("")) {
			 				d = null;
			 			} else {
			 				String pn = null;
			 				try {
					 			pn = d.getPortName(portId);								
							} catch (AISException e) {								
							}
				 			if (pn == null) {
				 				System.out.println("Il device "+d.getFullAddress()+" hon ha la porta "+portId);
				 			} else {
				 				String newValue = "";
				 				newValue = Stdio.inputString("Nuovo valore per "+pn+" (invio per non variare): ");
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
