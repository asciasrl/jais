package it.ascia.eds;

import it.ascia.eds.msg.Message;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.UnresolvedAddressException;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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
		 * Il nostro bus.
		 */
		private TCPSerialBus bus;
		/**
		 * @param selector un Selector pronto a leggere, su cui è registrato _un_ SocketChannel.
		 * @param bus il bus da avvisare quando arrivano dati.
		 * @param sock il SocketChannel *gia' registrato* sul selector
		 */
		public TCPSerialBusReader(Selector selector, TCPSerialBus bus, 
				SocketChannel sock) {
			this.selector = selector;
			this.bus = bus;
			this.sock = sock;
		}
		/**
		 * Qui si fa il lavoro sporco.
		 * 
		 * I dati vengono ricevuti dal SocketChannel vengono estratti e passati
		 * al bus sotto forma di array di byte. 
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
					bus.readData();
					keys.clear();
				}
			} catch (IOException e) {
				System.err.println("Errore durante l'attesa di dati da rete: " +
						e.getMessage());
			}
		}
	}
	
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
	private final int TCP_PORT = 2001;
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
     * Lock per la scrittura.
     * 
     * Non ci dovrebbero essere problemi di threading, ma questa precauzione
     * costa poco.
     */
    Lock writeLock;
    
    /**
     * Costruttore
     *
     * @param hostName nome dell'host (ad es. "1.2.3.4" o "www.pippo.com")
     * 
     * @throws un'Exception se incontra un errore
     */
    public TCPSerialBus(String hostName) throws EDSException {    	
		try {
			sock = SocketChannel.open(new InetSocketAddress(hostName, TCP_PORT));
			sock.configureBlocking(false);
		} catch (UnresolvedAddressException e) {
			throw new EDSException("Indirizzo non trovato: " + hostName);
		} catch (IOException e) {
			throw new EDSException("Impossibile ottenere gli stream: " + 
					e.getMessage());
		}
		try {
			readSelector = Selector.open();
			sock.register(readSelector, SelectionKey.OP_READ);
		} catch (IOException e) {
			throw new EDSException("Impossibile creare il Selector di lettura: " +
					e.getMessage());
		}
		try {
			writeSelector = Selector.open();
			sock.register(writeSelector, SelectionKey.OP_WRITE);
		} catch (IOException e) {
			throw new EDSException("Impossibile creare il Selector di " +
					"scrittura: " +	e.getMessage());
		}
		tcpSerialBusReader = new TCPSerialBusReader(readSelector, this, sock);
		new Thread(tcpSerialBusReader).start();
		writeLock = new ReentrantLock();
    }
    
    /**
     * Invia un messaggio sul bus.
     * 
     * Eventuali errori di trasmissione vengono ignorati.
     * 
     * @param m the message to send
     */
    public void write(Message m) {
    	byte[] rawMessage = m.getBytesMessage();
    	ByteBuffer bb = ByteBuffer.allocate(rawMessage.length);
    	bb.put(rawMessage);
    	bb.rewind();
    	writeLock.lock();
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
    		System.err.println("Errore durante l'invio di dati via rete: " +
				e.getMessage());
    	} finally {
    		writeLock.unlock();
    	}
    }
    
    /**
     * Riceve il buffer con i dati da passare alla superclasse.
     */
    protected void setByteBuffer(ByteBuffer bb) {
    	this.byteBuffer = bb;
    }

	protected boolean hasData() {
		if (byteBuffer != null) {
				return byteBuffer.hasRemaining();
		} else {
			return false;
		}
	}

	protected byte readByte() throws IOException {
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
    	try {
    		readSelector.close();
    		writeSelector.close();
    		sock.close();
    	} catch (IOException e) {
    		System.err.println("Errore durante la chiusura della connessione: " +
    				e.getMessage());
    	}
    }
}
