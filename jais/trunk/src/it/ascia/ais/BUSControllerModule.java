package it.ascia.ais;

import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

public abstract class BUSControllerModule extends ControllerModule {

	protected List myConnectors = new Vector();
	
	protected AutoUpdater autoUpdater = null;

	protected boolean running;

	/**
	 * Chiude tutti i connettori
	 */
	public void stop() {	
		running = false;
		if (autoUpdater != null) {
			autoUpdater.interrupt();
		}
		for (Iterator c = myConnectors.iterator(); c.hasNext();)
		{
			Connector connector = (Connector) c.next(); 
			logger.info("Chiusura connettore "+connector.getName());
			connector.close();
		}
	}

	public class AutoUpdater extends Thread {
		
		long autoupdate = 0;

		public AutoUpdater(long a) {
			autoupdate = a;
		}

		/**
		 * Aggiorna automaticamente i device connessi
		 * @see Thread.run()
		 */
		public void run() {
			if (autoupdate > 0) {
				logger.info("Autoupdate ogni "+autoupdate+"mS");				
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
							for (Iterator iterator = devices.values().iterator(); iterator
									.hasNext();) {
								Device device = (Device) iterator.next();
								DevicePort[] deviceports = device.getPorts();
								for (int i = 0; i < deviceports.length; i++) {
									DevicePort devicePort = deviceports[i];
									if (devicePort.isDirty() || devicePort.isExpired()) {
										logger.debug("AutoUpdate "+devicePort.getFullAddress());
										try {											
											device.updatePort(devicePort.getPortId());
											synchronized (this) {
												wait(autoupdate);							
											}
										} catch (AISException e) {
											logger.warn("Errore durante updatePort:",e);
										}
									}
								}
							}
						}
					} catch (InterruptedException e) {
						logger.debug("Interrotto.");
					} catch (ConcurrentModificationException e) {
						logger.debug("Modifica concorrente");
					} catch (Exception e) {
						logger.error("Eccezione:",e);
					}
				}
				logger.debug("Stop.");
			} else {
				logger.info("Autoupdate non attivo");				
			}
		}
		
	}

}
