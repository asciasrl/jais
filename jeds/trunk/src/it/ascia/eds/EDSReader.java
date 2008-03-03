package it.ascia.eds;

import it.ascia.eds.msg.Message;
import it.ascia.eds.msg.MessageParser;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Enumeration;
import java.util.TooManyListenersException;

import gnu.io.*;

/**
 * Apre il bus e decodifica i messaggi del protocollo EDS scrivendoli su console
 * 
 * @author sergio
 *
 */
public class EDSReader 
	implements Runnable, SerialPortEventListener {

    static CommPortIdentifier portId;
    static Enumeration	      portList;
    InputStream		      inputStream;
    SerialPort		      serialPort;
    Thread		      readThread;
    MessageParser mp;
	Message m;
	
    /**
     * Method declaration
     *
     * @param args
     */
    public static void main(String[] args) {
	    String		      defaultPort = "/dev/ttyUSB0";
	
	 	if (args.length > 0) {
		    defaultPort = args[0];
		}
	 	EDSReader reader = new EDSReader(defaultPort);
    }
    
    /**
     * Constructor declaration
     *
     */
    public EDSReader(String portName) {
        boolean		      portFound = false;
        
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
    		serialPort = (SerialPort) portId.open("EDSReaderApp", 2000);
    	} catch (PortInUseException e) {
    		// TODO
    	}

		try {
		    inputStream = serialPort.getInputStream();
		} catch (IOException e) {
			// TODO
		}

		mp = new MessageParser();
		
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

		readThread = new Thread(this);

		readThread.start();
    }

    /**
     * Method declaration
     */
    public void run() {
		try {
			System.out.println("Running ...");
			Thread.sleep(20000);
		} catch (InterruptedException e) {}
    } 

    /**
     * Risponde a eventi ricevuti dalla porta seriale.
     * 
     * Legge i dati e chiama i metodi della superclasse che li interpretano.
     *
     * @param event L'evento ricevuto dalla seriale
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
					    		//if (!m.getTipoMessaggio().equals("Aknowledge")) {
					    			System.out.println((new Date()).toString() + "\r\n" + m);
					    			//System.out.println(m.toHexString());
					    		//}
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
