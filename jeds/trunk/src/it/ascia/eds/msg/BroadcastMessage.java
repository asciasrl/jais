package it.ascia.eds.msg;

import java.util.Random;

/**
 * 
 * @author sergio
 */
public abstract class BroadcastMessage extends EDSMessage 
	{
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
	
	/**
	 * Genera l'header con destinatario e mittente casuali.
	 * 
	 * <p>Questo metodo deve essere chiamato prima di ogni reinvio.</p>
	 */
	public void randomizeHeaders() {
		Destinatario = r.nextInt() & 0xFF;
		Mittente = r.nextInt() & 0xFF;
	}
}
