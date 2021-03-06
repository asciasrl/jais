package it.ascia.aui.tests;

import org.apache.commons.configuration.XMLConfiguration;

import it.ascia.ais.Controller;
import it.ascia.aui.AUIControllerModule;
import junit.framework.TestCase;

public class AUIControllerModuleTest extends TestCase {
	
	AUIControllerModule acm;
	
	XMLConfiguration config;
	
	Controller c;

	public static void main(String[] args){
    	junit.textui.TestRunner.run(AUIControllerModuleTest.class);
    }

	public void setUp() {
		//c = Controller.getController();
		acm = new AUIControllerModule();
		//acm.setController(c);
		config = new XMLConfiguration();
		acm.setConfiguration(config);
    }

}
