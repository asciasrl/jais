package it.ascia.eds.msg;

import java.util.Random;

/**
 * 
 * @author sergio
 */
public abstract class BroadcastMessage extends Message 
	implements MessageInterface
	{
	/**
	 * Quante volte ri-inviare un messaggio broadcast.
	 */
	public int getSendTries() {
		return 8;
	}
}
