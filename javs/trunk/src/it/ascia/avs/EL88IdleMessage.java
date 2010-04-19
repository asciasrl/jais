package it.ascia.avs;

public class EL88IdleMessage extends EL88Message {
	
	public EL88IdleMessage() {
		super(SET_INFO,SEL_IDLE,FORMAT_0);
	}
}
