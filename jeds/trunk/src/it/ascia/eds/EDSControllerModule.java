package it.ascia.eds;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.configuration.HierarchicalConfiguration;

import it.ascia.ais.AISException;
import it.ascia.ais.ControllerModule;
import it.ascia.ais.Transport;
import it.ascia.eds.device.BMC;

public class EDSControllerModule extends ControllerModule {
    
	/**
	 * Quanto tempo aspettare la risposta dopo l'invio di un messaggio.
	 * 
	 * <p>Nel caso peggiore (1200 bps), la trasmissione di un messaggio richiede 
	 * 8 / 120 = 660 msec. In quello migliore (9600 bps), la trasmissione 
	 * richiede 82 msec. Questa costante deve tener conto del caso migliore.</p>
	 * 
	 * Questo valore si puo' modificare nel file di configurazione:
	 * /jais/EDS/retrytimeout
	 */
	protected int RETRY_TIMEOUT = 300;
	
	@SuppressWarnings("unchecked")
	public void start() {
		List connectors = getConfiguration().configurationsAt("connectors.connector");
		for (Iterator c = connectors.iterator(); c.hasNext();)
		{
		    HierarchicalConfiguration sub = (HierarchicalConfiguration) c.next();
		    if (sub.getBoolean("[@disabled]",false)) {
		    	logger.debug("Connector disabled: "+sub.getString("name"));
		    	continue;
		    }
		 	EDSConnector eds = null;
		 	try {
		 		// autoupdate sia per connettore che per modulo
		 		eds = new EDSConnector(sub.getLong("autoupdate",getConfiguration().getLong("autoupdate",1000)),
		 				sub.getString("name"),
		 				sub.getInt("computer",250),
		 				sub.getBoolean("discovernew", true),
		 				sub.getInt("retrytimeout", RETRY_TIMEOUT));
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
			 	// registra il connector
				controller.addConnector(eds);		
				// carica il file di configurazione
			 	String configFileName = sub.getString("config",null);
			 	if (configFileName != null) {
			 		eds.loadConfig(configFileName);
			 	}				
				List devices = sub.configurationsAt("devices.device");
				for (Iterator d = devices.iterator(); d.hasNext();)
				{
					HierarchicalConfiguration dev = (HierarchicalConfiguration) d.next();
					String model = dev.getString("model");
					String address = dev.getString("address");
					int revision = dev.getInt("revision",0);
					String name = dev.getString("name",null);
					try {
						BMC bmc = eds.addBmc(model,address,revision,name);						
						if (bmc != null) {
							bmc.discover();
						}
					} catch (Exception e) {
						logger.warn("addBMC Model: "+model+" Address: "+address+" Error: ",e);
					}
				}
		 	} catch (Exception e) {
		 		if (eds != null) {
		 			eds.stop();
		 		}
		 		stop();
				throw(new AISException("Errore avvio connettore: ",e));		 		
		 	}
		}				
 		logger.info("Completato start");
		super.start();
	}
	
}
