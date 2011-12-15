package it.ascia.ais;

import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.log4j.Logger;

/**
 * Mezzo di trasporto dei messaggi (seriale, tcp, ...) 
 * 
 * $Id$
 * 
 * Il transport puo' essere usato da un solo thread per volta, che deve acquisirne il permesso e rilasciarlo alla fine.
 * <pre>
 *   transport.acquire();
 *   transport.write(stuff);
 *   transport.release();
 * </pre>
 * 
 * 
 * TODO Gestire riconnessioni: metodo Transport.connect(), metodo Connector.getTransport() effettua tentativi riconnessione
 * TODO Modificare il metodo di creazione in modo che possa creare anche altri transport
 * TODO Gestire piu' transport con una priorita'
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
     * Il semaforo associato al transport
     */
	protected Semaphore semaphore;

    /**
     * Costruttore.
     */
    public Transport() {
		logger = Logger.getLogger(getClass());
		semaphore = new Semaphore(1,true);
    }

    public String toString()
    {
    	return name;
    }
    
    /**
     * 
     * @return Connector speed in baud
     */
    public abstract int getSpeed();
    
    /**
     * 
     * @return Information about the transport (class, name, status) 
     */
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

    /**
     * Transport creator method
     * Implements serial and tpcip transport
     * @param sub Transport sub configuration
     * @return a new Transport
     */
	public static Transport createTransport(HierarchicalConfiguration sub) {
		Transport transport;
 		List transports = sub.configurationsAt("transport");
 		if (transports.size() == 0) {
 			throw(new AISException("Trasport not defined"));
 		}
 		if (transports.size() > 1) {
 			throw(new AISException("Current implementation can use only one transport"));
 		} 		
		SubnodeConfiguration transportConfig = (SubnodeConfiguration) transports.get(0);
 		String type = transportConfig.getString("type");
 		if (type == null) {
 			throw(new AISException("Missing parameter 'type' of transport"));
 		}
 		if (type.equals("serial")) {
 			String port = transportConfig.getString("port");
 			int speed = transportConfig.getInt("speed");
 			int databits = transportConfig.getInt("databits",8);
 			int parity = transportConfig.getInt("parity",0);
 			int stopbits = transportConfig.getInt("stopbits",1);
 			transport = new SerialTransport(port,speed,databits,parity, stopbits);
 		} else if (type.equals("tcp")) {
 			String host = transportConfig.getString("host");
 			int port = transportConfig.getInt("port");					
 			transport = new TCPSerialTransport(host,port);
 		} else if (type.equals("loop")) {
 			transport = new LoopTransport();
 		} else if (type.equals("null")) {
 			transport = null;
		} else {
			throw(new AISException("Transport "+type+" non riconosciuto"));
		}
 		return transport;
	}

	/**
	 * Try to Acquires the permit of the transport. 
	 * @return true if the permit was acquired, false otherwise
	 */
	public boolean tryAcquire() {
		try {
			return semaphore.tryAcquire(0, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			return false;
		}
	}

	/**
	 * Try to Acquires the permit of the transport.  Wait until one become available. 
	 * Return when the permit is acquired, or throw the exception if interrupted. 
	 * @throws InterruptedException
	 */
	public void acquire() throws InterruptedException {
		semaphore.acquire();		
	}

	/**
	 * Release the permit of the transport.
	 * If any threads are trying to acquire the permit, then one is selected and given the permit that was just released. 
	 */
	public void release() {
		semaphore.release();
	}
    
}
