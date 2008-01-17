package it.ascia.eds;

import it.ascia.eds.msg.Message;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Enumeration;
import java.util.TooManyListenersException;

import gnu.io.*;

/**
 * Gestisce la comunicazione con il bs EDS attraverso un convertitore seriale
 * 
 * Tutti i messaggi che passano vengono smistati all'oggetto BMCComputer 
 * passato al costruttore.
 * 
 * @author sergio, arrigo
 */
public class SerialBus extends Bus implements SerialPortEventListener {

    static CommPortIdentifier portId;
    static Enumeration	      portList;
    InputStream		      inputStream;
    OutputStream       outputStream;
    static boolean	      outputBufferEmptyFlag = false;
    SerialPort		      serialPort;	
    
    /**
     * Costruttore
     *
     * @param portName nome della porta (ad es. "COM1" o "/dev/ttyUSB0")
     * 
     * @throws un'Exception se incontra un errore
     */
    public SerialBus(String portName) throws Exception {
        boolean portFound = false;
        
    	portList = CommPortIdentifier.getPortIdentifiers();

    	while (!portFound && portList.hasMoreElements()) {
    	    portId = (CommPortIdentifier) portList.nextElement();
    	    System.out.println(portId.getName());
    	    if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
	    		if (portId.getName().equals(portName)) {
	    		    System.out.println("Found port: "+portName);
	    		    portFound = true;
	    	    }
    	    }
    	} 
    	if (!portFound) {
    	    throw new Exception("port " + portName + " not found.");
    	} 

    	try {
    		serialPort = (SerialPort) portId.open("SerialBus", 2000);
    	} catch (PortInUseException e) {
	    	throw new Exception("Porta in uso: " + e.toString());
    	}

		try {
		    inputStream = serialPort.getInputStream();
		    outputStream = serialPort.getOutputStream();
		} catch (IOException e) {
			throw new Exception("Impossibile ottenere gli stream: " + 
					e.getMessage());
		}
		
		try {
		    serialPort.addEventListener(this);
		} catch (TooManyListenersException e) {
			throw new Exception("Troppi listeners sulla porta:" + 
					e.getMessage());
		}

		serialPort.notifyOnDataAvailable(true);

		try {
		    serialPort.setSerialPortParams(9600, SerialPort.DATABITS_8, 
						   SerialPort.STOPBITS_1, 
						   SerialPort.PARITY_NONE);
		} catch (UnsupportedCommOperationException e) {
			throw new Exception("Errore durante l'impostazione dei parametri:" +
					e.getMessage());
		}
		

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
    	try {
			outputBufferEmptyFlag = false;
			m.write(outputStream);
			// TODO: evitare di saturare il buffer di tx
//			while (!outputBufferEmptyFlag) {
//				Thread.sleep(1);
//			}    
//    	} catch (InterruptedException e) {
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
		    byte[] readBuffer = new byte[8];
	
		    try {
				while (inputStream.available() > 0) {
				    int numBytes = inputStream.read(readBuffer);
				    if (numBytes > 0) {
				    	for (int i = 0; i < numBytes; i++) {
							byte b = readBuffer[i];
					    	mp.push(b);
					    	if (mp.isValid()) {
					    		Message m = mp.getMessage();
//					    		if (!m.getTipoMessaggio().equals("Aknowledge")) {
//					    			System.out.println((new Date()).toString() + "\r\n" + m);
//					    		}
					    		dispatchMessage(m);

					    		//mp.clear();
					    	}
						}
				    }
				}			
		    } catch (IOException e) {}
	
		    break;
		}
    }
        
    /**
     * Chiude la porta seriale.
     */
    public void close() {
    	serialPort.close();
    }
}
