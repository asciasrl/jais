package it.ascia.ais;

import it.ascia.ais.Transport;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.UnresolvedAddressException;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * Gestisce la comunicazione attraverso un convertitore seriale-ethernet.
 * 
 * Tutti i messaggi che passano vengono smistati all'oggetto BMCComputer 
 * passato al costruttore.
 * 
 * @author arrigo
 */
public class TCPSerialTransport extends Transport {
	/**
	 * Un thread che attende i dati dalla rete e li manda al transport.
	 */
	private static class TCPSerialBusReader implements Runnable {
		/**
		 * Dimensioni del buffer che riceve i dati.
		 */
		private static final int BUF_SIZE = 32;
		/**
		 * Il nostro selector per l'attesa di dati in ingresso.
		 */
		private Selector selector;
		/**
		 * Il SocketChannel registrato su selector;
		 */
		private SocketChannel sock;
		/**
		 * Il nostro transport.
		 */
		private TCPSerialTransport bus;

		/**
		 * Il nostro logger.
		 */
		private Logger logger;
		/**
		 * Costruttore.
		 * 
		 * @param selector un Selector pronto a leggere, su cui è registrato 
		 * _un_ SocketChannel.
		 * @param transport il transport da avvisare quando arrivano dati.
		 * @param sock il SocketChannel *gia' registrato* sul selector
		 */
		public TCPSerialBusReader(Selector selector, TCPSerialTransport bus, 
				SocketChannel sock) {
			this.selector = selector;
			this.bus = bus;
			this.sock = sock;
			logger = Logger.getLogger(getClass());
		}
		
		/**
		 * Qui si fa il lavoro sporco.
		 * 
		 * <p>I dati vengono ricevuti dal SocketChannel vengono estratti e 
		 * passati al transport sotto forma di array di byte.</p> 
		 */
		public void run() {
			try {
				while (selector.select() > 0) {
					Set keys = selector.selectedKeys();
					// Abbiamo un solo SocketChannel
					ByteBuffer bb = ByteBuffer.allocate(BUF_SIZE);
					sock.read(bb);
					bb.flip();
					bus.setByteBuffer(bb);
					bus.connector.readData();
					keys.clear();
				}
			} catch (IOException e) {
				logger.error("Errore durante l'attesa di dati da rete: " +
						e.getMessage());
			}
		}
	} // Classe TCPSerialBusReader
	
	/**
	 * Il nostro thread che aspetta i dati e ce li passa.
	 */
	private TCPSerialBusReader tcpSerialBusReader;
	/**
	 * Il buffer che contiene i dati ricevuti.
	 */
	private ByteBuffer byteBuffer;
	/**
	 * La porta TCP a cui connettersi di default.
	 */
	private int tcpPort;
	/**
	 * Il nostro socket.
	 */
    SocketChannel sock;	
    /**
     * Il Selector per la lettura.
     */
    Selector readSelector;
    /**
     * Il selector per la scrittura.
     */
    Selector writeSelector;

    /**
     * Costruttore.
     *
     * @param hostName nome dell'host (ad es. "1.2.3.4" o "www.pippo.com")
     * 
     * @throws un'Exception se incontra un errore
     */
    public TCPSerialTransport(String hostName, int port) 
    	throws AISException {
    	this.tcpPort = port;
    	name = hostName + ":" + port;
		try {
			logger.info("Connessione a " + hostName + ":" + port);
			sock = SocketChannel.open(new InetSocketAddress(hostName, tcpPort));
			sock.configureBlocking(false);
		} catch (UnresolvedAddressException e) {
			throw new AISException("Indirizzo non trovato: " + hostName);
		} catch (IOException e) {
			throw new AISException("Impossibile ottenere gli stream: " + 
					e.getMessage());
		}
		try {
			readSelector = Selector.open();
			sock.register(readSelector, SelectionKey.OP_READ);
		} catch (IOException e) {
			throw new AISException("Impossibile creare il Selector di lettura: " +
					e.getMessage());
		}
		try {
			writeSelector = Selector.open();
			sock.register(writeSelector, SelectionKey.OP_WRITE);
		} catch (IOException e) {
			throw new AISException("Impossibile creare il Selector di " +
					"scrittura: " +	e.getMessage());
		}
		tcpSerialBusReader = new TCPSerialBusReader(readSelector, this, sock);
		logger.debug("Avvio listener");
		new Thread(tcpSerialBusReader).start();
    }
    
    /**
     * Invia un messaggio sulla connessione.
     * 
     * Eventuali errori di trasmissione vengono ignorati.
     * 
     * @param m the message to send
     */
    public synchronized void write(byte[] rawMessage) {
    	ByteBuffer bb = ByteBuffer.allocate(rawMessage.length);
    	bb.put(rawMessage);
    	bb.rewind();
    	try {
    		// How to make a non-blocking connection blocking
    		while (bb.hasRemaining()) {
    			int channels;
    			channels = writeSelector.select(); 
    			if (channels > 0) {
    				Set keys = writeSelector.selectedKeys();
    				// Abbiamo un solo SocketChannel
    				sock.write(bb);
    				keys.clear();
    			} else if (channels == 0) {
    				throw new IOException("la connessione e' caduta!");
    			} else {
    				// channels < 0: alquanto strano
    				throw new IOException("errore durante l'ascolto.");
    			}
    		}
    	} catch (IOException e) {
    		logger.error("Errore durante l'invio di dati via rete: " +
				e.getMessage());
    	}
    }
    
    /**
     * Riceve il buffer con i dati da passare alla superclasse.
     */
    protected void setByteBuffer(ByteBuffer bb) {
    	this.byteBuffer = bb;
    }

	public boolean hasData() {
		if (byteBuffer != null) {
				return byteBuffer.hasRemaining();
		} else {
			return false;
		}
	}

	public byte readByte() throws IOException {
		try {
			return byteBuffer.get();
		} catch (BufferUnderflowException e) {
			throw new IOException("Buffer vuoto.");
		}
	}
	
	/**
     * Chiude la connessione.
     */
    public void close() {
    	logger.info("Chiusura connessione seriale TCP ...");
    	try {
    		readSelector.close();
    		writeSelector.close();
    		sock.close();
    	} catch (IOException e) {
    		logger.error("Errore durante la chiusura della connessione: " +
    				e.getMessage());
    	}
    }
}
