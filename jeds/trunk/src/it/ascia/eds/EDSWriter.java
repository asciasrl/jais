package it.ascia.eds;

import it.ascia.eds.msg.ComandoUscitaDimmerMessage;
import it.ascia.eds.msg.ComandoUscitaMessage;
import it.ascia.eds.msg.EDSMessage;
import it.ascia.eds.msg.EDSMessageParser;
import it.ascia.eds.msg.RichiestaModelloMessage;
import it.ascia.eds.msg.RichiestaStatoMessage;
import it.ascia.eds.msg.VariazioneIngressoMessage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Enumeration;
import java.util.TooManyListenersException;

import gnu.io.*;

/**
 * Apre il bus e trasmette messaggi del protocollo EDS, leggendo le risposte
 * 
 * @author sergio
 *
 */
public class EDSWriter 
	implements Runnable, SerialPortEventListener {

    static CommPortIdentifier portId;
    static Enumeration	      portList;
    InputStream		      inputStream;
    OutputStream       outputStream;
    static boolean	      outputBufferEmptyFlag = false;
    SerialPort		      serialPort;
    Thread		      readThread;
    EDSMessageParser mp;
	EDSMessage m;
	
    /**
     * Method declaration
     *
     * @param args
     *
     * @see
     */
    public static void main(String[] args) {
	    String		      defaultPort = "COM1";
	
	 	if (args.length > 0) {
		    defaultPort = args[0];
		}
	 	EDSWriter writer = new EDSWriter(defaultPort);
    }
    
    /**
     * Constructor declaration
     *
     *
     * @see
     */
    public EDSWriter(String portName) {
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
    	    System.out.println("port " + portName + " not found.");
    	    return;
    	} 

    	try {
    		serialPort = (SerialPort) portId.open("EDSWriterApp", 2000);
    	} catch (PortInUseException e) {
	    	System.out.println("Porta in uso");
	    	System.out.println(e.toString());
	    	System.exit(-1);
    	}

		try {
		    inputStream = serialPort.getInputStream();
		    outputStream = serialPort.getOutputStream();
		} catch (IOException e) {
			// TODO
		}

		mp = new EDSMessageParser();
		
		try {
		    serialPort.addEventListener(this);
		} catch (TooManyListenersException e) {
			// TODO
		}

		serialPort.notifyOnDataAvailable(true);

		try {
		    serialPort.setSerialPortParams(9600, SerialPort.DATABITS_8, 
						   SerialPort.STOPBITS_1, 
						   SerialPort.PARITY_NONE);
		} catch (UnsupportedCommOperationException e) {
			// TODO
		}

	    try {
	    	serialPort.notifyOnOutputEmpty(true);
	    } catch (Exception e) {
	    	System.out.println("Error setting event notification");
	    	System.out.println(e.toString());
	    	System.exit(-1);
	    }
		
		readThread = new Thread(this);

		readThread.start();
		
		scrivi();
    }

    /**
     * Method declaration
     *
     * @see
     */
    public void run() {
		try {
			System.out.println("Running ...");
			Thread.sleep(20000);
		} catch (InterruptedException e) {}
    }
    
    public void scrivi() {
    	try {
			/*for (int i = 1; i <= 255; i++) {
				scrivi(new RichiestaModelloMessage(i,0));
				Thread.sleep(20);
			}*/
    		
    		scrivi(new RichiestaModelloMessage(255,0));
			Thread.sleep(100);
			
    		//scrivi(new VariazioneIngressoMessage(5,0,1,0,1));
			//Thread.sleep(100);

    		/*
    		scrivi(new ComandoUscitaDimmerMessage(5,0,0,50));
			Thread.sleep(100);
			scrivi(new RichiestaStatoMessage(5,0,0x03));
			Thread.sleep(1000);
    		scrivi(new ComandoUscitaDimmerMessage(5,0,0,0));
			Thread.sleep(100);
			scrivi(new RichiestaStatoMessage(5,0,0x03));
			Thread.sleep(100);
			*/

			/*
			scrivi(new VariazioneIngressoMessage(255,0,0,0,1));
			Thread.sleep(100);
			scrivi(new VariazioneIngressoMessage(255,0,0,0,0));
			Thread.sleep(100);
			for (int i = 0; i <= 1000; i++) {
				scrivi(new RichiestaStatoMessage(255,0,0x03));
				Thread.sleep(100);
			}
			*/
			
			/*
			for (int i = 0; i <= 1000; i++) {
				scrivi(new ComandoUscitaDimmerMessage(255,0,0,0));
				Thread.sleep(10);
				scrivi(new RichiestaStatoMessage(255,0,0x03));
				Thread.sleep(20);
				scrivi(new RichiestaStatoMessage(255,0,0x03));
				Thread.sleep(20);
				scrivi(new RichiestaStatoMessage(255,0,0x03));
				Thread.sleep(20);
//				scrivi(new RichiestaStatoMessage(255,0,0x03));
//				Thread.sleep(20);
//				Thread.sleep(1000);
			}
			*/

			/*scrivi(new ComandoUscitaMessage(255, 0, 1, 0, 50, 1));
			Thread.sleep(100);
			scrivi(new RichiestaStatoMessage(255,0,0x03));
			Thread.sleep(10000);
			*/
			
			scrivi(new RichiestaStatoMessage(255,0,0x03));
			Thread.sleep(100);
			for (int i = 0; i <= 100; i++) {
				scrivi(new ComandoUscitaDimmerMessage(255,0,0,i));
				Thread.sleep(20);
				scrivi(new RichiestaStatoMessage(255,0,0xff));
				Thread.sleep(20);
			}
			/*scrivi(new ComandoUscitaDimmerMessage(255,0,0,50));
			Thread.sleep(100);
			scrivi(new RichiestaStatoMessage(255,0,0x03));
			Thread.sleep(100);
			*/
			for (int i = 0; i <= 1000; i++) {
				scrivi(new RichiestaStatoMessage(255,0,0x03));
				Thread.sleep(200);
			}

			System.out.println("Fine.");
	    	System.exit(0);						
    	} catch (InterruptedException e) {
    	}    
    }
    
    public void scrivi(EDSMessage m) {
    	try {
			outputBufferEmptyFlag = false;
			m.write(outputStream);
			while (!outputBufferEmptyFlag) {
				Thread.sleep(1);
			}    
    	} catch (InterruptedException e) {
		} catch (IOException e) {    		
		}    
    }

    /**
     * Method declaration
     *
     *
     * @param event
     *
     * @see
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
					    		m = mp.getMessage();
					    		if (!m.getTipoMessaggio().equals("Aknowledge")) {
					    			System.out.println((new Date()).toString() + "\r\n" + m);
					    		}
					    		//mp.clear();
					    	}
						}
				    }
				}			
		    } catch (IOException e) {}
	
		    break;
		}
    } 
    
}
