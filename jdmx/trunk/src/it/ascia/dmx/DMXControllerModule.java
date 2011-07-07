package it.ascia.dmx;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.configuration.HierarchicalConfiguration;

import it.ascia.ais.ControllerModule;
import it.ascia.ais.Transport;

public class DMXControllerModule extends ControllerModule {
	
	public void start() {
		super.start();
		List connectors = getConfiguration().configurationsAt("connectors.connector");
		for (Iterator c = connectors.iterator(); c.hasNext();)
		{
		    HierarchicalConfiguration sub = (HierarchicalConfiguration) c.next();
		    if (sub.getBoolean("[@disabled]",false)) {
		    	logger.debug("Connector disabled: "+sub.getString("name"));
		    	continue;
		    }
		 	DMXConnector dmx = null;
		 	try {
		 		dmx = new DMXConnector(sub.getString("name"));
			 	Transport transport = Transport.createTransport(sub);		 		
		 		// associa transport e connector 
		 		dmx.addTransport(transport);
		 		dmx.setModule(this);
			 	// registra il connector
				controller.addConnector(dmx);
				for (int i = 1; i <= 512 ; i++) {
					dmx.addDevice(new DMXChannel(i));
				}
				for (int i = 1; i <= 171 ; i++) {
					dmx.addDevice(new DMXRGB(i));
				}
		 	} catch (Exception e) {
		 		logger.fatal("Errore durante inizializzazione:",e);
		 		if (dmx != null) {
		 			dmx.close();
		 		}
		 	}
		}				
 		logger.info("Completato start");
	}
	
}
