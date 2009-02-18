package it.ascia.ais;

import java.io.IOException;

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
    public Transport() {
		logger = Logger.getLogger(getClass());
    }

    /**
     * Associa il Transport al Connector
     * @param connector Il connettore associato
     */
    public void bind(Connector connector) {
    	this.connector = connector;
    	connector.transport = this;
    }
    
    public String toString()
    {
    	return name;
    }

    /**
     * Verifica se ci sono dati pronti da leggere.
     * 
     * @return true se ci sono dati leggibili da readByte()
     */
    public abstract boolean hasData();
    
    /**
     * Ritorna il prossimo byte ricevuto.
     * @throws IOException 
     * 
     * @return il dato ricevuto.
     */
    public abstract byte readByte() throws IOException;
    
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