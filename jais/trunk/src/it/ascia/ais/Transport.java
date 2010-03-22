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
     * Create transport
     * @param sub Transport sub configuration
     * @return a new Transport
     */
	public static Transport createTransport(HierarchicalConfiguration sub) {
		Transport transport;
 		List transports = sub.configurationsAt("transport");
 		if (transports.size() == 0) {
 			throw(new AISException("Trasport not defined"));
 		}
		SubnodeConfiguration transportConfig = (SubnodeConfiguration) transports.get(0);
 		String type = transportConfig.getString("type");
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
		} else {
			throw(new AISException("Transport "+type+" non riconosciuto"));
		}
 		return transport;
	}

	public boolean tryAcquire() {
		try {
			return semaphore.tryAcquire(0, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			return false;
		}
	}

	public void acquire() throws InterruptedException {
		semaphore.acquire();		
	}

	public void release() {
		semaphore.release();
	}

    
}
