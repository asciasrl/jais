package it.ascia.ais;

import org.apache.commons.configuration.XMLConfiguration;

public class DefaultControllerModule extends ControllerModule {

	public void onDeviceEvent(DeviceEvent event) {
		logger.info("Ricevuto evento: "+event.getInfo());
	}

	public void configure(XMLConfiguration config) {
		// TODO Auto-generated method stub
		
	}

}
