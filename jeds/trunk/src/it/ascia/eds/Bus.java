/**
 * Copyright (C) 2008 ASCIA S.R.L.
 */
package it.ascia.eds;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import it.ascia.eds.device.*;
import it.ascia.eds.msg.*;

/**
 * Interfaccia verso il bus EDS
 * 
 * @author arrigo
 *
 */
public abstract class Bus {
	/**
	 * Quanto tempo aspettare dopo un ping.
	 * 
	 * Nel caso peggiore (1200 bps), la trasmissione di un messaggio richiede 
	 * 8 / 120 = 660 msec. In quello migliore (9600 bps), la trasmissione 
	 * richiede 82 msec. Questa costante deve tener conto del caso migliore.
	 */
	protected static final int PING_WAIT = 200;
	/**
	 * Quante volte aspettare PING_WAIT prima di ritrasmettere.
	 * 
	 * Questo indica quante volte si attende PING_WAIT millisecondi, prima di
	 * riprovare a inviare un messaggio. Condizione da rispettare è che 
	 * PING_WAIT * WAIT_RETRIES sia maggiore del tempo più lungo previsto per 
	 * il round-trip di un messaggio. 
	 */
	protected static final int WAIT_RETRIES = 6;
	/**
	 * Quante volte provare a reinviare un messaggio senza risposta.
	 * 
	 * Quando l'attesa supera PING_WAIT * WAIT_RETRIES, questa costante decide
	 * quanti tentativi di ri-invio effettuare.
	 */
	protected static final int SEND_RETRIES = 3;

	/**
	 * L'InputStream da cui leggere i dati ricevuti. Deve essere impostato dalle sottoclassi.
	 */
	private InputStream inputStream;
	/**
	 * MessageParser per la lettura dei messaggi in ingresso.
	 */
	protected MessageParser mp;
	/**
	 * I device presenti nel bus.
	 */
    private Map devices;
    /**
     * Il BMC "finto" che corrisponde a questo computer.
     */
    private Device bmcComputer;
    
    /**
     * Indirizzo di un device che stiamo contattando.
     * 
     * Questo indirizzo può non appartenere a nessun elemento di devices.
     */
    private int pingedDevice;
    /**
     * Indirizzo del device che sta contattando pingedDevice.
     * 
     * Questo indirizzo deve appartenere a un elemento di devices.
     */
    private int pingerDevice;
    /**
     * Il messaggio di risposta che abbiamo ricevuto da pingedDevice
     */
    private Message pongMessage;

    public Bus() {
        devices = new HashMap();
        bmcComputer = null;
        pingedDevice = -1; // Il minimo è 0
		mp = new MessageParser();
    }
    
	/**
     * Invia un messaggio sul bus.
     * 
     * Eventuali errori di trasmissione vengono ignorati.
     * 
     * @param m the message to send
     */
    public abstract void write(Message m);
    
    /**
     * Chiude la connessione al bus.
     */
    public abstract void close();
    
    /**
     * Imposta inputStream.
     */
    protected void setInputStream(InputStream is) {
    	inputStream = is;
    }
    
    /**
     * Imposta il BMCComputer del bus.
     */
    public void setBMCComputer(BMCComputer bmcComputer) {
    	this.bmcComputer = bmcComputer;
    	devices.put(new Integer(bmcComputer.getAddress()), bmcComputer);
    }
    
    /**
     * Legge e interpreta i dati in arrivo.
     * 
     * Questa funzione deve essere chiamata dalla sottoclasse, quando inputStream
     * contiene dati da leggere.
     * 
     * I messaggi decodificati vengono passati a dispatchMessage().
     */
    protected void readData() {
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
//				    		if (!m.getTipoMessaggio().equals("Aknowledge")) {
//				    			System.out.println((new Date()).toString() + "\r\n" + m);
//				    		}
				    		dispatchMessage(m);
				    		//mp.clear();
				    	}
					}
			    }
			}			
	    } catch (IOException e) {}
    }

    /**
     * Invia un messaggio a tutti i BMC destinatari.
     * 
     * Questo metodo deve essere chiamato dalla sottoclasse, per ogni messaggio 
     * che viene ricevuto.
     * 
     * Stampa un messaggio su stderr se il messaggio è per un BMC che non è in 
     * lista, né in fase di ping.
     * 
     * Il BMCComputer riceve tutti i messaggi.
     * 
     * @param m il messaggio da inviare
     */
    private void dispatchMessage(Message m) {
    	int rcpt = m.getRecipient();
    	int sender = m.getSender();
    	if ((sender == pingedDevice) && (rcpt == pingerDevice)) {
    		// È un pong.
    		pongMessage = m;
    	} else {
    		if (m.isBroadcast()) { // Mandiamo il messaggio a tutti
    			Iterator it = devices.values().iterator();
    			while (it.hasNext()) {
    				Device bmc = (Device)it.next();
    				bmc.receiveMessage(m);
    			}
    		} else { // Non e' un messaggio broadcast
    			Device bmc = (Device)devices.get(new Integer(rcpt));
    			if (bmc != null) {
    				bmc.receiveMessage(m);
    			} else {
    				/*System.err.println("Ricevuto un messaggio per il BMC " + 
    						rcpt + " che non conosco:");
    				System.err.println((new Date()).toString() + "\r\n" + m);*/
    			}
    			// Lo mandiamo anche al BMCComputer
    			if ((bmcComputer != null) && (rcpt != bmcComputer.getAddress())) { 
    	    		bmcComputer.receiveMessage(m);
    			}
    		}
    	} // If non è un pong
    }

    /**
     * Invia un messaggio e attende una risposta dal destinatario.
     * 
     * Se la risposta non arriva dopo un certo tempo, essa viene ri-inviata un
     * tot di volte.
     * 
     * Il messaggio di risposta viene riconosciuto da dispatchMessage
     * in base a mittente e destinatario.
     * 
     * @returns true se il messaggio di risposta è arrivato.
     */
    public boolean sendPTPMessage(Message m) {
    	int waitings, tries;
    	boolean received;
    	pingedDevice = m.getRecipient();
    	pingerDevice = m.getSender();
    	pongMessage = null;
    	received = false;
    	try {
    		for (tries = 0;
    			(tries <= SEND_RETRIES) && (!received); 
    			tries++) {
    			write(m);
    			for (waitings = 0; 
    				(waitings < WAIT_RETRIES) && (!received); 
    				waitings++) {
    					Thread.sleep(PING_WAIT);
    					received = (pongMessage != null);
    			}
    		}
    	} catch (InterruptedException e) {
    	}
		return received;
    }

    /**
     * Ritorna un Device a partire dall'indirizzo.
     * 
     * @param address l'indirizzo da cercare.
     * 
     * @returns il Device oppure null se il Device non è nella lista.
     */
    public Device getDevice(int address) {
    	return (Device)devices.get(new Integer(address));
    }
    
    /**
     * Aggiunge un Device collegato al bus.
     * 
     * @param Device il Device da aggiungere.
     */
    public void addDevice(Device device) {
    	devices.put(new Integer(device.getAddress()), device);
    }
}
