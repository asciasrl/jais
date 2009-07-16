package it.ascia.aui.tests;

import java.util.Random;

import org.apache.log4j.Logger;

import it.ascia.ais.Controller;
import it.ascia.eds.EDSConnector;
import it.ascia.eds.msg.EDSMessage;
import it.ascia.eds.msg.RichiestaModelloMessage;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TestSerial extends TestCase {
	
	public static Test suite() {
		TestSuite ts = new TestSuite();
		ts.addTestSuite(TestSerial.class);
		return new AuiTestSetup(ts);
	}

	public int RichiestaModelloMessage_Bus1_a255(int n) {
		Logger log = Logger.getLogger(getClass());
		Controller c = Controller.getController();
		EDSConnector eds1 = (EDSConnector) c.getConnector("1");  
		EDSMessage msg;
		int success = 0;
		int slows = 0;
		int fails = 0;
		long start;
		//n = n * 10;
		for (int i=1; i <= n; i++) {
			//log.info("Messaggio di test n. "+i);
			msg = new RichiestaModelloMessage(255,1);
			start = System.nanoTime();
			if (eds1.sendMessage(msg)) {
				success++;
			} else {
				fails++;
			}
			long dt = (System.nanoTime()-start)/1000000;
			String s = "Messaggio di test n. "+i+" di "+n+" T= "+dt+" mS"; 
			if (dt > 100) {
				log.warn(s);
				slows++;
			} else {
				log.debug(s);
			}
		}
		log.info("Successi="+success+" Lenti="+slows+" Fallimenti="+fails);
		//return success/10;
		return success;
	}

	public void test00_1_RichiestaModelloMessage_Bus1_a255() {
    	assertEquals(1, RichiestaModelloMessage_Bus1_a255(1));				
	}

	public void test01_100_RichiestaModelloMessage_Bus1_a255() {
    	assertEquals(100, RichiestaModelloMessage_Bus1_a255(100));				
	}

	public void test02_1000_RichiestaModelloMessage_Bus1_a255() {
    	assertEquals(1000, RichiestaModelloMessage_Bus1_a255(1000));				
	}

	public void test03_1000_RichiestaModelloMessage_Bus1_a255() {
    	assertEquals(1000, RichiestaModelloMessage_Bus1_a255(1000));				
	}

	public void test04_1000_RichiestaModelloMessage_Bus1_a255() {
    	assertEquals(1000, RichiestaModelloMessage_Bus1_a255(1000));				
	}

	public void test05_1000_RichiestaModelloMessage_Bus1_a255() {
    	assertEquals(1000, RichiestaModelloMessage_Bus1_a255(1000));				
	}

	public void test06_1000_RichiestaModelloMessage_Bus1_a255() {
    	assertEquals(1000, RichiestaModelloMessage_Bus1_a255(1000));				
	}

	public void test07_1000_RichiestaModelloMessage_Bus1_a255() {
    	assertEquals(1000, RichiestaModelloMessage_Bus1_a255(1000));				
	}

	public void test08_1000_RichiestaModelloMessage_Bus1_a255() {
    	assertEquals(1000, RichiestaModelloMessage_Bus1_a255(1000));				
	}

	public void test09_1000_RichiestaModelloMessage_Bus1_a255() {
    	assertEquals(1000, RichiestaModelloMessage_Bus1_a255(1000));				
	}

	/*
	public void test02_100randomRichiestaModelloMessage() {
		Random r = new Random();
		Controller c = Controller.getController();
		EDSConnector eds1 = (EDSConnector) c.getConnector("1");  
		EDSConnector eds2 = (EDSConnector) c.getConnector("2");
		EDSMessage msg;
		for (int i=3; i < 255; i++) {
			//int a = r.nextInt(255);
			//int b = r.nextInt(255);			
			msg = new RichiestaModelloMessage(i,1);
			eds1.sendMessage(msg);
			msg = new RichiestaModelloMessage(i,2);
			eds2.sendMessage(msg);
		}
    	assertTrue(true);
	}
	*/
}
