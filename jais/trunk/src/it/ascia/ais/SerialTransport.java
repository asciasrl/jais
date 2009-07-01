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
 * Gestisce la comunicazione con il bus EDS attraverso una porta seriale locale.
 * 
 * Tutti i messaggi che passano vengono smistati all'oggetto BMCComputer 
 * passato al costruttore.
 * 
 * @author sergio, arrigo
 */
public class SerialTransport extends Transport {

    private static Enumeration portList;
    /**
     * Dove scrivere i messaggi.
     */
    private OutputStream outputStream;
    /**
     * Da dove leggere i messaggi.
     */
    private InputStream inputStream;
    private SerialPort serialPort;
	private static int INPUT_BUFFER_SIZE = 1024;
	private static int OUTPUT_BUFFER_SIZE = 1024;
	private static int RECEIVE_THRESHOLD = 8;
	private static int RECEIVE_FRAMING = 8;
	private static int RECEIVE_TIMEOUT = 100;

    public SerialTransport(Connector connector, String portName) throws AISException {
    	this(connector, portName, 9600);
    }

    public SerialTransport(Connector connector, String portName, int portSpeed) {
		this(connector, portName, portSpeed, INPUT_BUFFER_SIZE, OUTPUT_BUFFER_SIZE, RECEIVE_THRESHOLD, RECEIVE_FRAMING, RECEIVE_TIMEOUT);
	}


    /**
     * Costruttore
     *
     * @param portName nome della porta (ad es. "COM1" o "/dev/ttyUSB0")
     * @param connector Conettore da associare
     * @param portSpeed velocita' della porta (default 9600)
     * @param inputBufferSize dimensione in bytes del buffer
     * @param outputBufferSize dimensione in bytes del buffer 
     * @param receiveThreshold 
     * @param receiveFraming 
     * @param receiveTimeout in mS (multipli di 100)
     * 
     * @throws un'Exception se incontra un errore
     */
    public SerialTransport(Connector connector, String portName, int portSpeed, int inputBufferSize, int outputBufferSize, int receiveThreshold, int receiveFraming, int receiveTimeout) throws AISException {
    	super(connector);
        name = portName;  
    	logger.info("Connessione a " + portName + " speed " +  portSpeed);    	
    	CommPortIdentifier portId;
		try {
			portId = CommPortIdentifier.getPortIdentifier(portName);
		} catch (NoSuchPortException e) {
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
		    serialPort.setSerialPortParams(portSpeed, SerialPort.DATABITS_8, 
						   SerialPort.STOPBITS_1, 
						   SerialPort.PARITY_NONE);
		    logger.trace("Input buffer: "+serialPort.getInputBufferSize()+" bytes -> "+inputBufferSize);
		    serialPort.setInputBufferSize(inputBufferSize);
		    logger.debug("Input buffer: "+serialPort.getInputBufferSize()+" bytes");
		    logger.trace("Output buffer: "+serialPort.getOutputBufferSize()+" bytes -> "+outputBufferSize);
		    serialPort.setOutputBufferSize(outputBufferSize);
		    logger.debug("Output buffer: "+serialPort.getOutputBufferSize()+" bytes.");
		    try {
			    logger.trace("Receive Threshold: "+serialPort.getReceiveThreshold()+" bytes -> "+receiveThreshold);		    
		    	serialPort.enableReceiveThreshold(receiveThreshold);
			    logger.debug("Receive Threshold: "+serialPort.getReceiveThreshold()+" bytes.");		    
		    } catch (UnsupportedCommOperationException e ) {
		    	logger.debug("Receive Threshold not supportted");
		    }
		    try {
			    logger.trace("Receive Framing: "+serialPort.getReceiveFramingByte()+" bytes -> "+receiveFraming);		    
			    serialPort.enableReceiveFraming(receiveFraming);
			    logger.debug("Receive Framing: "+serialPort.getReceiveFramingByte()+" bytes.");		    
		    } catch (UnsupportedCommOperationException e ) {
		    	logger.debug("Receive Framing not supportted");
		    }
		    try {
			    logger.trace("Receive Timeout: "+serialPort.getReceiveTimeout()+" mS -> "+receiveTimeout);		    
			    serialPort.enableReceiveTimeout(receiveTimeout);
			    logger.debug("Receive Timeout: "+serialPort.getReceiveTimeout()+" mS");		    
		    } catch (UnsupportedCommOperationException e ) {
		    	logger.debug("Receive Timeout not supportted");
		    }
		    
		} catch (UnsupportedCommOperationException e) {
			throw new AISException("Impossibile configurare la porta: " + 
					e.getMessage());
		}
		
    	try {
    		serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
    	} catch (UnsupportedCommOperationException e) {  
	    	throw new AISException("Porta "+portName+" errore impostazione flow control: " + e.toString());
    	}
    	
		try {
		    inputStream = serialPort.getInputStream();
		    outputStream = serialPort.getOutputStream();
		} catch (IOException e) {
			throw new AISException("Impossibile ottenere gli stream: " + 
					e.getMessage());
		}
		
		try {
			serialPort.addEventListener(new SerialListener(portName));
		} catch (TooManyListenersException e) {
			throw new AISException("Troppi listeners sulla porta:" + e.getMessage());
		}
        serialPort.notifyOnDataAvailable(true);
        serialPort.notifyOnOverrunError(true);
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
			outputStream.write(b);
		} catch (IOException e) {
			logger.error("Errore scrittura");
		}
    }
        
    /**
     * Chiude la porta seriale.
     */
    public void close() {
    	try {
			inputStream.close();
	    	inputStream = null;
			outputStream.close();
			outputStream = null;
		} catch (IOException e) {
			logger.error("Exception in close():",e);
		}
    	serialPort.removeEventListener();
    	logger.trace("About to close()");
    	serialPort.close();
    	logger.debug("Chiuso.");
    }

	private class SerialListener implements SerialPortEventListener {
		
		private String name = null;
		
		private long counter = 0;
		
		private StringBuffer sb;

		public SerialListener(String portName) {
			name = portName;
			sb = new StringBuffer();
		}

		public void serialEvent(SerialPortEvent event) {
			if (name != null) {
				Thread.currentThread().setName("Listener-"+name);
				name = null;
			}
            if (event.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
            	try {
					while (inputStream.available() > 0) {
						int i = inputStream.read();
						if (i == -1) {
							logger.error("Nessun dato ricevuto");					
						} else {			
							counter = (counter + 1) % 10000;
							sb.append(" "+counter+":"+i);
							if (inputStream.available() == 0) {
								logger.trace("Buffer"+sb.toString());
								sb = new StringBuffer();
							}
							connector.received(i);
						}
					}
				} catch (IOException e) {
	    			logger.error("Errore di lettura: " + e.getMessage());
				}
            } else if (event.getEventType() == SerialPortEvent.OE) {
            	logger.error("Overrun error: "+event.getSource());
            }
		}
	}
}


