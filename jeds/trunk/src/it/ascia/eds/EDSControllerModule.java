package it.ascia.eds;


import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.configuration.HierarchicalConfiguration;

import it.ascia.ais.AISException;
import it.ascia.ais.Connector;
import it.ascia.ais.ControllerModule;
import it.ascia.ais.Device;
import it.ascia.ais.DeviceEvent;
import it.ascia.ais.SerialTransport;
import it.ascia.ais.TCPSerialTransport;
import it.ascia.ais.Transport;
import it.ascia.eds.device.BMCComputer;

public class EDSControllerModule extends ControllerModule {
	
	private AutoUpdater autoupdater;

	public void onDeviceEvent(DeviceEvent event) {
		//logger.info("Ricevuto evento: "+event.getInfo());
	}

	public void start() {
		List connectors = config.configurationsAt("EDS.connectors.connector");
		for (Iterator c = connectors.iterator(); c.hasNext();)
		{
		    HierarchicalConfiguration sub = (HierarchicalConfiguration) c.next();
		 	EDSConnector eds = null;
		 	Transport transport = null;
		 	try {
		 		eds = new EDSConnector(sub.getString("name"),controller);
		 		// crea il BMC Computer		 		
				BMCComputer bmcComputer = new BMCComputer(eds,sub.getString("computer","250"));
			 	eds.setBMCComputer(bmcComputer);
		 		// attiva il transport
		 		String type = sub.getString("transport.type");
		 		if (type.equals("serial")) {
		 			String port = sub.getString("transport.port");
		 			int speed = sub.getInt("transport.speed");
		 			transport = new SerialTransport(eds,port,speed);
		 			logger.info("Connesso via seriale a "+port+" a "+speed+"bps");
		 		} else if (type.equals("tcp")) {
		 			String host = sub.getString("transport.host");
		 			int port = sub.getInt("transport.port");					
		 			transport = new TCPSerialTransport(eds,host,port);
		 			logger.info("Connesso via seriale a "+host+":"+port);
				} else {
					throw(new AISException("Transport "+type+" non riconosciuto"));
				}
		 		// associa transport e connector 
		 		eds.bindTransport(transport);
			 	// effettua il discovery
			 	List discover = sub.getList("discover");
				for(Iterator i = discover.iterator(); i.hasNext();) {
					bmcComputer.discoverBMC(new Integer((String) i.next()).intValue());
				}
			 	// registra il connector
				controller.registerConnector(eds);		
				myConnectors.add(eds);
		 	} catch (AISException e) {
		 		logger.fatal(e.getMessage());
		 	}
		}				
 		int autoupdate = config.getInt("EDS.autoupdate",0);
 		autoupdater = new AutoUpdater(autoupdate);
 		autoupdater.start();
 		logger.info("Completato start");
	}

	public String doCommand(String command, HashMap params) {
		// TODO Auto-generated method stub
		return null;
	}
	
	protected class AutoUpdater extends Thread {
		
		int autoupdate = 0;

		public AutoUpdater(int a) {
			autoupdate = a;
		}

		/**
		 * Aggiorna automaticamente i device connessi
		 * @see Thread.run()
		 */
		public void run() {
			if (autoupdate > 0) {
				logger.info("Autoupdate ogni "+autoupdate+" secondi.");				
				while(true) {
					try {
						Thread.sleep(1000*autoupdate);
					} catch (InterruptedException e) {
					}
					logger.trace("Autoupdate");
					for (Iterator c = myConnectors.iterator(); c.hasNext();)
					{
						Connector connector = (Connector) c.next();
						HashMap devices = connector.getDevices();
						for (Iterator iterator = devices.values().iterator(); iterator
								.hasNext();) {
							Device device = (Device) iterator.next();
							try {
								device.getStatus();
							} catch (AISException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				}
			} else {
				logger.info("Autoupdate non attivo");				
			}
		}
		
	}

	
}
