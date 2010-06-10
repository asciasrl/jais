package it.ascia.bentel;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.configuration.HierarchicalConfiguration;

import it.ascia.ais.ControllerModule;
import it.ascia.ais.Transport;

public class BentelKyoControllerModule extends ControllerModule {
	
	public void start() {
		HierarchicalConfiguration config = getConfiguration();
		List<HierarchicalConfiguration> connectors = config.configurationsAt("connectors.connector");
		for (Iterator<HierarchicalConfiguration> c = connectors.iterator(); c.hasNext();)
		{
		    HierarchicalConfiguration sub = c.next();
		    BentelKyoConnector connector = null;
		 	try {
		 		connector = new BentelKyoConnector(sub.getString("name"));
		 		if (connector.getName().equals("rw")) {
			 		//continue;
			 	}
			 	Transport transport = Transport.createTransport(sub);
			 	connector.addTransport(transport);
			 	connector.discoverPanel();
			 	// prima ?
			 	controller.addConnector(connector);
		 	} catch (Exception e) {
		 		logger.fatal("Errore durante inizializzazione:",e);
		 	}
		}				

		controller.getConnector("rw").close();
		((BentelKyoConnector) controller.getConnector("ro")).log("inizio:");
		JBisKyoUnit b = new JBisKyoUnit(6,4,"0001" + (char)0xff + (char)0xff, "jbis");			
		JBisKyoDevice d = b.getDevice();
 		for (int i = 0; i < 100; i++) {
 			((BentelKyoConnector) controller.getConnector("ro")).log("Prima:");
	 		//logger.info("Lettura:");
			try {
				d.partition((byte)0xff,(byte)0);
			} catch (Exception e) {
				e.printStackTrace();
			}
			((BentelKyoConnector) controller.getConnector("ro")).log("Dopo:");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
 		}

 		logger.info("Completato start");
 		super.start();
	}

}
