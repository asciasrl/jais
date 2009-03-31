

import it.ascia.ais.AISException;
import it.ascia.ais.Controller;
import it.ascia.ais.Device;
import it.ascia.eds.device.BMCRegT22;

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

	    // Inizializzazione: tutto in base al file di configurazione
		busController = Controller.getController();
		busController.configure();
		busController.start();
		
	 	// La palla all'utente
		while (true) {
			String dest = Stdio.inputString("Indirizzo dispositivo (invio per terminare programma):");
			if (dest.equals("restart")) {
				busController.stop();
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				busController.start();
			} else if (dest.equals("")) {
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
					 				d.writePort(portId, newValue);
					 			}
				 			}
			 			}
					}
				}
			}
		}

		busController.stop();
		System.out.println("Termine programma");
	}

	public void resetRegT22(String address) throws AISException {
		Device reg = Controller.getController().getDevice(address);
		for (int stagione = 0; stagione <= 1; stagione++) {
			for (int giorno = 0; giorno <= 6; giorno++) {
				for (int ora = 0; ora <= 23; ora++) {
					reg.writePort(BMCRegT22.getSetPointPortId(stagione,giorno,ora), new Double(5.5*stagione+10+ora-giorno));					
				}
			}		
		}
	}
}
