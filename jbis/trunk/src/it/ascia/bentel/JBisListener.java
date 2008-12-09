/**
 * Copyright (C) 2008 ASCIA S.r.l.
 */
package it.ascia.bentel;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.TooManyListenersException;

import org.apache.log4j.Logger;

import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

/**
 * Interfaccia di comunicazione con la centralina.
 * 
 * <p>Questa classe e' in grado solo di ricevere i messaggi di log che la
 * centralina manda sulla porta seriale.</p>
 * 
 * FIXME: questa classe non funziona. Deve essere trasformata in Connector +
 * Device.
 * 
 * @author arrigo
 *
 */
public class JBisListener implements SerialPortEventListener {
	/**
	 * Dimensione massima del log eventi.
	 */
	private static final int MAX_LOG_SIZE = 200;
	/**
	 * Identificatore della nostra porta seriale.
	 */
	private CommPortIdentifier portId;
	/**
	 * La nostra porta seriale.
	 */
	private SerialPort serialPort;
	/**
     * Da dove leggere i messaggi.
     */
    private InputStream inputStream;
	/**
	 * Il nostro logger.
	 */
	private Logger logger;
	/**
	 * Il buffer temporaneo in cui mettiamo i dati ricevuti.
	 * 
	 * <p>Sembra che i pacchetti siano di 8 byte, ma i dati dell'evento ne
	 * occupano 7.</p>
	 */
	private byte buffer[];
	/**
	 * Indice all'interno del buffer. Indica il primo byte libero.
	 */
	private int bufferIndex;
	/**
	 * Registro eventi.
	 */
	private LinkedList eventLog;
	/**
	 * Chi avvisare quando si verifica un allarme.
	 * 
	 * FIXME
	 */
	// private AlarmReceiver alarmReceiver;
	
	/**
	 * Crea un nuovo evento a partire dai dati nel buffer. Inserisce l'evento
	 * nel log e avvisa chi di competenza.
	 * 
	 * <p>I dati devono essere contenuti dentro buffer.</p>
	 */
	private void processNewEvent() {
		Event event = new Event(buffer, 0);
		logger.debug(event.getInfo());
		if (event.isAlarm()) {
			// alarmReceiver.alarmReceived(event.getAlarm()); FIXME
		}
		// Gestione registro
		eventLog.addFirst(event);
		while (eventLog.size() > MAX_LOG_SIZE) {
			eventLog.removeLast();
		}
	}
	
	/**
	 * Legge dati dalla porta seriale e crea nuovi eventi.
	 * 
	 * @param n numero di byte da leggere.
	 */
	private void readBytes(int n) throws IOException {
		for (int i = 0; i < n; i++) {
			int b = (byte)inputStream.read();
			if (b == -1) {
				throw new IOException("Fine dello stream!");
			}
			buffer[bufferIndex] = (byte)b;
			// logger.trace("" + (b & 0xff));
			bufferIndex++;
			if (bufferIndex == buffer.length) {
				// Abbiamo un evento pronto da leggere?
				// Controlliamo il checksum
				int j;
				byte c = 0;
				for (j = 0; j < 7; j++) {
					c += buffer[j];
				}
				if (buffer[7] == c) {
					// Dati corretti.
					processNewEvent();
					bufferIndex = 0;
				} else {
					logger.warn("Checksum errato.");
					// Quando la centralina si accende, manda due byte pari a 0.
					// Se questo e' il caso, la soluzione e' scartare il primo 
					// byte del buffer.
					for (j = 1; j < buffer.length; j++) {
						buffer[j-1] = buffer[j];
					}
					bufferIndex--;
				}
			}
		}
	}
	
	/**
	 * Costruttore.
	 * 
	 * @param portName nome della porta seriale (ad es. "COM1")
	 */
	public JBisListener(String portName/*, AlarmReceiver ar FIXME */) 
	throws JBisException {
		boolean portFound = false;
		Enumeration	portList;
		logger = Logger.getLogger(getClass());
	    logger.info("Bentel Listener (C) Ascia S.r.l. 2008");
	    buffer = new byte[8];
	    bufferIndex = 0;
	    eventLog = new LinkedList();
	    // alarmReceiver = ar; FIXME
    	portList = CommPortIdentifier.getPortIdentifiers();
    	while (!portFound && portList.hasMoreElements()) {
    	    portId = (CommPortIdentifier) portList.nextElement();
    	    logger.debug("Detected port: " + portId.getName());
    	    if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
	    		if (portId.getName().equals(portName)) {
	    		    logger.info("Found port: "+portName);
	    		    portFound = true;
	    	    }
    	    }
    	} 
    	if (!portFound) {
    	    throw new JBisException("porta " + portName + " non trovata.");
    	} 

    	logger.info("Connessione a " + portName); 
    	try {
    		serialPort = (SerialPort) portId.open("SerialTransport", 2000);
    	} catch (PortInUseException e) {
	    	throw new JBisException("Porta in uso: " + e.toString());
    	}

		try {
		    inputStream = serialPort.getInputStream();
		    // outputStream = serialPort.getOutputStream();
		} catch (IOException e) {
			throw new JBisException("Impossibile ottenere gli stream: " + 
					e.getMessage());
		}
		
		try {
		    serialPort.addEventListener(this);
		} catch (TooManyListenersException e) {
			throw new JBisException("Troppi listeners sulla porta:" + 
					e.getMessage());
		}

		serialPort.notifyOnDataAvailable(true);

		try {
		    serialPort.setSerialPortParams(9600, SerialPort.DATABITS_8, 
						   SerialPort.STOPBITS_1, 
						   SerialPort.PARITY_EVEN);
		} catch (UnsupportedCommOperationException e) {
			throw new JBisException("Errore durante l'impostazione dei " +
					"parametri:" + e.getMessage());
		}
	}

	/**
	 * Reagisce a un evento della porta seriale.
	 * 
	 * <p>Questo metodo viene chiamato dal driver della porta.</p>
	 */
	public void serialEvent(SerialPortEvent event) {
		switch (event.getEventType()) {
		case SerialPortEvent.BI:
		case SerialPortEvent.OE:
		case SerialPortEvent.FE:
		case SerialPortEvent.PE:
		case SerialPortEvent.CD:
		case SerialPortEvent.CTS:
		case SerialPortEvent.DSR:
		case SerialPortEvent.RI:
		case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
			break;
		case SerialPortEvent.DATA_AVAILABLE:
			try {
					readBytes(inputStream.available());
				} catch (IOException e) {
					logger.error("Errore durante la lettura: " + 
							e.getMessage());
				}
			break;
		}
	}
	
	/**
	 * Chiude la porta seriale.
	 * 
	 * <p>Questo metodo deve essere chiamato al momento di uscire dal 
	 * programma.</p>
	 */
	public void close() {
		logger.info("Chisura...");
		serialPort.close();
	}
}
