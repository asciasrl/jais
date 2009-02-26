package it.ascia.ais;

import org.apache.commons.configuration.XMLConfiguration;

public class HTTPServerControllerPlugin extends ControllerPlugin {

	public HTTPServer server;
	
	public void configure(XMLConfiguration config) {
		int port = config.getInt("HTTPServer.port");
		String root = config.getString("HTTPServer.root");
		try {
			server = new HTTPServer(port, controller, root);
		} catch (AISException e) {
			// TODO Minimal catch block
			logger.fatal(e);
		}
	}

	public void onDeviceEvent(DeviceEvent event) {
		// TODO Auto-generated method stub

	}

}
