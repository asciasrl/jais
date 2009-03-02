package it.ascia.aui;

import org.apache.commons.configuration.HierarchicalConfiguration;

import it.ascia.ais.ControllerModule;

public class AUIControllerModule extends ControllerModule {

	public HierarchicalConfiguration getConfig() {
		HierarchicalConfiguration cfg = controller.getConfig().configurationAt("AUI");
		return cfg;
	}

}
