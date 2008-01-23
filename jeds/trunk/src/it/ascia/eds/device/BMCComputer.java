/**
 * COPYRIGHT (C) 2008 ASCIA S.R.L.
 */
package it.ascia.eds.device;

import java.util.*;

import it.ascia.eds.msg.Message;
import it.ascia.eds.msg.PTPMessage;
import it.ascia.eds.msg.PTPRequest;
import it.ascia.eds.msg.RichiestaModelloMessage;
import it.ascia.eds.msg.RispostaModelloMessage;
import it.ascia.eds.Bus;
import it.ascia.eds.EDSException;

/**
 * Il rappresentante di questo computer sul bus EDS.
 * 
 * Le sue funzioni sono:
 *   * tenere un log di tutti i messaggi ricevuti
 *   * spedire messaggi sul bus
 * 
 * Ci deve essere un solo oggetto di questa classe per ciascun bus.
 * 
 * @author arrigo
 */
public class BMCComputer extends BMC {
	
	/**
	 * Queue dei messaggi ricevuti.
	 */
	private LinkedList inbox;
	/**
	 * Queue dei messaggi in uscita (cioè che devono essere inviati).
	 */
	private LinkedList outbox;
	/**
     * Il messaggio di tipo ACK che stiamo mandando, per il quale aspettiamo
     * una risposta.
     */
    private PTPRequest messageToBeAnswered;
    /**
     * La risposta che abbiamo ricevuto per messageToBeAnswered.
     */
    private PTPMessage answerMessage;

	
	/**
	 * Costruttore.
	 * 
	 * @param bus il bus a cui siamo collegati
	 * @param address l'indirizzo di questo device sul bus
	 */
	public BMCComputer(int address, Bus bus) {
		super(address, -1, bus, "Computer");
		inbox = new LinkedList();
		outbox = new LinkedList();
		answerMessage = messageToBeAnswered = null;
	}
	
	/* (non-Javadoc)
	 * @see it.ascia.eds.device.Device#getAddress()
	 */
	public int getAddress() {
		return this.address;
	}

	/* (non-Javadoc)
	 * @see it.ascia.eds.device.Device#receiveMessage(it.ascia.eds.msg.Message)
	 */
	public void messageReceived(Message m) {
		if (PTPMessage.class.isInstance(m)) {
			PTPMessage ptpm = (PTPMessage) m;
			// Attendiamo risposte?
			if ((messageToBeAnswered != null) &&				
					messageToBeAnswered.isAnsweredBy(ptpm)) {
				answerMessage = ptpm;
			}
			// Aggiungiamo i BMC che si presentano
			if (RispostaModelloMessage.class.isInstance(m)) {
				RispostaModelloMessage risposta = (RispostaModelloMessage) m;
				try {
					BMC.createBMC(risposta.getSender(), risposta.getModello(),
							null, bus);
				} catch (EDSException e) {
				// Se il device e' gia' sul bus, non e' un errore.
				}
			}
		} // if m è un PTPMessage
		// Tutti i messaggi ricevuti devono finire nella inbox
		inbox.addLast(m);
	}
	
	public void messageSent(Message m) {
		// Sappiamo quello che abbiamo inviato.
	}
	
	/**
	 * Restituisce il primo messaggio nella coda "inbox".
	 * 
	 * @return il messaggio oppure null se la coda è vuota.
	 */
	public Message getNextMessage() {
		Message retval;
		try {
			retval = (Message) inbox.removeFirst();
		} catch (NoSuchElementException e) {
			retval = null;
		}
		return retval;
	}

	public String getInfo() {
		return "This computer";
	}

	/**
	 * Invia un messaggio point-to-point e attende risposta.
	 *
	 * Se la risposta non arriva dopo un certo tempo, essa viene ri-inviata un
     * tot di volte.
     * 
     * Il messaggio di risposta viene riconosciuto da dispatchMessage().
	 */
	public boolean sendPTPRequest(PTPRequest m) {
    	int waitings, tries;
    	boolean received = false;
    	messageToBeAnswered = m;
    	try {
    		for (tries = 0;
    			(tries < m.getMaxSendTries()) && (!received); 
    			tries++) {
    			bus.write(m);
    			for (waitings = 0; 
    				(waitings < bus.WAIT_RETRIES) && (!received); 
    				waitings++) {
    					Thread.sleep(bus.PING_WAIT);
    					received = (answerMessage != null);
    			}
    		}
    	} catch (InterruptedException e) {
    	}
    	messageToBeAnswered = null;
		return received;
    }

	
	/**
     * "Scopre" il BMC indicato inviandogli un messaggio di richiesta modello.
     * 
     * Se il BMC e' gia' in lista, vengono utilizzate le informazioni gia' note.
     * 
     * Se il BMC non era gia' in lista, allora verrà inserito dal metodo messageReceived().
     * 
     * @param address l'indirizzo del BMC da "scoprire".
     * 
     * @return il BMC se trovato o registrato, oppure null.
     *  
     */
    public BMC discoverBMC(int address) {
    	BMC retval;
    	// Gia' abbiamo il BMC in lista?
    	retval = (BMC)bus.getDevice(address);
    	if (retval == null) {
    		if (sendPTPRequest(new RichiestaModelloMessage(address, 
    				getAddress()))) {
    			retval = (BMC)bus.getDevice(address);
    		} else {
    			retval = null;
    		}
    	}
    	return retval;
    }
    
    public void discoverBroadcastBindings(BMC bmc){
    }

	public void updateStatus() {
		System.err.println("updateStatus non implementato su BMCComputer.");
	}

	public String getStatus() {
		return null;
	}

	/**
	 * Necessario per compilare.
	 */
	protected int getFirstInputPortNumber() {
		return 0;
	}
}
