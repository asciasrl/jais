package it.ascia.dxp;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.configuration.HierarchicalConfiguration;

import it.ascia.ais.AISException;
import it.ascia.ais.BUSControllerModule;
import it.ascia.ais.SerialTransport;
import it.ascia.ais.TCPSerialTransport;
import it.ascia.ais.Transport;

public class DXPControllerModule extends BUSControllerModule {
	
	public void start() {
		List connectors = config.configurationsAt("connectors.connector");
		for (Iterator c = connectors.iterator(); c.hasNext();)
		{
		    HierarchicalConfiguration sub = (HierarchicalConfiguration) c.next();
		 	DXPConnector conn = null;
		 	Transport transport = null;
		 	try {
		 		conn = new DXPConnector(sub.getString("name"),controller);
		 		// attiva il transport
		 		String type = sub.getString("transport.type");
		 		if (type.equals("serial")) {
		 			String port = sub.getString("transport.port");
		 			int speed = sub.getInt("transport.speed");
		 			transport = new SerialTransport(conn,port,speed);
		 			logger.info("Connesso via seriale a "+port+" a "+speed+"bps");
		 		} else if (type.equals("tcp")) {
		 			String host = sub.getString("transport.host");
		 			int port = sub.getInt("transport.port");					
		 			transport = new TCPSerialTransport(conn,host,port);
		 			logger.info("Connesso via seriale a "+host+":"+port);
				} else {
					throw(new AISException("Transport "+type+" non riconosciuto"));
				}
		 		// associa transport e connector 
		 		conn.bindTransport(transport);
			 	// registra il connector
				controller.registerConnector(conn);		
				myConnectors.add(conn);
				// aggiunta devices
				List devices = sub.configurationsAt("devices.device");
				for (Iterator d = devices.iterator(); d.hasNext();)
				{
					HierarchicalConfiguration dev = (HierarchicalConfiguration) d.next();
					conn.addModule(dev.getString("model"),dev.getString("address"));
				}

		 	} catch (Exception e) {
		 		logger.fatal("Errore durante inizializzazione:",e);
		 	}
		}				
 		int autoupdate = config.getInt("autoupdate",0);
 		autoUpdater = new AutoUpdater(autoupdate);
 		autoUpdater.setName("AutoUpdater");
 		running = true;
 		autoUpdater.start();
 		logger.info("Completato start");
	}

}
