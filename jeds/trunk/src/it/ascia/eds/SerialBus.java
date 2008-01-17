package it.ascia.eds;

// Messaggi
import it.ascia.eds.msg.ComandoUscitaDimmerMessage;
import it.ascia.eds.msg.ComandoUscitaMessage;
import it.ascia.eds.msg.Message;
import it.ascia.eds.msg.MessageParser;
import it.ascia.eds.msg.RichiestaModelloMessage;
import it.ascia.eds.msg.RichiestaStatoMessage;
import it.ascia.eds.msg.RispostaModelloMessage;
import it.ascia.eds.msg.VariazioneIngressoMessage;

import it.ascia.eds.device.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.TooManyListenersException;
import java.util.Map;
import java.util.HashMap;

import gnu.io.*;

/**
 * Gestisce la comunicazione con il bs EDS attraverso un convertitore seriale
 * 
 * Tutti i messaggi che passano vengono smistati all'oggetto BMCComputer 
 * passato al costruttore.
 * 
 * @author sergio, arrigo
 */
public class SerialBus implements Bus, SerialPortEventListener {

    static CommPortIdentifier portId;
    static Enumeration	      portList;
    InputStream		      inputStream;
    OutputStream       outputStream;
    static boolean	      outputBufferEmptyFlag = false;
    SerialPort		      serialPort;
    MessageParser mp;
	Message m;
	
	/**
	 * Quanto tempo aspettare dopo un ping.
	 * 
	 * Nel caso peggiore (1200 bps), la trasmissione di un messaggio richiede 
	 * 8 / 120 = 660 msec. 
	 */
	static final int PING_WAIT = 1000;
	
	/**
	 * I device presenti nel bus
	 */
    private Map devices;
    
    /**
     * Il BMC "finto" che corrisponde a questo computer
     */
    private Device bmcComputer;
    
    /**
     * Indirizzo di un device che stiamo pingando.
     * 
     * Questo indirizzo non appartiene a nessun elemento di devices
     */
    private int pingedDevice;
    /**
     * Il messaggio di pong (risposta al ping) che abbiamo ricevuto
     */
    private Message pongMessage;
    
    /**
     * Costruttore
     *
     * @param portName nome della porta (ad es. "COM1" o "/dev/ttyUSB0")
     * 
     * @throws un'Exception se incontra un errore
     */
    public SerialBus(String portName) throws Exception {
        boolean portFound = false;
        devices = new HashMap();
        bmcComputer = null;
        pingedDevice = -1; // Il minimo è 0
        
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

		mp = new MessageParser();
		
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

    public void setBMCComputer(BMCComputer bmcComputer) {
    	this.bmcComputer = bmcComputer;
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
					    		m = mp.getMessage();
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
     * Invia un messaggio a tutti i BMC destinatari
     * 
     * Stampa un messaggio su stderr se il messaggio è per un BMC che non è in 
     * lista, né in fase di ping.
     * 
     * Il BMCComputer riceve tutti i messaggi.
     * 
     * @param m il messaggio da inviare
     */
    protected void dispatchMessage(Message m) {
    	if (m.getSender() == pingedDevice) { // FIXME: dovremmo controllare anche il contenuto 
    		// Se è un pong per un discovery, non interessa agli altri
    		System.out.println("Ricevuto un pong!");
    		pongMessage = m;
    	} else {
    		if (m.isBroadcast()) { // Mandiamo il messaggio a tutti
    			Iterator it = devices.values().iterator();
    			while (it.hasNext()) {
    				Device bmc = (Device)it.next();
    				bmc.receiveMessage(m);
    			}
    		} else { // Non e' un messaggio broadcast
    			int rcpt = m.getRecipient();
    			Device bmc = (Device)devices.get(new Integer(rcpt));
    			if (bmc != null) {
    				bmc.receiveMessage(m);
    			} else if ((bmcComputer == null) || 
    						(rcpt != bmcComputer.getAddress())) {
    				System.err.println("Ricevuto un messaggio per il BMC " + 
    						rcpt + " che non conosco:");
    				System.err.println((new Date()).toString() + "\r\n" + m);
    			}
    		}
    	} // If non è un pong
    	if (bmcComputer != null) 
    		bmcComputer.receiveMessage(m);
    }
    
    /*
     * 
     */
    public BMC discoverBMC(int address) throws Exception {
    	BMC retval;
    	// Gia' abbiamo il BMC in lista?
    	retval = (BMC)devices.get(new Integer(address));
    	if (retval == null) {
    		if (bmcComputer == null ){
         		throw new Exception("Non esiste un BMCComputer su questo bus");
         	}
    		pongMessage = null;
    		pingedDevice = address;
    		write(new RichiestaModelloMessage(address, 
    				bmcComputer.getAddress()));
    		Thread.sleep(PING_WAIT);
    		if (pongMessage != null) {
    			int model;
    			RispostaModelloMessage m = (RispostaModelloMessage) pongMessage;
    			model = m.getModello();
    			switch(model) {
    			case 88:
    			case 8:
    			case 40:
    			case 60:
    			case 44:
    				retval = new BMCStandardIO(address, model);
    				break;
    			case 41:
    			case 61:
    			case 81:
    				retval = new BMCIR(address, model);
    				break;
    			case 101:
    			case 102:
    			case 103:
    			case 104:
    			case 106:
    			case 111:
    				retval = new BMCDimmer(address, model);
    				break;
    			case 131:
    				retval = new BMCIntIR(address, model);
    				break;
    			case 152:
    			case 154:
    			case 156:
    			case 158:
    				retval = new BMCScenarioManager(address, model);
    				break;
    			case 127:
    				retval = new BMCChronoTerm(address, model);
    				break;
    			default:
    				System.err.println("Modello di BMC sconosciuto: " + 
    						model);
    				retval = null;
    			}
    		} else {
    			retval = null;
    		}
    		pingedDevice = -1;
    		if (retval != null) {
    			devices.put(new Integer(address), retval);
    		}
    	}
    	return retval;
    }
    
    /**
     * Chiude la porta seriale.
     */
    public void close() {
    	serialPort.close();
    }
}
