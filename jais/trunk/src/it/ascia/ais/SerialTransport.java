package it.ascia.ais;

import it.ascia.ais.Transport;
import it.ascia.ais.AISException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.TooManyListenersException;

import gnu.io.*;

/**
 * Gestisce la comunicazione con il bs EDS attraverso un convertitore seriale.
 * 
 * Tutti i messaggi che passano vengono smistati all'oggetto BMCComputer 
 * passato al costruttore.
 * 
 * @author sergio, arrigo
 */
public class SerialTransport extends Transport implements SerialPortEventListener {

    private static CommPortIdentifier portId;
    private static Enumeration	      portList;
    /**
     * Dove scrivere i messaggi.
     */
    private OutputStream outputStream;
    /**
     * Da dove leggere i messaggi.
     */
    private InputStream inputStream;
    private static boolean	      outputBufferEmptyFlag = false;
    private SerialPort		      serialPort;

    public SerialTransport(String portName) throws AISException {
    	this(portName, 9600);
    }

    /**
     * Costruttore
     *
     * @param portName nome della porta (ad es. "COM1" o "/dev/ttyUSB0")
     * @param connector Conettore da associare
     * @param portSpeed velocita' della porta (default 9600)
     * 
     * @throws un'Exception se incontra un errore
     */
    public SerialTransport(String portName, int portSpeed) throws AISException {
        name = portName;  
    	logger.info("Connessione a " + portName + " speed " +  portSpeed);    	
    	CommPortIdentifier portId;
		try {
			portId = CommPortIdentifier.getPortIdentifier(portName);
		} catch (NoSuchPortException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
	    	portList = CommPortIdentifier.getPortIdentifiers();
	    	while (portList.hasMoreElements()) {
	    	    portId = (CommPortIdentifier) portList.nextElement();
	    	    if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
		    	    logger.debug("Detected serial port: " + portId.getName());
	    	    }
	    	}
	    	throw new AISException("Porta "+portName+" non trovata: " + e.toString());
		}
    	try {
    		serialPort = (SerialPort) portId.open("SerialTransport", 2000);
    	} catch (PortInUseException e) {
	    	throw new AISException("Porta "+portName+" in uso: " + e.toString());
    	}

		try {
		    inputStream = serialPort.getInputStream();
		    outputStream = serialPort.getOutputStream();
		} catch (IOException e) {
			throw new AISException("Impossibile ottenere gli stream: " + 
					e.getMessage());
		}
		
		try {
		    serialPort.addEventListener(this);
		} catch (TooManyListenersException e) {
			throw new AISException("Troppi listeners sulla porta:" + 
					e.getMessage());
		}

		serialPort.notifyOnDataAvailable(true);

		try {
		    serialPort.setSerialPortParams(portSpeed, SerialPort.DATABITS_8, 
						   SerialPort.STOPBITS_1, 
						   SerialPort.PARITY_NONE);
		} catch (UnsupportedCommOperationException e) {
					e.getMessage();
		}
    }


    
    /**
     * Invia un messaggio sul transport.
     * 
     * Eventuali errori di trasmissione vengono ignorati.
     * 
     * @param m the message to send
     */
    public synchronized void write(byte[] b) {
    	try {
			outputBufferEmptyFlag = false;
			outputStream.write(b);
		} catch (IOException e) {    		
		}
    }

    /**
     * Evento ricevuto dalla porta seriale
     *
     * @param event l'evento ricevuto
     *
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
	
		    break;
		    
		case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
			outputBufferEmptyFlag = true;
		    break;
	
		case SerialPortEvent.DATA_AVAILABLE:
			// La superclasse sa che cosa fare
		    connector.readData();	
		    break;
		}
    }
        
    /**
     * Chiude la porta seriale.
     */
    public void close() {
    	serialPort.close();
    }

	public boolean hasData() {
		try {
			return (inputStream.available() > 0);
		} catch (IOException e) {
			logger.error("Impossibile verificare la presenza di dati:" +
					e.getMessage());
			return false;
		}
	}

	public byte readByte() throws IOException {
		return (byte)inputStream.read();
	}
}
