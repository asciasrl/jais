package it.ascia.ais;

import java.util.HashMap;

public class DefaultControllerModule extends ControllerModule {

	public void onDeviceEvent(DeviceEvent event) {
		logger.info("Ricevuto evento: "+event.getInfo());
	}

	public void start() {
		// TODO Auto-generated method stub
		
	}

	public void stop() {
		// TODO Auto-generated method stub
		
	}

	public String doCommand(String command, HashMap params) {
		// TODO Auto-generated method stub
		return null;
	}

}
