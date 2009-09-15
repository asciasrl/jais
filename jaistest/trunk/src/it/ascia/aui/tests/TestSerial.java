package it.ascia.aui.tests;

import org.apache.log4j.Logger;

import it.ascia.ais.Controller;
import it.ascia.ais.SerialTransport;
import it.ascia.eds.EDSConnector;
import it.ascia.eds.msg.EDSMessage;
import it.ascia.eds.msg.RichiestaModelloMessage;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TestSerial extends TestCase {
	
	private static final int DEST = 3;

	public static Test suite() {
		TestSuite ts = new TestSuite();
		ts.addTestSuite(TestSerial.class);
		return new AuiTestSetup(ts);
	}

	public int RichiestaModelloMessage_Bus1_aDEST(int n) {
		Logger log = Logger.getLogger(getClass());
		Controller c = Controller.getController();
		EDSConnector eds = (EDSConnector) c.getConnector("6");  
		EDSMessage msg;
		int success = 0;
		int slows = 0;
		int fails = 0;
		long start0 = System.nanoTime();
		long start;
		long speed = 0;
		//n = n * 10;
		//SerialTransport tr = (SerialTransport) eds.getTransport();
		//byte[] b = {0x55};
		for (int i=1; i <= n; i++) {
			//log.info("Messaggio di test n. "+i);
			msg = new RichiestaModelloMessage(DEST,76);
			start = System.nanoTime();
			//tr.write(b);
			if (eds.sendMessage(msg)) {
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
			dt = ((System.nanoTime()-start0)/1000000000L);
			if (dt > 0 && (i %10)==0 ) {
				speed = i / dt;
				System.out.print(speed + " "+i+"/"+success+"/"+slows+"/"+ fails+"\r");
			}
		}
		log.info("Successi="+success+" Lenti="+slows+" Fallimenti="+fails);
		//return success/10;
		return success;
	}

	public void test00_1_RichiestaModelloMessage_Bus1_aDEST() {
    	assertEquals(1, RichiestaModelloMessage_Bus1_aDEST(1));				
	}

	public void test01_100_RichiestaModelloMessage_Bus1_aDEST() {
    	assertEquals(100, RichiestaModelloMessage_Bus1_aDEST(100));				
	}

	public void test02_1000_RichiestaModelloMessage_Bus1_aDEST() {
    	assertEquals(1000, RichiestaModelloMessage_Bus1_aDEST(1000));				
	}

	public void test03_1000_RichiestaModelloMessage_Bus1_aDEST() {
    	assertEquals(1000, RichiestaModelloMessage_Bus1_aDEST(1000));				
	}

	public void test04_1000_RichiestaModelloMessage_Bus1_aDEST() {
    	assertEquals(1000, RichiestaModelloMessage_Bus1_aDEST(1000));				
	}

	public void test05_1000_RichiestaModelloMessage_Bus1_aDEST() {
    	assertEquals(1000, RichiestaModelloMessage_Bus1_aDEST(1000));				
	}

	public void test06_1000_RichiestaModelloMessage_Bus1_aDEST() {
    	assertEquals(1000, RichiestaModelloMessage_Bus1_aDEST(1000));				
	}

	public void test07_1000_RichiestaModelloMessage_Bus1_aDEST() {
    	assertEquals(1000, RichiestaModelloMessage_Bus1_aDEST(1000));				
	}

	public void test08_1000_RichiestaModelloMessage_Bus1_aDEST() {
    	assertEquals(1000, RichiestaModelloMessage_Bus1_aDEST(1000));				
	}

	public void test09_1000_RichiestaModelloMessage_Bus1_aDEST() {
    	assertEquals(1000, RichiestaModelloMessage_Bus1_aDEST(1000));				
	}

	public void test10_10K_RichiestaModelloMessage_Bus1_aDEST() {
    	assertEquals(10000, RichiestaModelloMessage_Bus1_aDEST(10000));				
	}

	/*
	public void test02_100randomRichiestaModelloMessage() {
		Random r = new Random();
		Controller c = Controller.getController();
		EDSConnector eds1 = (EDSConnector) c.getConnector("1");  
		EDSConnector eds2 = (EDSConnector) c.getConnector("2");
		EDSMessage msg;
		for (int i=3; i < DEST; i++) {
			//int a = r.nextInt(DEST);
			//int b = r.nextInt(DEST);			
			msg = new RichiestaModelloMessage(i,1);
			eds1.sendMessage(msg);
			msg = new RichiestaModelloMessage(i,2);
			eds2.sendMessage(msg);
		}
    	assertTrue(true);
	}
	*/
}
