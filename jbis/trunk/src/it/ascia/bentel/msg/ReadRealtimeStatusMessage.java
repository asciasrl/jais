package it.ascia.bentel.msg;

public class ReadRealtimeStatusMessage extends BentelKyoMessage {

	public ReadRealtimeStatusMessage() {
		int b[] = {0xf0, 0x04, 0xf0, 0x04, 0x00};
		loadRequest(b, true);
	}
	
	@Override
	public int getResponseSize() {
		return 6;
	}

	@Override
	public long getResponseTimeout() {
		return 1000;
	}

}
