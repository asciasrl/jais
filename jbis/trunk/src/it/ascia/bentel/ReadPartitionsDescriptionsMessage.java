package it.ascia.bentel;

import it.ascia.bentel.msg.BentelKyoMessage;

public class ReadPartitionsDescriptionsMessage extends BentelKyoMessage {

	public ReadPartitionsDescriptionsMessage(int block) {
		// TODO partizioni 5-8
		int[][] b = {{0xf0, 0xd0, 0x12, 0x3f, 0x00}};
		loadRequest(b[block], true);		
	}

	@Override
	public int getResponseSize() {
		return 65;
	}

	@Override
	public long getResponseTimeout() {
		return 1000;
	}

	/**
	 * 
	 * @param partition 0 - 3
	 * @return
	 */
	public String getDescription(int partition) {
		return getResponseAsText().substring(partition * 16, (partition + 1) * 16 - 1);
	}

}
