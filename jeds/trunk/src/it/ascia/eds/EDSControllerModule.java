package it.ascia.eds;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.configuration.HierarchicalConfiguration;

import it.ascia.ais.AISException;
import it.ascia.ais.ControllerModule;
import it.ascia.ais.Transport;
import it.ascia.eds.device.BMC;

public class EDSControllerModule extends ControllerModule {
	
	@SuppressWarnings("unchecked")
	public void start() {
		super.start();
		List connectors = getConfiguration().configurationsAt("connectors.connector");
		for (Iterator c = connectors.iterator(); c.hasNext();)
		{
		    HierarchicalConfiguration sub = (HierarchicalConfiguration) c.next();
		 	EDSConnector eds = null;
		 	try {
		 		// autoupdate sia per connettore che per modulo
		 		eds = new EDSConnector(sub.getLong("autoupdate",getConfiguration().getLong("autoupdate",1000)),sub.getString("name"),sub.getInt("computer",250));
			 	Transport transport = Transport.createTransport(sub);		 		
		 		// associa transport e connector 
		 		eds.addTransport(transport);
		 		eds.setModule(this);
			 	// effettua il discovery
			 	List discover = sub.getList("discover",null);
			 	if (discover == null) {
			 		logger.debug("Nessun dispositivo da ricercare (discover)");
			 	} else {
					for(Iterator i = discover.iterator(); i.hasNext();) {
						try {
							eds.discoverBMC(new Integer((String) i.next()).intValue());						
						} catch (NumberFormatException e) {
							logger.error("Indirizzo dispositivo da cercare non corretto: "+e.getMessage());
						}
					}
			 	}
				// carica il file di configurazione
			 	String configFileName = sub.getString("config",null);
			 	if (configFileName != null) {
			 		eds.loadConfig(configFileName);
			 	}				
			 	// registra il connector
				controller.addConnector(eds);		
				List devices = sub.configurationsAt("devices.device");
				for (Iterator d = devices.iterator(); d.hasNext();)
				{
					HierarchicalConfiguration dev = (HierarchicalConfiguration) d.next();
					String model = dev.getString("model");
					String address = dev.getString("address");
					try {
						BMC bmc = eds.addBmc(model,address);						
						if (bmc != null) {
							bmc.discover();
						}
					} catch (AISException e) {
						logger.warn("addBMC Model: "+model+" Address: "+address+" Error: ",e);
					}
				}
		 	} catch (Exception e) {
		 		logger.fatal("Errore durante inizializzazione:",e);
		 		if (eds != null) {
		 			eds.close();
		 		}
		 	}
		}				
 		logger.info("Completato start");
	}
	
}
