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
	 * @return una riga di testo con il contenuto in esadecimale
	 */
	public abstract String dumpBuffer();

	/**
	 * Accoda i byte ricevuti dalla seriale fino ad ottenere una sequenza che rappresenta un messaggio.
	 * Non appena è stato composto un messaggio valido {@link #isValid()} restituisce true, viene instanziato il messaggio e 
	 * puo' essere letto con {@link #getMessage()}
	 * @param b Byte letto dalla seriale, inteso come unsigned short (8 bit), cioe' un numero nell'intervallo 0-255 
	 */
	public abstract void push(int b);
	
	/**
	 * Ha fatto il parsing di un messaggio valido, che puo' essere letto con getMessage() 
	 * @return messaggio valido
	 */
	public boolean isValid() {
		return valid;
	}

	/**
	 * Restituisce la instanza di messaggio risultato del parsing
	 * @return Instanza di Message
	 */
	public Message getMessage()
	{
		return message;
	}

	/**
	 * Ritorna true se il parser sta sta ricevendo un messaggio ma questo non e' ancora completo
	 * Si deve ritardare l'invio di un successivo messaggio
	 * @return Parser occupato
	 */
	public abstract boolean isBusy();
	
}
