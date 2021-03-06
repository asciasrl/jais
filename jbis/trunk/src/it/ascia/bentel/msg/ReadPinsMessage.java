package it.ascia.bentel.msg;

public class ReadPinsMessage extends BentelKyoMessage {

	/**
	 * 0xf0 0x39 0x01 0x3f 0x00 0x69
	 *  
	 * 0x00 0x00 0x00 0x01 0xff 0xff 1 
	 * 0x00 0x00 0x00 0x02 0xff 0xff 2
	 * 0x00 0x00 0x00 0x03 0xff 0xff 3
	 * 0x00 0x00 0x00 0x04 0xff 0xff 4
	 * 0x00 0x00 0x00 0x05 0xff 0xff 5
	 * 0x00 0x00 0x00 0x06 0xff 0xff 6
	 * 0x00 0x00 0x00 0x07 0xff 0xff 7
	 * 0x00 0x00 0x00 0x08 0xff 0xff 8
	 * 0x00 0x00 0x00 0x09 0xff 0xff 9
	 * 0x00 0x00 0x01 0x00 0xff 0xff 10
	 * 0x00 0x00 0x01 0x01           11 
	 * 0x1c
	 * 
	 * 
	 * 0xf0 0x79 0x01 0x3f 0x00 0xa9 
	 *                     0xff 0xff 11 
	 * 0x00 0x00 0x01 0x02 0xff 0xff 12
	 * 0x00 0x00 0x01 0x03 0xff 0xff 13
	 * 0x00 0x00 0x01 0x04 0xff 0xff 14
	 * 0x00 0x00 0x01 0x05 0xff 0xff 15
	 * 0x00 0x00 0x01 0x06 0xff 0xff 16
	 * 0x00 0x00 0x01 0x07 0xff 0xff 17
	 * 0x00 0x00 0x01 0x08 0xff 0xff 18
	 * 0x00 0x00 0x01 0x09 0xff 0xff 19
	 * 0x00 0x00 0x02 0x00 0xff 0xff 20
	 * 0x00 0x00 0x02 0x01 0xff 0xff 21
	 * 0x00 0x00                     22 
	 * 0x23
	 * 
	 * 0xf0 0xb9 0x01 0x0f 0x00 0xb9 
	 * 
	 *           0x02 0x02 0xff 0xff 22
	 * 0x00 0x00 0x02 0x03 0xff 0xff 23
	 * 0x00 0x00 0x02 0x04 0xff 0xff 24
	 * 0x09
	 */
		
	/**
	 * 
	 * @param block 0:zones 1-4 1:zones 5-8
	 */
	public ReadPinsMessage(int block) {
		// 0xf0 0x39 0x01 0x3f 0x00 0x69
		int[][] b = {
				{0xf0, 0x39, 0x01, 0x3f, 0x00},
				{0xf0, 0x79, 0x01, 0x3f, 0x00},
				{0xf0, 0xb9, 0x01, 0x0f, 0x00}
		};
		loadRequest(b[block], true);		
	}

	@Override
	public int getResponseSize() {
		// TODO Auto-generated method stub
		return 65;
	}

	@Override
	public long getResponseTimeout() {
		// TODO Auto-generated method stub
		return 2000;
	}
	
}
