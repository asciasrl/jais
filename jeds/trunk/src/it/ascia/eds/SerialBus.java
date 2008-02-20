package it.ascia.eds;

import it.ascia.eds.msg.Message;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.TooManyListenersException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import gnu.io.*;

/**
 * Gestisce la comunicazione con il bs EDS attraverso un convertitore seriale.
 * 
 * Tutti i messaggi che passano vengono smistati all'oggetto BMCComputer 
 * passato al costruttore.
 * 
 * @author sergio, arrigo
 */
public class SerialBus extends Bus implements SerialPortEventListener {

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
    /**
     * Lock per la scrittura.
     * 
     * Non ci dovrebbero essere problemi di threading, ma questa precauzione
     * costa poco.
     */
    private Lock writeLock;
    
    /**
     * Costruttore
     *
     * @param portName nome della porta (ad es. "COM1" o "/dev/ttyUSB0")
     * 
     * @throws un'Exception se incontra un errore
     */
    public SerialBus(String portName, String busName) throws EDSException {
    	super(busName);
        boolean portFound = false;
        
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
    	    throw new EDSException("port " + portName + " not found.");
    	} 

    	logger.info("Connessione a " + portName); 
    	try {
    		serialPort = (SerialPort) portId.open("SerialBus", 2000);
    	} catch (PortInUseException e) {
	    	throw new EDSException("Porta in uso: " + e.toString());
    	}

		try {
		    inputStream = serialPort.getInputStream();
		    outputStream = serialPort.getOutputStream();
		} catch (IOException e) {
			throw new EDSException("Impossibile ottenere gli stream: " + 
					e.getMessage());
		}
		
		try {
		    serialPort.addEventListener(this);
		} catch (TooManyListenersException e) {
			throw new EDSException("Troppi listeners sulla porta:" + 
					e.getMessage());
		}

		serialPort.notifyOnDataAvailable(true);

		try {
		    serialPort.setSerialPortParams(9600, SerialPort.DATABITS_8, 
						   SerialPort.STOPBITS_1, 
						   SerialPort.PARITY_NONE);
		} catch (UnsupportedCommOperationException e) {
			throw new EDSException("Errore durante l'impostazione dei parametri:" +
					e.getMessage());
		}
		writeLock = new ReentrantLock();

		// Questo blocco blocca (!) la ricezione
//	    try {
//	    	serialPort.notifyOnOutputEmpty(true);
//	    } catch (Exception e) {
//	    	throw new Exception("Error setting event notification: " + 
//	    			e.toString());
//	    }
				
//		scrivi();
    }
    
//    public void scrivi() {
//    	try {
//			/*for (int i = 1; i <= 255; i++) {
//				scrivi(new RichiestaModelloMessage(i,0));
//				Thread.sleep(20);
//			}*/
//    		
//    		scrivi(new RichiestaModelloMessage(255,0));
//			Thread.sleep(100);
//			
//    		//scrivi(new VariazioneIngressoMessage(5,0,1,0,1));
//			//Thread.sleep(100);
//
//    		/*
//    		scrivi(new ComandoUscitaDimmerMessage(5,0,0,50));
//			Thread.sleep(100);
//			scrivi(new RichiestaStatoMessage(5,0,0x03));
//			Thread.sleep(1000);
//    		scrivi(new ComandoUscitaDimmerMessage(5,0,0,0));
//			Thread.sleep(100);
//			scrivi(new RichiestaStatoMessage(5,0,0x03));
//			Thread.sleep(100);
//			*/
//
//			/*
//			scrivi(new VariazioneIngressoMessage(255,0,0,0,1));
//			Thread.sleep(100);
//			scrivi(new VariazioneIngressoMessage(255,0,0,0,0));
//			Thread.sleep(100);
//			for (int i = 0; i <= 1000; i++) {
//				scrivi(new RichiestaStatoMessage(255,0,0x03));
//				Thread.sleep(100);
//			}
//			*/
//			
//			/*
//			for (int i = 0; i <= 1000; i++) {
//				scrivi(new ComandoUscitaDimmerMessage(255,0,0,0));
//				Thread.sleep(10);
//				scrivi(new RichiestaStatoMessage(255,0,0x03));
//				Thread.sleep(20);
//				scrivi(new RichiestaStatoMessage(255,0,0x03));
//				Thread.sleep(20);
//				scrivi(new RichiestaStatoMessage(255,0,0x03));
//				Thread.sleep(20);
////				scrivi(new RichiestaStatoMessage(255,0,0x03));
////				Thread.sleep(20);
////				Thread.sleep(1000);
//			}
//			*/
//
//			/*scrivi(new ComandoUscitaMessage(255, 0, 1, 0, 50, 1));
//			Thread.sleep(100);
//			scrivi(new RichiestaStatoMessage(255,0,0x03));
//			Thread.sleep(10000);
//			*/
//			
//			scrivi(new RichiestaStatoMessage(255,0,0x03));
//			Thread.sleep(100);
//			for (int i = 0; i <= 100; i++) {
//				scrivi(new ComandoUscitaDimmerMessage(255,0,0,i));
//				Thread.sleep(20);
//				scrivi(new RichiestaStatoMessage(255,0,0xff));
//				Thread.sleep(20);
//			}
//			/*scrivi(new ComandoUscitaDimmerMessage(255,0,0,50));
//			Thread.sleep(100);
//			scrivi(new RichiestaStatoMessage(255,0,0x03));
//			Thread.sleep(100);
//			*/
//			for (int i = 0; i <= 1000; i++) {
//				scrivi(new RichiestaStatoMessage(255,0,0x03));
//				Thread.sleep(200);
//			}
//
//			System.out.println("Fine.");
//	    	System.exit(0);						
//    	} catch (InterruptedException e) {
//    	}    
//    }
    
    /**
     * Invia un messaggio sul bus.
     * 
     * Eventuali errori di trasmissione vengono ignorati.
     * 
     * @param m the message to send
     */
    public void write(Message m) {
    	writeLock.lock();
    	try {
			outputBufferEmptyFlag = false;
			m.write(outputStream);
			// TODO: evitare di saturare il buffer di tx
//			while (!outputBufferEmptyFlag) {
//				Thread.sleep(1);
//			}    
//    	} catch (InterruptedException e) {
		} catch (IOException e) {    		
		} finally {
			writeLock.unlock();
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
		    readData();	
		    break;
		}
    }
        
    /**
     * Chiude la porta seriale.
     */
    public void close() {
    	serialPort.close();
    }

	protected boolean hasData() {
		try {
			return (inputStream.available() > 0);
		} catch (IOException e) {
			logger.error("Impossibile verificare la presenza di dati:" +
					e.getMessage());
			return false;
		}
	}

	protected byte readByte() throws IOException {
		return (byte)inputStream.read();
	}
}
