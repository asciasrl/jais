package it.ascia.eds;


import java.util.Iterator;
import java.util.List;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;

import it.ascia.ais.AISException;
import it.ascia.ais.ControllerModule;
import it.ascia.ais.DeviceEvent;
import it.ascia.ais.SerialTransport;
import it.ascia.ais.TCPSerialTransport;
import it.ascia.ais.Transport;
import it.ascia.eds.device.BMCComputer;

public class EDSControllerModule extends ControllerModule {

	public void onDeviceEvent(DeviceEvent event) {
		logger.info("Ricevuto evento: "+event.getInfo());
	}

	public void configure(XMLConfiguration config) {
		List connectors = config.configurationsAt("EDS.connectors.connector");
		for (Iterator c = connectors.iterator(); c.hasNext();)
		{
		    HierarchicalConfiguration sub = (HierarchicalConfiguration) c.next();
		 	EDSConnector eds = null;
		 	Transport transport = null;
		 	try {
		 		eds = new EDSConnector(sub.getString("name"),controller);
		 		// attiva il transport
		 		String type = sub.getString("transport.type");
		 		if (type.equals("serial")) {
		 			String port = sub.getString("transport.port");
		 			int speed = sub.getInt("transport.speed");
		 			transport = new SerialTransport(port,speed);
		 			logger.info("Connesso via seriale a "+port+" a "+speed+"bps");
		 		} else if (type.equals("tcp")) {
		 			String host = sub.getString("transport.host");
		 			int port = sub.getInt("transport.port");					
		 			transport = new TCPSerialTransport(host,port);
		 			logger.info("Connesso via seriale a "+host+":"+port);
				} else {
					throw(new AISException("Transport "+type+" non riconosciuto"));
				}
		 		eds.bindTransport(transport);
		 		// crea il BMC Computer		 		
				int computer = sub.getInt("computer");
				BMCComputer bmcComputer = new BMCComputer(computer);
			 	eds.setBMCComputer(bmcComputer);
			 	// effettua il discovery
			 	List discover = sub.getList("discover");
				for(Iterator i = discover.iterator(); i.hasNext();)
	 			bmcComputer.discoverBMC(new Integer((String) i.next()).intValue()); 
			 	// registra il connector
				controller.registerConnector(eds);			 	
		 	} catch (EDSException e) {
		 		logger.fatal(e.getMessage());
		 	} catch (AISException e) {
				// TODO Auto-generated catch block
		 		logger.fatal(e.getMessage());
			}
		}				
	}
	
}
