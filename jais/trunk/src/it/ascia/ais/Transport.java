package it.ascia.ais;

import org.apache.log4j.Logger;

/**
 * Mezzo di trasporto dei messaggi (seriale, tcp, ...) 
 * 
 * 
 * TODO Gestire riconnessioni: metodo Transport.connect(), metodo Connector.getTransport() effettua tentativi riconnessione
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
    
    public abstract String getInfo();

	/**
     * Invia un messaggio sul transport.
     * 
     * <p>Eventuali errori di trasmissione vengono ignorati.</p>
     * 
     * @param b the message to send
     */
    public abstract void write(byte[] b);
    
    /**
     * Chiude la connessione del transport.
     */
    public abstract void close();

    
}
