package it.ascia.avs;

import it.ascia.ais.Controller;

import junit.framework.TestCase;

public class EasyLinkMessageTestCase extends TestCase {
	
	Controller c;

	/*
	public void testCrc_1_0() {
		int crc = 0xFFFF;
		int b = 0x00;
		crc = EasyLinkMessage.updateCRC(crc, b);
		assertEquals(0xE1F0, crc);
	}
	
	public void testCrc_1_1() {
		int crc = 0xFFFF;
		int b = 0x37;
		crc = EasyLinkMessage.updateCRC(crc, b);
		assertEquals(0xA744, crc);
	}

	public void testCrc_1_2() {
		int crc = 0xFFFF;
		int b = 0xFF;
		crc = EasyLinkMessage.updateCRC(crc, b);
		assertEquals(0xFF00, crc);
	}

	public void testCrc_1_3() {
		int crc = 0xFFFF;
		crc = EasyLinkMessage.updateCRC(crc, 0x00);
		crc = EasyLinkMessage.updateCRC(crc, 0x00);
		assertEquals(0x1D0F, crc);
	}

	public void testCrc_1_4() {
		int crc = 0xFFFF;
		crc = EasyLinkMessage.updateCRC(crc, 0x00);
		crc = EasyLinkMessage.updateCRC(crc, 0x00);
		crc = EasyLinkMessage.updateCRC(crc, 0x00);
		crc = EasyLinkMessage.updateCRC(crc, 0x00);
		crc = EasyLinkMessage.updateCRC(crc, 0x00);
		crc = EasyLinkMessage.updateCRC(crc, 0x00);
		assertEquals(0x0e10, crc);
	}
	*/
	
	public void testCrc_2_0() {
		int crc = 0xFFFF;
		int b = 0x00;
		crc = EL88Message.calcCRC(crc, b);
		assertEquals(0xE1F0, crc);
	}
	
	public void testCrc_2_1() {
		int crc = 0xFFFF;
		int b = 0x37;
		crc = EL88Message.calcCRC(crc, b);
		assertEquals(0xA744, crc);
	}

	public void testCrc_2_2() {
		int crc = 0xFFFF;
		int b = 0xFF;
		crc = EL88Message.calcCRC(crc, b);
		assertEquals(0xFF00, crc);
	}

	public void testCrc_2_3() {
		int crc = 0xFFFF;
		crc = EL88Message.calcCRC(crc, 0x00);
		crc = EL88Message.calcCRC(crc, 0x00);
		assertEquals(0x1D0F, crc);
	}

	public void testCrc_2_4() {
		int crc = 0xFFFF;
		crc = EL88Message.calcCRC(crc, 0x00);
		crc = EL88Message.calcCRC(crc, 0x00);
		crc = EL88Message.calcCRC(crc, 0x00);
		crc = EL88Message.calcCRC(crc, 0x00);
		crc = EL88Message.calcCRC(crc, 0x00);
		crc = EL88Message.calcCRC(crc, 0x00);
		assertEquals(0x0e10, crc);
	}

	public void testCrc_3_1() {
		int crc = 0x2404;
		crc = EL88Message.calcCRC(crc, 0x00);
		assertEquals(0x0e10, crc);
	}


	/*
	@Override
	protected void setUp() throws Exception {
		c = Controller.getController();
		c.configure();
		c.start();
		super.setUp();
	}
	
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		c.stop();
	}
	*/
	
}
