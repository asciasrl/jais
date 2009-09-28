package it.ascia.ais;

import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

public abstract class BUSControllerModule extends ControllerModule {

	protected List myConnectors = new Vector();
	
	protected AutoUpdater autoUpdater = null;
	
	public void start() {
 		int autoupdate = getConfiguration().getInt("autoupdate",1000);
 		autoUpdater = new AutoUpdater(autoupdate);
 		autoUpdater.setName("AutoUpdater-"+getName());
 		autoUpdater.start();
		super.start();
	}
	
	/**
	 * Chiude tutti i connettori
	 */
	public void stop() {
		if (autoUpdater != null) {
			autoUpdater.interrupt();
			try {
				autoUpdater.join();
			} catch (InterruptedException e) {
				logger.debug("Interrotto:",e);
			}
		}
		for (Iterator c = myConnectors.iterator(); c.hasNext();)
		{
			Connector connector = (Connector) c.next(); 
			logger.debug("Chiusura connettore "+connector.getName());
			connector.close();
			logger.trace("Chiuso connettore "+connector.getName());
		}
		super.stop();
	}

	public class AutoUpdater extends Thread {
		
		long autoupdate = 0;
		
		private boolean running = false;
		
		public void start() {
			running = true;
			super.start();
		}
		
		public void interrupt() {
			running = false;
			super.interrupt();
		}

		/**
		 * Aggiorna automaticamente i device connessi
		 * @param a Tempo di attesa in mS fra una richiesta di aggiornamento e la successiva; se = 0 l'aggiornamento NON viene effettuato
		 */
		public AutoUpdater(long a) {
			autoupdate = a;
		}

		/**
		 * Esegue periodicamente la richiesta di aggiornamento delle porte.
		 * Vengono scandite tutte le porte di tutti i connettori e se una di
		 * queste deve essere aggiornata, viene invocato il metodo updatePort di
		 * Device
		 */
		public void run() {
			if (autoupdate > 0) {
				logger.info("Autoupdate ogni "+autoupdate+"mS");
				boolean skipautoupdate = getConfiguration().getBoolean("skipautoupdate",false);
				if (skipautoupdate) {
					logger.info("EXPERIMENTAL Skip AutoUpdate without listener");
				}
				running = true;
				while (running) {
					try {
						synchronized (this) {
							wait(autoupdate);							
						}
						//logger.trace("Autoupdate");
						for (Iterator c = myConnectors.iterator(); c.hasNext();)
						{
							Connector connector = (Connector) c.next();
							HashMap devices = connector.getDevices();
							for (Iterator iterator = devices.values().iterator(); iterator.hasNext();) {
								Device device = (Device) iterator.next();
								if (device.isUnreachable()) {
									break;
								}
								DevicePort[] deviceports = device.getPorts();
								for (int i = 0; i < deviceports.length; i++) {
									DevicePort devicePort = deviceports[i];
									if (devicePort.isDirty() || devicePort.isExpired()) {
										if (!devicePort.hasListeners()) {
											// TODO Experimental
											if (skipautoupdate) {
												continue;
											}
										}
										if (device.isUnreachable()) {
											break;
										}
										if (devicePort.isQueuedForUpdate()) {
											continue;
										}
										connector.queueUpdate(devicePort);
									}
								}
							}
						}
					} catch (InterruptedException e) {
						logger.debug("Interrotto.");
					} catch (ConcurrentModificationException e) {
						logger.debug("Modifica concorrente, riavvio scansione dispositivi.");
					} catch (Exception e) {
						logger.error("Eccezione:",e);
					}
				}
				logger.debug("Stop.");
			} else {
				logger.error("Autoupdate non attivo");			
			}
		}
		
	}

}
