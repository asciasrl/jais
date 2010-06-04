package it.ascia.ais;

import it.ascia.ais.Transport;
import it.ascia.ais.AISException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.TooManyListenersException;

import gnu.io.*;

/**
 * Gestisce la comunicazione attraverso una porta seriale locale.
 * 
 * @author sergio, arrigo
 */
public class SerialTransport extends Transport {
    
    public static boolean DEBUG = false;
	/**
     * Dove scrivere i messaggi.
     */
    private OutputStream outputStream;
    /**
     * Da dove leggere i messaggi.
     */
    private InputStream inputStream;
    private SerialPort serialPort;
	private String portName;
	
	private boolean closed = true;
	
	private int portSpeed;
	private int databits = SerialPort.DATABITS_8;
	private int parity = SerialPort.PARITY_NONE;
	private int stopbits = SerialPort.STOPBITS_1;
	private int inputBufferSize;
	private int outputBufferSize;
	private int receiveThreshold;
	private int receiveFraming;
	private int receiveTimeout;
	
	private static boolean rxtxLoaded = false;
	
	private static int INPUT_BUFFER_SIZE = 1024;
	private static int OUTPUT_BUFFER_SIZE = 1024;
	private static int RECEIVE_THRESHOLD = 8;
	private static int RECEIVE_FRAMING = 8;
	private static int RECEIVE_TIMEOUT = 100;

    public SerialTransport(String portName) throws AISException {
    	this(portName, 9600);
    }

    public SerialTransport(String portName, int portSpeed) {
		this(portName, portSpeed, SerialPort.DATABITS_8, SerialPort.PARITY_NONE, SerialPort.STOPBITS_1);
	}
    
    public SerialTransport(String portName, int portSpeed, int databits, int parity, int stopbits) {
    	this(portName,portSpeed, databits, parity, stopbits, INPUT_BUFFER_SIZE, OUTPUT_BUFFER_SIZE, RECEIVE_THRESHOLD, RECEIVE_FRAMING, RECEIVE_TIMEOUT);
    }
   
    public String getInfo() {
    	String s = getClass().getSimpleName() + " " + name + ": ";
    	if (serialPort == null) {
    		s += "Disconnected";
    	} else {
    		s += serialPort.getBaudRate()+" "+serialPort.getDataBits()+parityChar(serialPort.getParity())+serialPort.getStopBits();
    	}
		return s;
    }
    
    private char parityChar(int parity) {
		switch (parity) {
			case SerialPort.PARITY_NONE: return 'N';
			case SerialPort.PARITY_EVEN: return 'E';
			case SerialPort.PARITY_MARK: return 'M';
			case SerialPort.PARITY_ODD:  return 'O';
			case SerialPort.PARITY_SPACE: return 'S';
		}
		return '?';
    }

    /**
     * Costruttore
     * 
     * Specificando "auto" come nome della porta, viene usata la prima porta seriale disponibile
     *
     * @param portName nome della porta (ad es. "COM1" o "/dev/ttyUSB0" o "auto")
     * @param connector Conettore da associare
     * @param portSpeed velocita' della porta (default 9600)
     * @param databits
     * @param parity
     * @param stopbits
     * @param inputBufferSize dimensione in bytes del buffer
     * @param outputBufferSize dimensione in bytes del buffer 
     * @param receiveThreshold 
     * @param receiveFraming 
     * @param receiveTimeout in mS (multipli di 100)
     * 
     * @throws un'Exception se incontra un errore
     */    
    public SerialTransport(String portName, int portSpeed, int databits, int parity, int stopbits, int inputBufferSize, int outputBufferSize, int receiveThreshold, int receiveFraming, int receiveTimeout) throws AISException {
        name = portName;
    	this.portName = portName;
    	this.portSpeed = portSpeed;
    	this.databits = databits;
    	this.parity = parity;
    	this.stopbits = stopbits;
    	this.inputBufferSize = inputBufferSize;
    	this.outputBufferSize = outputBufferSize;
    	this.receiveThreshold = receiveThreshold;
    	this.receiveFraming = receiveFraming;
    	this.receiveTimeout = receiveTimeout;
    	loadLibrary();
    	open();
    }
    
	private void loadLibrary() {
		
		if (rxtxLoaded) {
			return;
		}
		String oSName = System.getProperty("os.name");
		String oSVersion = System.getProperty("os.version");
		String oSArch = System.getProperty("os.arch");
		String osLibPath = null;
		if (oSName.startsWith("W")) {
			if (oSArch.contains("64")) {
				osLibPath = "win64";
			} else {
				osLibPath = "win32";
			}
		} else if (oSName.startsWith("L")) {
			if (oSArch.contains("64")) {
				osLibPath = "x86_64-unknown-linux-gnu";
			} else {
				osLibPath = "i686-pc-linux-gnu";
			}			
		} else if (oSName.startsWith("M")) {
			osLibPath = "mac-10.5";
		} else if (oSName.startsWith("S")) {
			if (oSArch.contains("64")) {
				osLibPath = "sparc-sun-solaris2.10-64";
			} else {
				osLibPath = "sparc-sun-solaris2.10-32";
			}			
		}
		
		if (osLibPath == null) {
			throw(new AISException("Operating system unsupported by rxtx: "+oSName+" / " + oSVersion+" / " + oSArch));		
		} else {
			logger.debug("Loading rxtx for "+oSName+" / " + oSVersion+" / " + oSArch+" path="+osLibPath);
		}
		
		// determinare combinazioni OSname / OSarch e di conseguenza il nomi della DLL
		// modificare "java.library.path" NON funziona (viene letta all'avvio della JVM)
		// terminare il path assoluto usando come base "java.library.path" ed aggiungere la subdir di rxtx
		// modificare rxtx-2.2pre2.jar in modo che NON carichi la libreria in ogni classe
		
		String libPath = System.getProperty("java.library.path").split(System.getProperty("path.separator"))[0];
		
		String fs = System.getProperty("file.separator");
		
		File f = new File(libPath + fs + "rxtx-2.2pre2-bins"+fs+osLibPath+fs+System.mapLibraryName("rxtxSerial"));
		
		if (f.exists()) {
			String path = f.getAbsolutePath();
			logger.debug("Loading rxtx library from: "+path);			
			System.load(path);
		} else {
			throw(new AISException("Library rxtx not found in:"+f.getPath()));
		}
		
		rxtxLoaded = true;
	}

	private void open() {
    	logger.debug("Opening serial port '" + portName + "' "+portSpeed+" "+databits+parityChar(parity)+stopbits);
    	if (portName.toLowerCase().equals("auto")) {						
	    	Enumeration portList = CommPortIdentifier.getPortIdentifiers();
	    	while (portList.hasMoreElements()) {
	    		CommPortIdentifier portId = (CommPortIdentifier) portList.nextElement();
	    	    if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
					String autoPortName = portId.getName();
		    	    logger.trace("Detected serial port: " + autoPortName);
		        	try {
		        		serialPort = (SerialPort) portId.open("SerialTransport", 1000);
		            	logger.debug("Opening autodetected serial port '" + autoPortName + "'");
		            	name = autoPortName + " (auto)";
		        		break;
		        	} catch (PortInUseException e) {
		    	    	logger.trace("Port " + autoPortName + " in use: " + e.toString());
		        	}
	    	    }
	    	}
	    	if (serialPort == null) {
	    		throw new AISException("Cannot find one available serial port");
	    	}
    	} else {
    		try {
    			CommPortIdentifier portId = CommPortIdentifier.getPortIdentifier(portName);
	    		serialPort = (SerialPort) portId.open("SerialTransport", 2000);
    		} catch (NoSuchPortException e) {
    			throw new AISException("Port "+portName+" not found: " + e.toString());
	    	} catch (PortInUseException e) {
		    	throw new AISException("Port '"+portName+"' already in use: " + e.toString());
	    	}
    	}
		try {
		    serialPort.setSerialPortParams(portSpeed, databits, stopbits, parity);
		} catch (UnsupportedCommOperationException e) {
			logger.fatal(e);
			throw new AISException("Unable to configure port: " + e.getMessage());
		}
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
    	try {
    		serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
    	} catch (UnsupportedCommOperationException e) {  
	    	throw new AISException("Porta "+portName+" errore impostazione flow control: " + e.toString());
    	}
    	
		try {
		    inputStream = serialPort.getInputStream();
		    outputStream = serialPort.getOutputStream();
		} catch (IOException e) {
			throw new AISException("Impossibile ottenere gli stream: " + e.getMessage());
		}
		
		try {
			serialPort.addEventListener(new SerialListener(serialPort.getName()));
		} catch (TooManyListenersException e) {
			throw new AISException("Troppi listeners sulla porta:" + e.getMessage());
		}
        serialPort.notifyOnDataAvailable(true);
        serialPort.notifyOnOverrunError(true);
        logger.debug("Opened serial transport "+this);
        closed = false;
    }

	/**
	 * Try to reopen connection
	 */
    private void reopen() {
    	if (closed) {
    		logger.warn("Cannot reopen closed transport");
    		return;
    	}
    	logger.info("Trying to reopen serial port");
    	close();
    	try {
    		open();
    	} catch (Exception e) {
    		logger.fatal("Unable to reopen:",e);
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
			outputStream.write(b);
			outputStream.flush();
		} catch (NullPointerException e) {
			logger.fatal("Output stream not available");
			reopen();
		} catch (IOException e) {
			logger.fatal("Write error: ",e);
			reopen();
		}
    }
        
    /**
     * Chiude la porta seriale.
     */
    public void close() {
    	closed = true;
    	try {
    		if (inputStream != null) {
	        	logger.trace("Closing input stream.");
				inputStream.close();
		    	inputStream = null;
    		}
    		if (outputStream != null) {
	        	logger.trace("Closing output stream.");
				outputStream.close();
				outputStream = null;
    		}
		} catch (Exception e) {
			logger.error("Exception in close():",e);
		}
    	serialPort.removeEventListener();
    	logger.trace("About to close() serialPort");
    	serialPort.close();
    	logger.debug("SerialTransport closed.");
    	serialPort = null;
    }

	private class SerialListener implements SerialPortEventListener {
		
		private String name = null;
		
		private long counter = 0;
		
		private StringBuffer sb;

		public SerialListener(String portName) {
			name = portName;
			if (DEBUG) sb = new StringBuffer();
		}

		public void serialEvent(SerialPortEvent event) {
			if (name != null) {
				Thread.currentThread().setName("Listener-"+name);
				name = null;
			}
            if (event.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
            	try {
            		int n = inputStream.available(); 
					while (n > 0) {
						int i = inputStream.read();
						n--;
						if (i == -1) {
							logger.error("Nessun dato ricevuto");					
						} else {			
							if (DEBUG) {
								counter = (counter + 1) % 10000;
								sb.append(" "+counter+":"+Message.b2h(i));
								if (n == 0) {
									logger.trace("Buffer"+sb.toString());
									sb = new StringBuffer();
								}
							}
							if (connector != null) {
								connector.received(i);
							}
						}
					}
				} catch (Exception e) {
	    			logger.fatal("Read error: ",e);
	    			// Non fare il reopen qui, perche' provoca dead lock
	    			//reopen();
				}
            } else if (event.getEventType() == SerialPortEvent.OE) {
            	logger.error("Overrun error: "+event.getSource());
            }
		}
	}

	@Override
	public int getSpeed() {
		return portSpeed;
	}

}