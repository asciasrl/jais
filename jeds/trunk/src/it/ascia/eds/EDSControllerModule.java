package it.ascia.eds;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.configuration.HierarchicalConfiguration;

import it.ascia.ais.AISException;
import it.ascia.ais.BUSControllerModule;
import it.ascia.ais.SerialTransport;
import it.ascia.ais.TCPSerialTransport;
import it.ascia.ais.Transport;

public class EDSControllerModule extends BUSControllerModule {
	
	public void start() {
		List connectors = config.configurationsAt("connectors.connector");
		for (Iterator c = connectors.iterator(); c.hasNext();)
		{
		    HierarchicalConfiguration sub = (HierarchicalConfiguration) c.next();
		 	EDSConnector eds = null;
		 	Transport transport = null;
		 	try {
		 		eds = new EDSConnector(sub.getString("name"),controller);
		 		eds.setAddress(sub.getInt("computer",250));
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
			 	List discover = sub.getList("discover",null);
			 	if (discover == null) {
			 		logger.debug("Nessun dispositivo da ricercare (discover)");
			 	}
				for(Iterator i = discover.iterator(); i.hasNext();) {
					try {
						eds.discoverBMC(new Integer((String) i.next()).intValue());						
					} catch (NumberFormatException e) {
						logger.error("Indirizzo dispositivo da cercare non corretto: "+e.getMessage());
					}
				}
			 	// registra il connector
				controller.registerConnector(eds);		
				myConnectors.add(eds);
		 	} catch (Exception e) {
		 		logger.fatal("Errore durante inizializzazione:",e);
		 	}
		}				
 		int autoupdate = config.getInt("autoupdate",0);
 		autoUpdater = new AutoUpdater(autoupdate);
 		autoUpdater.setName(getClass().getSimpleName()+"-autoUpdater");
 		running = true;
 		autoUpdater.start();
 		logger.info("Completato start");
	}
	
}
