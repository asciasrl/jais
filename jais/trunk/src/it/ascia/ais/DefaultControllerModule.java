package it.ascia.ais;

public class DefaultControllerModule extends ControllerModule {

	public void onDeviceEvent(DeviceEvent event) {
		logger.info("Ricevuto evento: "+event.getInfo());
	}

}
