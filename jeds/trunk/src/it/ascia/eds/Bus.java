/**
 * Copyright (C) 2008 ASCIA S.R.L.
 */
package it.ascia.eds;

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
     * Questo indirizzo può non appartenere a nessun elemento di devices
     */
    private int pingedDevice;
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
     * Imposta il BMCComputer del bus.
     */
    public void setBMCComputer(BMCComputer bmcComputer) {
    	this.bmcComputer = bmcComputer;
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
    protected void dispatchMessage(Message m) {
    	if (m.getSender() == pingedDevice) { // FIXME: dovremmo controllare anche il contenuto 
    		// Se è un pong per un discovery, non interessa agli altri
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
    				/*System.err.println("Ricevuto un messaggio per il BMC " + 
    						rcpt + " che non conosco:");
    				System.err.println((new Date()).toString() + "\r\n" + m);*/
    			}
    		}
    	} // If non è un pong
    	if (bmcComputer != null) 
    		bmcComputer.receiveMessage(m);
    }

    /**
     * Invia un messaggio e attende una risposta dal destinatario.
     * 
     * Se la risposta non arriva dopo un certo tempo, essa viene ri-inviata un
     * tot di volte.
     * 
     * @returns il messaggio di risposta, oppure null
     */
    public Message sendPTPMessage(Message m) {
    	int waitings, tries;
    	boolean received;
    	pingedDevice = m.getRecipient();
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
		pingedDevice = -1;
		return pongMessage;
    }
    
    /**
     * "Scopre" il BMC indicato inviandogli un messaggio di richiesta modello.
     * 
     * Se il BMC e' gia' in lista, vengono utilizzate le informazioni gia' note.
     * 
     * Se il BMC non era gia' in lista, allora viene inserito.
     * 
     * @param address l'indirizzo del BMC da "scoprire"
     * 
     * @return L'oggetto BMC (della sottoclasse giusta), o null se nessun BMC
     * ha risposto al ping.
     *  
     * @throws un'Exception se non esiste un BMCComputer sul bus.
     */
    public BMC discoverBMC(int address) throws Exception {
    	BMC retval;
    	// Gia' abbiamo il BMC in lista?
    	retval = (BMC)devices.get(new Integer(address));
    	if (retval == null) {
    		Message mess;
    		if (bmcComputer == null ){
         		throw new Exception("Non esiste un BMCComputer su questo bus");
         	}
    		mess = sendPTPMessage(new RichiestaModelloMessage(address, 
    				bmcComputer.getAddress()));
    		if (mess != null) {
    			int model;
    			RispostaModelloMessage m = (RispostaModelloMessage) mess;
    			model = m.getModello();
    			switch(model) {
    			case 88:
    			case 8:
    			case 40:
    			case 60:
    			case 44:
    				retval = new BMCStandardIO(address, model, this);
    				break;
    			case 41:
    			case 61:
    			case 81:
    				retval = new BMCIR(address, model, this);
    				break;
    			case 101:
    			case 102:
    			case 103:
    			case 104:
    			case 106:
    			case 111:
    				retval = new BMCDimmer(address, model, this);
    				break;
    			case 131:
    				retval = new BMCIntIR(address, model, this);
    				break;
    			case 152:
    			case 154:
    			case 156:
    			case 158:
    				retval = new BMCScenarioManager(address, model, this);
    				break;
    			case 127:
    				retval = new BMCChronoTerm(address, model, this);
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

}
