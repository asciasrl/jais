package it.ascia.eds.msg;

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
	
	public final boolean isBroadcast() {
		return true;
	}
}
