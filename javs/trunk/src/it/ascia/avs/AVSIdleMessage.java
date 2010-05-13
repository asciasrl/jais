package it.ascia.avs;


public class AVSIdleMessage extends AVSMessage {
	
	public AVSIdleMessage() {
		super(Code.SET_IDLE,FORMAT_0);
	}
}
