package it.ascia.dmx;

import it.ascia.ais.Message;

public class DMXGetMessage extends Message {

	int channel;

	public DMXGetMessage(int channel) {
		this.channel = channel;
	}

	@Override
	public String toString() {
		return "Get channel " + channel; 
	}

	public int getChannel() {
		return channel;
	}

	@Override
	public byte[] getBytesMessage() {
		String s = "C" + String.format("%03d", getChannel()) + "?";		
		return s.getBytes();
	}

}
