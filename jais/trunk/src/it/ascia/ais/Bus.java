package it.ascia.ais;

import java.io.IOException;

import org.apache.log4j.Logger;

public abstract class Bus {
	
    /**
     * Il nostro logger.
     */
    protected Logger logger;

    /**
     * Il connecttore associato a questo Bus
     */
    protected Connector connector;
    
    /**
     * Costruttore.
     * @param name il nome del bus, che sara' la parte iniziale degli indirizzi
     * di tutti i Device collegati a questo bus.
     */
    public Bus(Connector connector) {
		logger = Logger.getLogger(getClass());
    	this.connector = connector;
    	connector.bus = this;
    }
    
    public String getName()
    {
    	return connector.getName();
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
     * Invia un messaggio sul bus.
     * 
     * <p>Eventuali errori di trasmissione vengono ignorati.</p>
     * 
     * @param m the message to send
     */
    public abstract void write(byte[] b);
    
    /**
     * Chiude la connessione al bus.
     */
    public abstract void close();

}
