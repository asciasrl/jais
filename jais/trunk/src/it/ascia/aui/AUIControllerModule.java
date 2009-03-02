package it.ascia.aui;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;

import it.ascia.ais.ControllerModule;
import it.ascia.ais.DeviceEvent;

public class AUIControllerModule extends ControllerModule {

	public void configure(XMLConfiguration config) {
		// TODO Auto-generated method stub
		this.config = config;
	}
	
	public void onDeviceEvent(DeviceEvent event) {
		// TODO Auto-generated method stub

	}

	public HierarchicalConfiguration getConfig() {
		HierarchicalConfiguration cfg = controller.getConfig().configurationAt("AUI");
		return cfg;
	}    

}
