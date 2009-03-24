package it.ascia.ais;

import org.apache.log4j.Logger;

/**
 * Mezzo di trasporto dei messaggi (seriale, tcp, ...) 
 * 
 * @author Sergio
 *
 */
public abstract class Transport {
	
    /**
     * Il nostro logger.
     */
    protected Logger logger;
    
    /**
     * Il nome di questo trasport
     */
    protected String name;

    /**
     * Il connettore associato a questo Transport
     */
    protected Connector connector;
    
    /**
     * Costruttore.
     * @param connector Il connettore associato
     */
    public Transport(Connector connector) {
		logger = Logger.getLogger(getClass());
		this.connector = connector;		
    }

    public String toString()
    {
    	return name;
    }

	/**
     * Invia un messaggio sul transport.
     * 
     * <p>Eventuali errori di trasmissione vengono ignorati.</p>
     * 
     * @param m the message to send
     */
    public abstract void write(byte[] b);
    
    /**
     * Chiude la connessione al transport.
     */
    public abstract void close();

    
}
