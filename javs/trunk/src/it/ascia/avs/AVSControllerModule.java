package it.ascia.avs;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.configuration.HierarchicalConfiguration;

import it.ascia.ais.ControllerModule;
import it.ascia.ais.Transport;

public class AVSControllerModule extends ControllerModule {

	@SuppressWarnings("unchecked")
	public void start() {
		super.start();
		List<HierarchicalConfiguration> connectors = getConfiguration().configurationsAt("connectors.connector");
		for (Iterator<HierarchicalConfiguration> c = connectors.iterator(); c.hasNext();)
		{
		    HierarchicalConfiguration sub = c.next();
		 	AVSConnector connector = null;
		 	try {
	 			connector = new AVSConnector(sub.getString("name"),sub.getString("interfaccia","EasyLink"),sub.getString("centrale","Advance88"));
			 	Transport transport = Transport.createTransport(sub);		 		
		 		// associa transport e connector 
		 		connector.addTransport(transport);
		 		//myConnectors.add(eds);
		 		connector.setModule(this);
			 	// registra il connector
				controller.addConnector(connector);		
		 	} catch (Exception e) {
		 		logger.fatal("Errore durante inizializzazione:",e);
		 		if (connector != null) {
		 			connector.close();
		 		}
		 	}
		}				
 		logger.info("Completato start");
	}
	
}
