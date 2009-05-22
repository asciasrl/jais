package it.ascia.ais;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class CmdControllerModule extends ControllerModule {

	protected CmdConsole cmd = null;
	
	public void start() {
 		cmd = new CmdConsole();
 		cmd.setName("CmdConsole");
 		running = true;
 		cmd.start();
 		logger.info("Completato start");

	}

	public void stop() {
		running = false;
		if (cmd != null) {
			cmd.interrupt();
		}
	}
	
	/**
	 * Fornisce alcune funzioni utili per l'input/output da terminale.
	 * 
	 * <p>Java e' tanto un bel linguaggio, ma fare input da terminale e' 
	 * difficle.</p>
	 * 
	 * @author arrigo
	 */
	public class Stdio {

		private BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));;
		
		/**
		 * Richiede un intero.
		 * 
		 * @param message messaggio da mostrare.
		 * 
		 * @return il valore inserito dall'utente.
		 */
		int inputInteger(String message) {
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

		/**
		 * Richiede l'inserimento di un numero double.
		 * 
		 * @param message messaggio da mostrare.
		 * 
		 * @return il numero inserito.
		 */
		double inputDouble(String message) {
			double retval = 0;
			boolean entered = false;
			while (!entered) {
				try {
					System.out.print(message);
					retval = Double.parseDouble(stdin.readLine());
					entered = true;
				} catch (NumberFormatException e) {
					// Inserito un input invalido. Lo ignoriamo.
				} catch (IOException e) {
				}
			}
			return retval;
		}
		
		String inputString(String message) {
			System.out.print(message);
			try {
				return stdin.readLine();
			} catch (IOException e) {
				return "";
			}
		}
	}
	
	private class CmdConsole extends Thread {
		
		public void run() {
		 	// La palla all'utente
			Stdio stdio = new Stdio();
			while (running) {
				String dest = stdio.inputString("Indirizzo dispositivo (<n>|stop|restart):");
				if (dest.equals("restart")) {
					controller.restart();
					continue;
				}
				if (dest.equals("stop")) {
					controller.stop();
					continue;
				}
				Device[] devices = null;
				try {
					devices = controller.findDevices(dest);
				} catch (Exception e) {
					devices = new Device[0];
				}					
				if ( devices.length == 0) {
					System.out.println("Device non trovato!");
					continue;
				}
				if (devices.length > 1 ){
					System.out.println("Elenco devices:");
					for (int i = 0; i < devices.length; i++) {
						Device device = devices[i];							
						System.out.println(device.getFullAddress());
					}
					continue;
				} 
				Device d = devices[0];
				while (d != null) {
		 			System.out.println(d.getInfo());
		 			System.out.println(d.getStatus());
					String portId = stdio.inputString("Porta di "+d.getFullAddress()+" (invio per cambiare device): ");
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
			 				newValue = stdio.inputString("Nuovo valore per "+pn+" (invio per non variare): ");
				 			if (! newValue.equals("")) {
				 				d.writePortValue(portId,newValue);
				 				//p.writeValue(newValue);
				 			}
			 			}
		 			}
				}
			}
		}
	}

}
