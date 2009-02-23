package it.ascia.ais;

import org.mortbay.log.Log;

public class DefaultControllerPlugin extends ControllerPlugin {

	public void onDeviceEvent(DeviceEvent event) {
		// TODO Auto-generated method stub
		Log.info(event.getInfo());
	}

}
