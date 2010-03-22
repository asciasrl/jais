package it.ascia.bentel.msg;

public class ReadZonesDescriptionsMessage extends BentelKyoMessage {

	/**
	 * 
	 * @param block 0:zones 1-4 1:zones 5-8
	 */
	public ReadZonesDescriptionsMessage(int block) {
		// 0xf0 0x50 0x12 0x3f 0x00 0x91
		// 0xf0 0x90 0x12 0x3f 0x00 0xd1
		int[][] b = {
				{0xf0, 0x50, 0x12, 0x3f, 0x00},
				{0xf0, 0x90, 0x12, 0x3f, 0x00}
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
	
	/**
	 * 
	 * @param zone 0 - 3
	 * @return
	 */
	public String getDescription(int zone) {
		return getResponseAsText().substring(zone * 16, (zone + 1) * 16 - 1);
	}

	/*
	protected String decodeResponse() {
		if (response == null) {
			return "[]";
		} else {
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < 4; i++) {
				sb.append(" "+i+"=");
				for (int j = 0; j < 16; j++) {
					sb.append((char)response.elementAt(i * 16 + j).intValue());
				}
			}
			return sb.toString();
		}
	}
	*/

}
