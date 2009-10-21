package it.ascia.aui.tests;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import it.ascia.ais.Controller;
import it.ascia.ais.SerialTransport;
import it.ascia.eds.EDSConnector;
import it.ascia.eds.msg.EDSMessage;
import it.ascia.eds.msg.RichiestaModelloMessage;
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

	public void test00_1_RichiestaModelloMessage_Bus1_aDEST() {
    	//assertEquals(1, );				
	}
}