package it.ascia.aui.tests;

import java.util.Random;

import it.ascia.ais.Address;
import it.ascia.ais.Connector;
import junit.framework.TestCase;

public class TestAddress extends TestCase {

	public void testVoid() {
		Address a = new Address();
		assertTrue(a.toString().equals("*.*:*"));
	}
	
	public void testNull() {
		Address a = new Address(null);
		assertTrue(a.toString().equals("*.*:*"));
	}

	public void testNull2() {
		Address a = new Address(null);
		Address b = new Address(null);
		assertTrue(a.toString().equals(b.toString()));
	}

	public void testConnector() {
		Address a = new Address("a","b","c");
		assertTrue(a.getConnectorName().equals("a"));		
	}

	public void testDevice() {
		Address a = new Address("a","b","c");
		assertTrue(a.getDeviceAddress().equals("b"));		
	}

	public void testPort() {
		Address a = new Address("a","b","c");
		assertTrue(a.getPortId().equals("c"));		
	}

	public void testPartial1() {
		Address a = new Address("a",null,null);
		assertTrue(a.toString().equals("a.*:*"));
	}

	public void testPartial2() {
		Address a = new Address("a","b",null);
		assertTrue(a.toString().equals("a.b:*"));
	}

	public void testPartial3() {
		Address a = new Address("a","b","c");
		assertTrue(a.toString().equals("a.b:c"));
	}

	public void testPartial4() {
		Address a = new Address(null,"b","c");
		assertTrue(a.toString().equals("*.b:c"));
	}

	public void testAppend1() {
		Address a = new Address("a",null,null);
		try {
			a.setConnectorName("b");
			fail("Should raise an IllegalArgumentException");
		} catch (IllegalArgumentException e) {
		}
	}

	public void testAppend1a() {
		Address a = new Address((Connector)null,null,null);
		a.setConnectorName("a");
		assertTrue(a.toString().equals("a.*:*"));
	}

	public void testAppend2() {
		Address a = new Address("a","b",null);
		try {
			a.setDeviceAddress("b");
			fail("Should raise an IllegalArgumentException");
		} catch (IllegalArgumentException e) {
		}
	}

	public void testAppend2a() {
		Address a = new Address("a",null,null);
		a.setDeviceAddress("b");
		assertTrue(a.toString().equals("a.b:*"));
	}

	public void testAppend3() {
		Address a = new Address("a","b","c");
		try {
			a.setPortId("c");
			fail("Should raise an IllegalArgumentException");
		} catch (IllegalArgumentException e) {
		}
	}

	public void testAppend3a() {
		Address a = new Address("a","b",null);
		a.setPortId("c");
		assertTrue(a.toString().equals("a.b:c"));
	}

	public void testParse() {
		Address a = new Address("a.b:c");
		assertTrue(a.toString().equals("a.b:c"));		
	}

	public void testParse1() {
		Address a = new Address("*");
		assertTrue(a.toString().equals("*.*:*"));		
	}

	public void testParse2() {
		Address a = new Address("*.*");
		assertTrue(a.toString().equals("*.*:*"));		
	}

	public void testParse3() {
		Address a = new Address("*.*:*");
		assertTrue(a.toString().equals("*.*:*"));		
	}

	public void testParse4() {
		Address a = new Address("*:*");
		assertTrue(a.toString().equals("*.*:*"));		
	}

	public void testParse5() {
		Address a = new Address("a.:");
		assertTrue(a.toString().equals("a.*:*"));		
	}

	public void testParse5b() {
		Address a = new Address("a.:c");
		assertTrue(a.toString().equals("a.*:c"));		
	}

	public void testParse6() {
		Address a = new Address(".b:");
		assertTrue(a.toString().equals("*.b:*"));		
	}

	public void testParse7() {
		Address a = new Address(".:c");
		assertTrue(a.toString().equals("*.*:c"));		
	}

	public void testParse8() {
		Address a = new Address(":c");
		assertTrue(a.toString().equals("*.*:c"));		
	}

	public void testParse9() {
		Address a = new Address("b:c");
		assertTrue(a.toString().equals("*.b:c"));		
	}

	public void testParse10() {
		Address a = new Address("");
		assertTrue(a.toString().equals("*.*:*"));		
	}

	public void testParseConnector1() {
		Address a = new Address("a.b:c");
		assertTrue(a.getConnectorName().equals("a"));		
	}

	public void testParseConnector2() {
		Address a = new Address("b:c");
		assertTrue(a.getConnectorName() == null);		
	}

	public void testParseConnector2a() {
		Address a = new Address("*.b:c");
		assertTrue(a.getConnectorName() == null);		
	}

	public void testParseConnector2b() {
		Address a = new Address("a.b:");
		assertTrue(a.getConnectorName().equals("a"));		
	}

	public void testParseConnector2c() {
		Address a = new Address("a.:");
		assertTrue(a.getConnectorName().equals("a"));		
	}

	public void testParseConnector3() {
		Address a = new Address("a.b:c");
		assertTrue(a.getConnectorName().equals("a"));		
	}

	public void testParseDevice1() {
		Address a = new Address("a");
		assertTrue(a.getDeviceAddress().equals("a"));		
	}

	public void testParseDevice1a() {
		Address a = new Address("*");
		assertTrue(a.getDeviceAddress() == null);		
	}

	public void testParseDevice2() {
		Address a = new Address("a.b");
		assertTrue(a.getDeviceAddress().equals("b"));		
	}

	public void testParseDevice3() {
		Address a = new Address("a.b:c");
		assertTrue(a.getDeviceAddress().equals("b"));		
	}

	public void testParsePort1() {
		Address a = new Address("a");
		assertTrue(a.getPortId() == null);		
	}

	public void testParsePort2() {
		Address a = new Address("a.b");
		assertTrue(a.getPortId() == null);		
	}

	public void testParsePort3() {
		Address a = new Address("a.b:c");
		assertTrue(a.getPortId().equals("c"));		
	}

	public void testParsePort4() {
		Address a = new Address("a.b:*");
		assertTrue(a.getPortId() == null);		
	}
	
	public void testCompare1() {
		Address a = new Address("a.b:c");
		Address b = new Address("a.b:c");
		assertTrue(a.equals(b));				
	}
	
	public void testCompare2() {
		Address a = new Address("a.b:c");
		Address b = new Address("a.b:d");
		assertTrue(a.compareTo(b) < 0);				
	}

	public void testCompare3() {
		Address a = new Address("a.b:c");
		Address b = new Address("a.b:d");
		assertTrue(b.compareTo(a) > 0);				
	}
	
	public void testCompare4() {
		Address a = new Address("a.b:c");
		Address b = new Address("a.b:*");
		try {
			boolean x = a.compareTo(b) < 0;
			fail("Expected NullPointerException");
		} catch(NullPointerException e) {
			// OK
		}
	}

	public void testCompare5() {
		Address a = new Address("a.b:*");
		Address b = new Address("a.b:c");
		try {
			boolean x = a.compareTo(b) > 0;
			fail("Expected NullPointerException");
		} catch(NullPointerException e) {
			// OK
		}
	}

	public void testMateches1() {
		Address a = new Address("a.b:c");
		Address b = new Address("a.b:c");
		assertTrue(a.matches(b));
	}

	public void testMateches2() {
		Address a = new Address("a.b:c");
		Address b = new Address("*");
		assertTrue(a.matches(b));
	}

	public void testMateches3() {
		Address a = new Address("a.b:c");
		Address b = new Address("*");
		assertTrue(b.matches(a));
	}
	
	public void testMateches4() {
		Address a = new Address("a.b:c");
		Address b = new Address("a.*:c");
		assertTrue(a.matches(b));
	}

	public void testMateches5() {
		Address a = new Address("a.b:c");
		Address b = new Address(":c");
		assertTrue(a.matches(b));
	}

	public void testMateches6() {
		Address a = new Address("a.b:*");
		Address b = new Address("*.b:c");
		assertTrue(a.matches(b));
	}

	public void testMateches7() {
		Address a = new Address("a.*:*");
		Address b = new Address("*.*:c");
		assertTrue(a.matches(b));
	}

	public void testMateches8() {
		Address a = new Address("a.b:c");
		assertTrue(a.matches("*.*:c"));
	}

	public void testMateches9() {
		Address a = new Address("b:c");
		assertTrue(a.matches("a.b:c"));
	}

}
