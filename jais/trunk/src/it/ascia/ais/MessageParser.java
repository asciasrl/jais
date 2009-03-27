/**
 * 
 */
package it.ascia.ais;

import org.apache.log4j.Logger;

/**
 * @author Sergio
 *
 */
public abstract class MessageParser {

	protected boolean valid = false;

	protected Message message;

	protected Logger logger;

	public MessageParser() {
		logger = Logger.getLogger(getClass());
	}

	/**
	 * Restitusce una rappresentazione testuale del contenuto del buffer
	 * @return
	 */
	public abstract String dumpBuffer();

	/**
	 * Aggiunge un byte al buffer di parsing
	 * Non appena è stato composto un messaggio valido, viene instanziato e puo' essere letto con getMessage()
	 * @param b unsigned short
	 */
	public abstract void push(int b);
	
	/**
	 * Ha fatto il parsing di un messaggio valido, che puo' essere letto con getMessage() 
	 * @return
	 */
	public boolean isValid() {
		return valid;
	}

	/**
	 * Restituisce la instanza di messaggio risultato del parsing
	 * @return
	 */
	public Message getMessage()
	{
		return message;
	}

	/**
	 * Ritorna true se il parser sta sta ricevendo un messaggio ma questo non e' ancora completo
	 * Si deve ritardare l'invio di un successivo messaggio
	 * @return
	 */
	public abstract boolean isBusy();
	
}
