package it.ascia.eds;

import it.ascia.eds.msg.Message;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.UnresolvedAddressException;
import java.util.Enumeration;
import java.util.Set;
import java.util.TooManyListenersException;

import gnu.io.*;

/**
 * Gestisce la comunicazione con il bus EDS attraverso un convertitore seriale-ethernet.
 * 
 * Tutti i messaggi che passano vengono smistati all'oggetto BMCComputer 
 * passato al costruttore.
 * 
 * @author arrigo
 */
public class TCPSerialBus extends Bus {
	/**
	 * Un thread che attende i dati dalla rete e li manda al bus.
	 */
	private static class TCPSerialBusReader implements Runnable {
		/**
		 * Il nostro selector.
		 */
		private Selector selector;
		/**
		 * Il nostro bus.
		 */
		private TCPSerialBus bus;
		/**
		 * @param selector un Selector pronto a leggere, su cui è registrato _un_ SocketChannel.
		 * @param bus il bus da avvisare quando arrivano dati.
		 */
		public TCPSerialBusReader(Selector selector, TCPSerialBus bus) {
			this.selector = selector;
			this.bus = bus;
		}
		/**
		 * Qui si fa il lavoro sporco.
		 */
		public void run() {
			try {
				while (selector.select() > 0) {
					Set keys = selector.selectedKeys();
					bus.readData();
					keys.clear();
				}
			} catch (IOException e) {
				System.out.println("Errore durante l'attesa di dati: " +
						e.getMessage());
			}
		}
	}
	/**
	 * La porta TCP a cui connettersi di default.
	 */
	private final int TCP_PORT = 2001;
    static boolean outputBufferEmptyFlag = false;
    SocketChannel sock;	
    
    /**
     * Costruttore
     *
     * @param portName nome della porta (ad es. "COM1" o "/dev/ttyUSB0")
     * 
     * @throws un'Exception se incontra un errore
     */
    public TCPSerialBus(String hostName) throws EDSException {    	
		try {
			sock = SocketChannel.open(new InetSocketAddress(hostName, TCP_PORT));
		    setInputStream(sock.socket().getInputStream());
		} catch (UnresolvedAddressException e) {
			throw new EDSException("Indirizzo non trovato: " + hostName);
		} catch (IOException e) {
			throw new EDSException("Impossibile ottenere gli stream: " + 
					e.getMessage());
		}
		// TODO:
		// - finire la classe che legge (controllare la gestione del Selector)
		// - preparare il selector qui e lanciare il thread
		// - preparare il sistema di scrittura bloccante - non-bloccante
    }
    
    /**
     * Invia un messaggio sul bus.
     * 
     * Eventuali errori di trasmissione vengono ignorati.
     * 
     * @param m the message to send
     */
    public void write(Message m) {
			// TODO 			        
    }
        
    /**
     * Chiude la connessione.
     */
    public void close() {
    	try {
    		sock.close();
    	} catch (IOException e) {
    		System.err.println("Errore durante la chiusura della connessione: " +
    				e.getMessage());
    	}
    }
}
