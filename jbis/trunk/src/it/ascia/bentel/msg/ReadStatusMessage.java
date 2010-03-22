package it.ascia.bentel.msg;

public class ReadStatusMessage extends BentelKyoMessage {

	public ReadStatusMessage() {
		int b[] = {0xf0, 0x68, 0x0e, 0x07, 0x00};
		loadRequest(b, true);
	}
	
	@Override
	public int getResponseSize() {
		return 9;
	}

	@Override
	public long getResponseTimeout() {
		return 1000;
	}

}
