package it.ascia.aui.tests;

import java.io.FileNotFoundException;

import it.ascia.ais.Controller;
import it.ascia.aui.AUIControllerModule;

import javax.servlet.http.HttpSession;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TestAUIRPCServer extends TestCase {
	
	private static final int DEST = 3;
	
	private HttpSession sessione; 
	
	public TestAUIRPCServer() {
		//session = new HttpSession
	}

	public static Test suite() {
		TestSuite ts = new TestSuite();
		ts.addTestSuite(TestAUIRPCServer.class);
		return new AuiTestSetup(ts);
	}

	public void testSaveAs() {
    	AUIControllerModule aui = (AUIControllerModule) Controller.getController().getModule("AUI");
    	/*
    	try {
			aui.saveConfigurationAs(aui.getConfiguration(),"test.xml", true);
			assertTrue(true);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		*/
	}
}