package it.ascia.eds.msg;

import java.util.Random;

/**
 * 
 * @author sergio
 */
public abstract class BroadcastMessage extends EDSMessage 
	{

	public BroadcastMessage() {
		Destinatario = r.nextInt() & 0xFF;
		Mittente = r.nextInt() & 0xFF;
	}
	
	/**
	 * La nostra fonte di numeri casuali.
	 */
	private static Random r = new Random();

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
