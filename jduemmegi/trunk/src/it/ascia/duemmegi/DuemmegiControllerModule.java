package it.ascia.duemmegi;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.configuration.HierarchicalConfiguration;

import it.ascia.ais.ControllerModule;
import it.ascia.ais.Transport;

public class DuemmegiControllerModule extends ControllerModule {
	
	public void start() {
		HierarchicalConfiguration config = getConfiguration();
		List connectors = config.configurationsAt("connectors.connector");
		for (Iterator c = connectors.iterator(); c.hasNext();)
		{
		    HierarchicalConfiguration sub = (HierarchicalConfiguration) c.next();
		 	DFCPConnector conn = null;
		 	try {
		 		conn = new DFCPConnector(config.getLong("autoupdate",1000),sub.getString("name"),this);
			 	Transport transport = Transport.createTransport(sub);		 		
		 		// associa transport e connector 
		 		conn.addTransport(transport);
			 	// registra il connector
				controller.addConnector(conn);		
				// aggiunta devices
				List devices = sub.configurationsAt("devices.device");
				for (Iterator d = devices.iterator(); d.hasNext();)
				{
					HierarchicalConfiguration dev = (HierarchicalConfiguration) d.next();
					conn.addDevice(dev.getString("model"),dev.getString("address"));
				}
		 	} catch (Exception e) {
		 		logger.fatal("Errore durante inizializzazione:",e);
		 		conn.close();
		 	}
		}				
 		logger.info("Completato start");
 		super.start();
	}

}
