/**
 * Copyright (C) 2008 ASCIA S.R.L.
 */
package it.ascia.eds;

import java.util.Iterator;
import it.ascia.ais.AISException;
import it.ascia.ais.Connector;
import it.ascia.ais.Controller;
import it.ascia.ais.Message;
import it.ascia.eds.device.*;
import it.ascia.eds.msg.*;

/**
 * Interfaccia verso il bus EDS.
 * 
 * <p>JAIS vede questo come un Connector, i cui Device sono BMC.</p>
 * 
 * @author arrigo
 *
 */
public class EDSConnector extends Connector {

	/**
     * Il messaggio che stiamo mandando, per il quale aspettiamo una risposta.
     */
    private PTPRequest messageToBeAnswered;
    
    /**
     * Indirizzo con cui il Connettore invia messaggi sul BUS
     */
    private int myAddress = 0;

	public void setAddress(int myAddress) {
		this.myAddress = myAddress; 		
	}
	
    /**
	 * Quanto tempo aspettare la risposta dopo l'invio di un messaggio.
	 * 
	 * <p>Nel caso peggiore (1200 bps), la trasmissione di un messaggio richiede 
	 * 8 / 120 = 660 msec. In quello migliore (9600 bps), la trasmissione 
	 * richiede 82 msec. Questa costante deve tener conto del caso migliore.</p>
	 */
	protected int RETRY_TIMEOUT = 200;

	/**
	 * Quante volte provare a reinviare un messaggio che richiede una risposta.
	 * 
	 * <p>Quando l'attesa supera PING_WAIT * WAIT_RETRIES, questa costante 
	 * decide quanti tentativi di ri-invio effettuare.</p>
	 */
	public static final int ACKMESSAGE_SEND_RETRIES = 3;
	/**
	 * Quante volte provare a reinviare un messaggio di richiesta stato 
	 * senza risposta.
	 * 
	 * <p>Quando l'attesa supera retryTimeout, questa costante 
	 * decide quanti tentativi di ri-invio effettuare.</p>
	 */
	protected static final int STATUSREQ_SEND_RETRIES = 3;
	/**
	 * Quante volte reinviare un messaggio broadcast
	 */
	public static final int BROADCAST_RESENDS = 7;
	
	/**
	 * EDSMessageParser per la lettura dei messaggi in ingresso.
	 */
	protected EDSMessageParser mp;
    
    /**
     * Connettore per il BUS EDS.
     * 
     * @param name il nome del Connector, che sara' la parte iniziale degli indirizzi
     * di tutti i Device collegati a questo Connector.
     */
    public EDSConnector(String name, Controller controller) {
    	super(name,controller);
		mp = new EDSMessageParser();
    }

    /**
     * Ritorna l'indirizzo del connettore.
     * 
     * <p>Questo metodo e' utile per i BMC, quando devono richiedere 
     * informazioni sul proprio stato. I messaggi che inviano devono partire 
     * "a nome" del connettore.</p>
     */
    public int getMyAddress() {
    	return myAddress;
    }
        
    /**
     * Gestisce ogni byte ricevuto finchè compone un messaggio e quindi ne effettua il dispacciamento
     */
    public void received(int b) {
		mp.push(b);
		if (mp.isValid()) {
			EDSMessage m = (EDSMessage) mp.getMessage();
			if (m != null) {
				receiveQueue.offer(m);
			}
		}    	
    }

    protected void dispatchMessage(Message m) throws AISException {
		if (EDSMessage.class.isInstance(m)) {
			dispatchMessage((EDSMessage) m);
		}
	}

    /**
     * Invia un messaggio a tutti i BMC destinatari e al mittente.
     * 
     * <p>Questo metodo deve essere chiamato dalla sottoclasse, per ogni 
     * messaggio che viene ricevuto.</p>
     * 
     * <p>Stampa un messaggio su stderr se il messaggio e' per un BMC che non e'
     * in lista, ne' in fase di ping.</p>
     * 
     * <p>Il BMCComputer riceve tutti i messaggi.</p>
     * 
     * @param m il messaggio da inviare
     * @throws AISException 
     */
    protected void dispatchMessage(EDSMessage m) throws AISException {
    	int rcpt = m.getRecipient();
    	int sender = m.getSender();
    	if (messageToBeAnswered != null 
    			&& PTPMessage.class.isInstance(m) 
    			&& messageToBeAnswered.isAnsweredBy((PTPMessage) m)) {
    		if (PTPResponse.class.isInstance(m) && PTPRequest.class.isInstance(messageToBeAnswered)) {
    			((PTPResponse) m).setRequest(messageToBeAnswered);
    		}
			// sveglia sendPTPRequest
			synchronized (messageToBeAnswered) {
	    		messageToBeAnswered.setAnswered(true);
				messageToBeAnswered.notify(); 						
			}
    	}
    	if (BroadcastMessage.class.isInstance(m)) { 
    		// Mandiamo il messaggio a tutti
    		Iterator it = getDevices().values().iterator();
    		while (it.hasNext()) {
    			BMC bmc = (BMC)it.next();
    			bmc.messageReceived(m);
    		}
    	} else if (RispostaModelloMessage.class.isInstance(m)) {
			// Aggiungiamo i BMC che si presentano
			RispostaModelloMessage risposta = (RispostaModelloMessage) m;
			BMC bmc = (BMC) getDevice(risposta.getSource()); 
			if (bmc == null) {
				bmc = createBMC(risposta.getSource(), risposta.getModello());
				if (bmc != null) {
					logger.info("Creato BMC "+bmc+" indirizzo:"+bmc.getFullAddress());
					bmc.discover();
				}
			} else {
				bmc.messageSent(m);
			}
    	} else { 
    		BMC bmc;
    		
    		// Al mittente 
    		bmc = (BMC)getDevice((new Integer(sender)).toString());
    		if (bmc != null) {
    			bmc.messageSent(m);
    		}

    		// Al destinatario 
    		bmc = (BMC)getDevice((new Integer(rcpt)).toString());
    		if (bmc != null) {
    			bmc.messageReceived(m);
    		}
    	}
    }

    private BMC createBMC(String source, int modello) throws AISException {
    	return BMC.createBMC(this, source, modello, null, true);
	}


	public int getRetryTimeout() {
    	return RETRY_TIMEOUT;
    }

    public boolean sendMessage(Message m) {
    	if (EDSMessage.class.isInstance(m)) {
    		return sendMessage((EDSMessage) m);
    	} else {
    		return false;
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
		boolean retval = false;
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
				try {
					transportSemaphore.acquire();
					transport.write(m.getBytesMessage());
					transportSemaphore.release();
				} catch (InterruptedException e) {
					logger.error("Interrupted:",e);
				}
				// non c'e' modo di sapere se e' arrivato; siamo ottimisti.
				retval = true;
			}
		}
		return retval;
	}

	/**
	 * Invia un messaggio broadcast.
	 * 
	 * <p>Gli header0
	 *  vengono ri-generati ad ogni invio.</p>
	 */
	private void sendBroadcastMessage(BroadcastMessage m) {
		int tries = m.getSendTries();
		try {
			transportSemaphore.acquire();
			for (int i = 0; i < tries; i++) {
				try {
					Thread.sleep(getRetryTimeout());
				} catch (InterruptedException e) {
				}
				transport.write(m.getBytesMessage());
			}
			transportSemaphore.release();
		} catch (InterruptedException e) {
			logger.error("Interrupted:",e);
		}
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
		if (messageToBeAnswered != null) {
			if (messageToBeAnswered.compareTo(m) == 0) {
				logger.debug("Messaggio gia' inviato in attesa di risposta: "+m);				
				return true;
			}
		}
		try {
			transportSemaphore.acquire();
			if (messageToBeAnswered != null) {
				logger.error("messageToBeAnswered non nullo: "+messageToBeAnswered);
				logger.error("Messaggio in attesa :"+m);
				return false;
			}
        	messageToBeAnswered = m;
	    	synchronized (messageToBeAnswered) {
	    		for (tries = 1;
	    			(tries <= m.getMaxSendTries()) && (!m.isAnswered()); 
	    			tries++) {
	    			logger.trace("Invio "+tries+" di "+m.getMaxSendTries()+" : "+m.toHexString());
	    			while (mp.isBusy()) {
	    				logger.trace("Delaying... "+mp.dumpBuffer());
	    				Thread.sleep(100);
	    			}
	    			transport.write(m.getBytesMessage());
	    			// si mette in attesa, ma se nel frattempo arriva la risposta viene avvisato
	    	    	try {
	    				//transportSemaphore.release();
	    	    		messageToBeAnswered.wait((long)(getRetryTimeout() * (1 + 0.2 * Math.random())));
	    				//transportSemaphore.acquire();
	    	    	} catch (InterruptedException e) {
	    				logger.trace("sendPTPRequest wait:2");
	    	    	}
	    		}
		    	received = m.isAnswered();
		    	messageToBeAnswered = null;
	    	}
			transportSemaphore.release();
		} catch (InterruptedException e) {
			logger.error("Interrupted:",e);
		}
    	if (! received) {
    		logger.error("Messaggio non risposto: "+m);
    	}
		return received;
    }

	/**
     * "Scopre" il BMC indicato inviandogli un messaggio di richiesta modello.
     * 
     * <p>Se il BMC non era gia' in lista, allora verra' inserito dal metodo 
     * messageReceived().
     * 
     * @param address l'indirizzo del BMC da "scoprire".
     * 
     */
    public void discoverBMC(int address) {
    	queueMessage(new RichiestaModelloMessage(address,getMyAddress()));
    }

}
