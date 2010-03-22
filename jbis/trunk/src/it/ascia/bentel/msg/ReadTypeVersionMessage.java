package it.ascia.bentel.msg;

public class ReadTypeVersionMessage extends BentelKyoMessage {

	public ReadTypeVersionMessage() {
		int[] b = {0xf0, 0x00, 0x00, 0x0b, 0x00};
		loadRequest(b, true);
	}
	
	@Override
	public int getResponseSize() {
		// TODO Auto-generated method stub
		return 13;
	}

	@Override
	public long getResponseTimeout() {
		// TODO Auto-generated method stub
		return 1000;
	}

	public String getType() {
		if (response != null) {
			String s = getResponseAsText();
			return s.substring(0,7).trim();
		} else {
			return null;
		}		
	}

	public String getVersion() {
		if (response != null) {
			String s = getResponseAsText();
			return s.substring(8).trim();
		} else {
			return null;
		}		
	}

}
