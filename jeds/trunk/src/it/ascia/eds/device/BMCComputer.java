/**
 * COPYRIGHT (C) 2008 ASCIA S.R.L.
 * 
 * TODO Valutare quali metodi / logica spostare nel connector
 */
package it.ascia.eds.device;

import it.ascia.ais.AISException;
import it.ascia.ais.Connector;
import it.ascia.ais.Device;
import it.ascia.eds.msg.BroadcastMessage;
import it.ascia.eds.msg.EDSMessage;
import it.ascia.eds.msg.PTPMessage;
import it.ascia.eds.msg.PTPRequest;
import it.ascia.eds.msg.RichiestaAssociazioneUscitaMessage;
import it.ascia.eds.msg.RichiestaModelloMessage;
import it.ascia.eds.msg.RichiestaUscitaMessage;
import it.ascia.eds.msg.RispostaAssociazioneUscitaMessage;
import it.ascia.eds.msg.RispostaModelloMessage;
import it.ascia.eds.EDSConnector;

/**
 * Il rappresentante di questo computer sul transport EDS.
 * 
 * <p>Le sue funzioni sono:</p>
 * <ul>
 * <li>tenere un log di tutti i messaggi ricevuti;</li>
 * <li>spedire messaggi sul transport.</li>
 * </ul>
 * 
 * <p>Ci deve essere un solo oggetto di questa classe per ciascun transport.</p>
 * 
 * @author arrigo
 */
public class BMCComputer extends BMC {
	/**
     * Il messaggio di tipo ACK che stiamo mandando, per il quale aspettiamo
     * una risposta.
     */
    private PTPRequest messageToBeAnswered;
	
	/**
	 * Costruttore.
	 * 
	 * @param transport il transport a cui siamo collegati
	 * @param address l'indirizzo di questo device sul transport
	 * @throws AISException 
	 */
	public BMCComputer(Connector connector, String address) throws AISException {
		super(connector, address, -1, "Computer");
		messageToBeAnswered = null;
	}

	/** 
	 * Alla ricezione di un messaggio:
	 * <ol>
	 *   <li>Aggiunge il messaggio alla inbox</li>
	 *   <li>Se il messaggio e' una risposta a richiesta modello, agiunge un BMC</li>
	 *   <li>Conferma che il messaggio e' stato ricevuto</li>
	 * </ol>  
	 * @throws AISException 
	 * @see it.ascia.eds.device.Device#receiveMessage(it.ascia.eds.msg.EDSMessage)
	 */
	public void messageReceived(EDSMessage m) throws AISException {
		if (m.isBroadcast()) {
			// TODO propagare alle uscite
		} else {
			PTPMessage ptpm = (PTPMessage) m;
			// Aggiungiamo i BMC che si presentano
			if (RispostaModelloMessage.class.isInstance(ptpm)) {
				RispostaModelloMessage risposta = (RispostaModelloMessage) ptpm;
				Device bmc = getConnector().getDevice(risposta.getSource());
				if (bmc == null) {
					bmc = BMC.createBMC(getConnector(), risposta.getSource(), risposta.getModello(), null, true);
					logger.info("Creato BMC "+bmc);
				}
			}
			// Attendiamo risposte?
			if (messageToBeAnswered != null) {
				if (messageToBeAnswered.isAnsweredBy(ptpm)) {
					if (RispostaAssociazioneUscitaMessage.class.isInstance(ptpm)) {
						RispostaAssociazioneUscitaMessage mrisp = (RispostaAssociazioneUscitaMessage) ptpm;
						RichiestaAssociazioneUscitaMessage mrich = (RichiestaAssociazioneUscitaMessage) messageToBeAnswered;
						EDSConnector conn = (EDSConnector) getConnector();
						//String address = conn.getFullAddress(mrisp.getSource());
						String address = mrisp.getSource();
						int gruppo = mrisp.getComandoBroadcast();
						if (gruppo > 0) {
							int outPortNumber = mrich.getUscita();
							int casella = mrich.getCasella();
							BMC bmc = (BMC) conn.getDevice(address);
							logger.info("Associazione dispositivo:"+bmc.getName()+" uscita:"+getOutputPortId(outPortNumber)+" casella:"+casella+" gruppo:"+gruppo);
							bmc.bindOutput(gruppo, outPortNumber);
						}
					}
					// sveglia sendPTPRequest
					synchronized (messageToBeAnswered) {
						messageToBeAnswered.answered = true;
						messageToBeAnswered.notify(); 						
					}
				}
			}
		} // if e' un PTPMessage
	}
	
	public void messageSent(EDSMessage m) {
		// Sappiamo quello che abbiamo inviato.
	}
	
	public String getInfo() {
		return "This computer";
	}

	/**
	 * Invia un messaggio point-to-point e attende risposta.
	 *
	 * <p>Se la risposta non arriva dopo un certo tempo, essa viene ri-inviata 
	 * un tot di volte.</p>
     * 
     * <p>Il messaggio di risposta viene riconosciuto da dispatchMessage().</p>
	 */
	private boolean sendPTPRequest(PTPRequest m) {
    	int tries;
    	boolean received = false;
    	EDSConnector connector = (EDSConnector)getConnector();  
		if (messageToBeAnswered != null) {
			if (messageToBeAnswered.compareTo(m) == 0) {
				logger.debug("Messaggio gia' inviato in attesa di risposta: "+m);				
			} else {
				logger.fatal("messageToBeAnswered non nullo: "+messageToBeAnswered);
				logger.fatal("Messaggio non inviato: "+m);
			}
			return false;
		}
    	messageToBeAnswered = m;
    	synchronized (messageToBeAnswered) {
    		for (tries = 1;
    			(tries <= m.getMaxSendTries()) && (!m.answered); 
    			tries++) {
    			logger.trace("Invio "+tries+" di "+m.getMaxSendTries()+" : "+m.toHexString());    			
    			connector.transport.write(m.getBytesMessage());
    			// si mette in attesa, ma se nel frattempo arriva la risposta viene avvisato
    	    	try {
    	    		messageToBeAnswered.wait((long)(1000 * connector.getRetryTimeout() * (1 + 0.2 * Math.random())));
    	    	} catch (InterruptedException e) {
    				logger.trace("sendPTPRequest wait:2");
    	    	}
    		}
	    	received = m.answered;
	    	messageToBeAnswered = null;
    	}
    	if (! received) {
    		logger.error("Messaggio non risposto: "+m);
    	}
		return received;
    }
	
	/**
	 * Invia un messaggio broadcast.
	 * 
	 * <p>Gli header0
	 *  vengono ri-generati ad ogni invio.</p>
	 */
	private void sendBroadcastMessage(BroadcastMessage m) {
		int tries = m.getSendTries();
		EDSConnector connector = (EDSConnector)getConnector();
		for (int i = 0; i < tries; i++) {
			m.randomizeHeaders();
			try {
				Thread.sleep(connector.getRetryTimeout());
			} catch (InterruptedException e) {
			}
			connector.transport.write(m.getBytesMessage());
		}
	}
	
	/**
	 * Invia un messaggio sul transport.
	 * 
	 * <p>Se il messaggio richiede una risposta, questa viene attesa seguendo i
	 * timeout stabiliti.</p>
	 * 
	 * @return true se l'invio e' andato a buon fine; nel caso di richieste,
	 * ritorna true se e' arrivata una risposta.
	 */
	public boolean sendMessage(EDSMessage m) {
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
				getConnector().transport.write(m.getBytesMessage());
				// non c'e' modo di sapere se e' arrivato; siamo ottimisti.
				retval = true;
			}
		}
		return retval;
	}

	
	/**
     * "Scopre" il BMC indicato inviandogli un messaggio di richiesta modello.
     * 
     * <p>Se il BMC e' gia' in lista, non invia messaggi, ma ritorna le 
     * informazioni gia' note.</p>
     * 
     * <p>Se il BMC non era gia' in lista, allora verra' inserito dal metodo 
     * messageReceived(). Questo metodo chiama anche 
     * @link{#discoverBroadcastBindings} per scoprire le associazioni del
     * BMC ai comandi broadcast.
     * 
     * @param address l'indirizzo del BMC da "scoprire".
     * 
     * @return il BMC se trovato o registrato, oppure null.
     */
    public BMC discoverBMC(int address) {
    	BMC bmc;
    	Device devices[];
    	// Gia' abbiamo il BMC in lista?
    	devices = getConnector().getDevices(String.valueOf(address));
    	if (devices.length == 0) {
    		// No!
    		logger.trace("Ricerca del BMC con indirizzo " + address);
    		if (sendPTPRequest(new RichiestaModelloMessage(address, 
    				getIntAddress()))) {
    			devices = getConnector().getDevices(String.valueOf(address));
    			if (devices.length > 0) {
    				bmc = (BMC)devices[0];
    				logger.info("Trovato BMC "+bmc.getInfo());
    				// TODO gestire risposte a discoverUscite(bmc);
    				// TODO gestire risposte a 
    				discoverBroadcastBindings(bmc);
    			} else {
    				// Molto strano: l'ACK del messaggio e' arrivato, ma non 
    				// abbiamo nessun BMC.
    				logger.warn("Sembra che ci siano problemi sulla " +
    						"connessione");
    				bmc = null;
    			}
    		} else {
    			bmc = null;
    		}
    	} else {
    		bmc = (BMC)devices[0];
    	}
    	return bmc;
    }
    
    /**
     * Rileva le opzioni delle uscite.
     * 
     * <p>Questo metodo manda molti messaggi! Il metodo messageReceived() del 
     * BMC deve interpretare i messaggi di risposta.</p>
     *  
     * {@link RispostaUscitaMessage}.
     */
    public void discoverUscite(BMC bmc){
    	int outPort;
    	for (outPort = 0; outPort < bmc.getOutPortsNumber(); outPort++) {
    		RichiestaUscitaMessage m = new RichiestaUscitaMessage(bmc.getIntAddress(),
    					getIntAddress(), outPort);
    		sendPTPRequest(m);
    	}
    }

    /**
     * Rileva le associazioni delle uscite con comandi broadcast.
     * 
     * <p>Questo metodo manda molti messaggi! Il metodo messageReceived() del 
     * BMC deve interpretare i messaggi di risposta.</p>
     *  
     * {@link RispostaAssociazioneUscitaMessage}.
     */
    public void discoverBroadcastBindings(BMC bmc){
    	int outPort;
    	int casella;
    	logger.debug("Richiesta associazioni broadcast BMC " + 
    			bmc.getName());
    	for (outPort = 0; outPort < bmc.getOutPortsNumber(); outPort++) {
    		for (casella = 0; casella < bmc.getCaselleNumber(); casella++) {
    			RichiestaAssociazioneUscitaMessage m;
    			m = new RichiestaAssociazioneUscitaMessage(bmc.getIntAddress(),
    					getIntAddress(), outPort, casella);
    			sendPTPRequest(m);
    		}
    	}
    }

	public long updateStatus() {
		logger.error("updateStatus non implementato su BMCComputer.");
		return 0;
	}

	// Niente da dichiarare.
	public String getStatus(String port, long timestamp) {
		return "";
	}

	/**
	 * Necessario per compilare.
	 */
	public int getFirstInputPortNumber() {
		return 0;
	}

	public int getOutPortsNumber() {
		return 8;
	}

	public void poke(String port, String value) throws AISException {
		throw new AISException("Unimplemented.");
	}

	public int getInPortsNumber() {
		return 8;
	}

	public long updatePort(String portId) throws AISException {
		setPortValue(portId, new Boolean(false));
		return 0;
	}

	public void writePort(String portId, Object newValue) throws AISException {
		// TODO Auto-generated method stub
		
	}

}
