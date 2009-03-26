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

    public SerialTransport(Connector connector, String portName) throws AISException {
    	this(connector, portName, 9600);
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
    public SerialTransport(Connector connector, String portName, int portSpeed) throws AISException {
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
    	serialPort.close();
    	logger.debug("Chiuso.");
    }

	private class SerialListener implements SerialPortEventListener {
		
		private String name = null;

		public SerialListener(String portName) {
			name = portName;
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
							connector.received(i);
						}
					}
				} catch (IOException e) {
	    			logger.error("Errore di lettura: " + e.getMessage());
				}
            }
		}
	}
}


