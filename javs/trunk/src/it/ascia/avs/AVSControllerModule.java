package it.ascia.avs;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.configuration.HierarchicalConfiguration;

import it.ascia.ais.AISException;
import it.ascia.ais.BUSControllerModule;
import it.ascia.ais.Transport;

public class AVSControllerModule extends BUSControllerModule {

	public void start() {
		super.start();
		List<HierarchicalConfiguration> connectors = getConfiguration().configurationsAt("connectors.connector");
		for (Iterator<HierarchicalConfiguration> c = connectors.iterator(); c.hasNext();)
		{
		    HierarchicalConfiguration sub = c.next();
		 	EasyLinkConnector connector = null;
		 	try {
		 		if (sub.getString("interfaccia","EasyLink").equals("EasyLink")) {
		 			connector = new EasyLinkConnector(sub.getString("name"),sub.getString("centrale","Advance88"));
		 		} else {
		 			throw(new AISException("Unsupported interface: "+sub.getString("interfaccia")));
		 		}
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
