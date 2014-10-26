package it.ascia.duemmegi;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.configuration.HierarchicalConfiguration;

import it.ascia.ais.AISException;
import it.ascia.ais.ControllerModule;
import it.ascia.ais.DevicePort;
import it.ascia.ais.Transport;

public class DuemmegiControllerModule extends ControllerModule {
	
	public void start() {
		HierarchicalConfiguration config = getConfiguration();
		List connectors = config.configurationsAt("connector");
		for (Iterator c = connectors.iterator(); c.hasNext();)
		{
		    HierarchicalConfiguration sub = (HierarchicalConfiguration) c.next();
		    if (sub.getString("model","DFCP") == "DFCP") {
			 	DFCPConnector conn = null;
			 	try {
			 		conn = new DFCPConnector(sub.getString("name"),config.getInt("address",1),config.getLong("autoupdate",1000));
				 	Transport transport = Transport.createTransport(sub);		 		
			 		// associa transport e connector 
			 		conn.addTransport(transport);
				 	// registra il connector
					controller.addConnector(conn);
					// ricerca automatica
					conn.discover();				
					// aggiunta devices
					List devices = sub.configurationsAt("device");
					DominoDevice device;
					for (Iterator d = devices.iterator(); d.hasNext();)
					{
						HierarchicalConfiguration dev = (HierarchicalConfiguration) d.next();
						String model=dev.getString("[@model]");
						String address = dev.getString("[@address]");
						device = (DominoDevice) conn.getDevice(address);
						if (device == null) {
							device = conn.addDevice(model, address);
						}
						if (device == null) {
							continue;
						}
						device.setDescription(dev.getString("[@description]"));
						List<?> portsConfig = dev.configurationsAt("port");
						for (Iterator<?> portsConfigIterator = portsConfig.iterator(); portsConfigIterator.hasNext();)
						{
							HierarchicalConfiguration portConfig = (HierarchicalConfiguration) portsConfigIterator.next();
							String portId = portConfig.getString("[@id]");
							try {
								DevicePort port = device.getPort(portId);
								port.setDescription(portConfig.getString("[@description]"));
								port.setRoom(portConfig.getString("[@room]"));		
							} catch (AISException e) {
								logger.warn(e);
							}
						}
					}
					conn.start();
			 	} catch (Exception e) {
			 		logger.fatal("Errore durante inizializzazione:",e);
			 		conn.stop();
			 		throw(e);
			 	}
		    } else if (sub.getString("model") == "DFRS") {
			 	DXPConnector conn = null;
			 	try {
			 		conn = new DXPConnector(config.getLong("autoupdate",1000),sub.getString("name"),this);
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
			 		conn.stop();
			 	}
		    } else {
		    	logger.fatal("Modello di interfaccia non previsto: " + sub.getString("model"));
		    }
		}				
 		logger.info("Completato start");
 		super.start();
	}

}
