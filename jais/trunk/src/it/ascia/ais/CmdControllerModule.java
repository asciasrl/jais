package it.ascia.ais;

import java.io.BufferedReader;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.channels.Channels;
import java.util.Collection;
import java.util.Vector;

public class CmdControllerModule extends ControllerModule {

	protected CmdConsole cmd = null;

	public void start() {
 		cmd = new CmdConsole();
 		cmd.setName("CmdConsole");
 		cmd.start();
 		super.start();
	}

	public void stop() {
 		super.stop();
		if (cmd != null) {
			cmd.interrupt();
			try {
				cmd.join();
			} catch (InterruptedException e) {
			}
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

		private BufferedReader stdin;
		
		public Stdio() {
			//stdin = new BufferedReader(new InputStreamReader(System.in));
			stdin = new BufferedReader(
		            new InputStreamReader(
		                    Channels.newInputStream(
		                    (new FileInputStream(FileDescriptor.in)).getChannel())));

		}
		
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
			} catch (Exception e) {
				logger.warn("exception:",e);
				return null;
			}
		}
	}
	
	private class CmdConsole extends Thread {
		
		private boolean running;

		public void interrupt() {
			running = false;
			logger.debug("Interrupting");
			super.interrupt();
		}
		
		public void run() {
		 	// La palla all'utente
			Stdio stdio = new Stdio();
			running = true;
			while (running) {
				System.out.println("JAIS Server");
				String dest = stdio.inputString("Indirizzo dispositivo (<n>|stop|restart):");
				if (dest == null || dest.length() == 0) {
					continue;
				}
				if (dest.equals("restart")) {
					controller.restart();
					continue;
				}
				if (dest.equals("stop")) {
					controller.stop();
					continue;
				}
				Collection<Device> devices = null;
				try {
					devices = controller.getDevices(new Address(dest));
				} catch (Exception e) {
					devices = new Vector<Device>();
				}					
				if ( devices.size() == 0) {
					System.out.println("Device non trovato!");
					continue;
				}
				if (devices.size() > 1 ){
					System.out.println("Elenco devices:");
					for (Device device : devices) {
						System.out.println(device.getAddress());
					}
					continue;
				} 
				Device d = (Device) devices.toArray()[0];
				while (d != null) {
		 			System.out.println(d.getInfo());
		 			System.out.println(getStatus(d));
					String portId = stdio.inputString("Porta di "+d.getAddress()+" (invio per cambiare device): ");
		 			if (portId.equals("")) {
		 				d = null;
		 			} else {
		 				DevicePort p = null;
		 				try {
				 			p = d.getPort(portId);								
						} catch (AISException e) {								
						}
			 			if (p == null) {
			 				System.out.println("Il device "+d.getAddress()+" hon ha la porta "+portId);
			 			} else {
			 				String newValue = "";
			 				newValue = stdio.inputString("Nuovo valore per "+p.getAddress()+" (invio per non variare): ");
				 			if (! newValue.equals("")) {
				 				p.writeValue(newValue);
				 			}
			 			}
		 			}
				}
			}
		}

		private String getStatus(Device d) {
			StringBuffer s = new StringBuffer();
			for (DevicePort devicePort : d.getPorts()) {
				s.append(" "+devicePort.getAddress());
				s.append(" ("+devicePort.getClass().getSimpleName()+")");
				s.append("="+devicePort.getCachedValue());
				s.append('\n');
			}
			return s.toString();
		}
	}
	
	

}
