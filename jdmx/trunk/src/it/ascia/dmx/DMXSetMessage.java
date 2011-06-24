package it.ascia.dmx;

import it.ascia.ais.Message;

public class DMXSetMessage extends Message {
	
	int channel;
	int value;

	public DMXSetMessage(int channel, int value) {
		this.channel = channel;
		this.value = value;		
	}
	
	@Override
	public String toString() {
		return "Set channel " + channel + " to " + value; 
	}

	@Override
	public byte[] getBytesMessage() {
		String s = "C" + String.format("%03d", getChannel()) + "L" + String.format("%03d", getValue());
		return s.getBytes();
	}

	public int getChannel() {
		return channel;
	}

	public int getValue() {
		return value;
	}

}
