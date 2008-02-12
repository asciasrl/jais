/**
 * COPYRIGHT (C) 2008 ASCIA S.R.L.
 */
package it.ascia.eds.device;

import java.util.*;

import it.ascia.eds.msg.BroadcastMessage;
import it.ascia.eds.msg.Message;
import it.ascia.eds.msg.PTPMessage;
import it.ascia.eds.msg.PTPRequest;
import it.ascia.eds.msg.RichiestaAssociazioneUscitaMessage;
import it.ascia.eds.msg.RichiestaModelloMessage;
import it.ascia.eds.msg.RispostaAssociazioneUscitaMessage;
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
	 * Dimensione massima della inbox.
	 */
	private final int MAX_INBOX_SIZE = 100;
	/**
	 * Queue dei messaggi ricevuti. I piu' recenti sono all'inizio.
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
	 * Costruttore.
	 * 
	 * @param bus il bus a cui siamo collegati
	 * @param address l'indirizzo di questo device sul bus
	 */
	public BMCComputer(int address, Bus bus) {
		super(address, -1, bus, "Computer");
		inbox = new LinkedList();
		outbox = new LinkedList();
		messageToBeAnswered = null;
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
		if (!m.isBroadcast()) {
			PTPMessage ptpm = (PTPMessage) m;
			// Attendiamo risposte?
			if (messageToBeAnswered != null) {
				messageToBeAnswered.isAnsweredBy(ptpm);
			}
			// Aggiungiamo i BMC che si presentano
			if (RispostaModelloMessage.class.isInstance(ptpm)) {
				RispostaModelloMessage risposta = (RispostaModelloMessage) ptpm;
				try {
					BMC.createBMC(risposta.getSender(), risposta.getModello(),
							null, bus, true);
				} catch (EDSException e) {
				// Se il device e' gia' sul bus, non e' un errore.
				}
			}
		} // if m è un PTPMessage
		// Tutti i messaggi ricevuti devono finire nella inbox. Ma non troppi.
		inbox.addFirst(m);
		while (inbox.size() > MAX_INBOX_SIZE) {
			inbox.removeLast();
		}
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
	private boolean sendPTPRequest(PTPRequest m) {
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
    					int delay = (int)
    						(bus.PING_WAIT * (1 + Math.random() * 0.2));
    					Thread.sleep(delay);
    					received = (messageToBeAnswered.wasAnswered());
    			}
    		}
    	} catch (InterruptedException e) {
    	}
    	messageToBeAnswered = null;
		return received;
    }
	
	/**
	 * Invia un messaggio broadcast.
	 * 
	 * <p>Gli header vengono ri-generati ad ogni invio.</p>
	 */
	private void sendBroadcastMessage(BroadcastMessage m) {
		int tries = m.getSendTries();
		for (int i = 0; i < tries; i++) {
			m.randomizeHeaders();
			try {
				Thread.sleep(bus.WAIT_RETRIES * bus.PING_WAIT);
			} catch (InterruptedException e) {
			}
			bus.write(m);
		}
	}
	
	/**
	 * Invia un messaggio sul bus.
	 * 
	 * Se il messaggio richiede una risposta, questa viene attesa seguendo i
	 * timeout stabiliti.
	 * 
	 * @return true se l'invio e' andato a buon fine; nel caso di richieste,
	 * ritorna true se e' arrivata una risposta.
	 */
	public synchronized boolean sendMessage(Message m) {
		boolean retval;
		if (m.isBroadcast()) {
			sendBroadcastMessage((BroadcastMessage) m);
			retval = true;
		} else { 
			// E' un PTPMessage
			PTPMessage ptpm = (PTPMessage) m;
			if (ptpm.wantsReply()) {
				// E' un PTPRequest
				retval = sendPTPRequest((PTPRequest) ptpm);
			} else {
				// Invio nudo e crudo
				bus.write(m);
				// non c'e' modo di sapere se e' arrivato; siamo ottimisti.
				retval = true;
			}
		}
		return retval;
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
    
    /**
     * Rileva le associazioni delle uscite con comandi broadcast.
     * 
     * Questo metodo manda molti messaggi! Il metodo messageReceived() del BMC
     * deve interpretare i messaggi di risposta 
     * {@link RispostaAssociazioneUscitaMessage}.
     */
    public void discoverBroadcastBindings(BMC bmc){
    	int outPort;
    	int casella;
    	for (outPort = 0; outPort < bmc.getOutPortsNumber(); outPort++) {
    		for (casella = 0; casella < bmc.getCaselleNumber(); casella++) {
    			RichiestaAssociazioneUscitaMessage m;
    			m = new RichiestaAssociazioneUscitaMessage(bmc.getAddress(),
    					getAddress(), outPort, casella);
    			sendPTPRequest(m);
    		}
    	}
    }

	public void updateStatus() {
		logger.error("updateStatus non implementato su BMCComputer.");
	}

	// Niente da dichiarare.
	public String getStatus(String port, String busName) {
		return "";
	}

	/**
	 * Necessario per compilare.
	 */
	public int getFirstInputPortNumber() {
		return 0;
	}

	public int getOutPortsNumber() {
		return 0; // per ora...
	}

	public void setPort(String port, String value) throws EDSException {
		throw new EDSException("Unimplemented.");
	}

}
