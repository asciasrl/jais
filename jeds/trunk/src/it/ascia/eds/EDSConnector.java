/**
 * Copyright (C) 2008 ASCIA S.R.L.
 */
package it.ascia.eds;

import java.io.IOException;
import java.util.Iterator;
import it.ascia.ais.AISException;
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
public class EDSConnector extends it.ascia.ais.Connector {
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
	 * MessageParser per la lettura dei messaggi in ingresso.
	 */
	protected MessageParser mp;
    /**
     * Il BMC "finto" che corrisponde a questo.
     */
    private BMCComputer bmcComputer;
    
    /**
     * Connettore per il BUS EDS.
     * 
     * @param name il nome del Connector, che sara' la parte iniziale degli indirizzi
     * di tutti i Device collegati a questo Connector.
     */
    public EDSConnector(String name, Controller controller) {
    	super(name,controller);
        bmcComputer = null;
		mp = new MessageParser();
    }
    
    
    /**
     * Imposta il BMCComputer del connector.
     * @throws AISException 
     */
    public void setBMCComputer(BMCComputer bmcComputer) throws AISException {
    	this.bmcComputer = bmcComputer;
    }
    
    public BMCComputer getBMCComputer() {
    	return this.bmcComputer;
    }
        
    /**
     * Ritorna l'indirizzo del BMCComputer del connettore.
     * 
     * <p>Questo metodo e' utile per i BMC, quando devono richiedere 
     * informazioni sul proprio stato. I messaggi che inviano devono partire 
     * "a nome" del BMCComputer.</p>
     */
    public int getBMCComputerAddress() {
    	return bmcComputer.getIntAddress();
    }
    
    /**
     * Legge e interpreta i dati in arrivo.
     * 
     * <p>Questa funzione deve essere chiamata dalla sottoclasse, quando ci
     * sono dati pronti da leggere con readByte().</p>
     * 
     * <p>I messaggi decodificati vengono passati a dispatchMessage().</p>
     */
    /* TODO eliminare readData ?
    public void readData() {
    	while (transport.hasData()) {
    		try {
    			byte b = transport.readByte();
    			mp.push(b);
    			if (mp.isValid()) {
    				EDSMessage m = mp.getMessage();
    				// TODO logger.trace("dispatchMessage: "+m);
    				if (m != null) {
    					dispatchMessage(m);
    				}
    			}
    		} catch (IOException e) {
    			logger.error("Errore di lettura: " + e.getMessage());
    		} catch (AISException e) {
    			logger.error("Errore: " + e.getMessage());
			}
    	} // while hasData()
    }
    */
    
    /**
     * Gestisce ogni byte ricevuto
     */
    public void received(byte b) {
		mp.push(b);
		if (mp.isValid()) {
			EDSMessage m = mp.getMessage();
			if (m != null) {
				try {
					dispatchMessage(m);
				} catch (AISException e) {
	    			logger.error("Errore: " + e.getMessage());
				}
			}
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
    private void dispatchMessage(EDSMessage m) throws AISException {
    	int rcpt = m.getRecipient();
    	int sender = m.getSender();
    	// TODO logger.trace("Dispath: BEGIN da " + sender + " per " + rcpt);
    	if (BroadcastMessage.class.isInstance(m)) { 
    		// Mandiamo il messaggio a tutti
    		Iterator it = getDevices().entrySet().iterator();
    		while (it.hasNext()) {
    			BMC bmc = (BMC)it.next();
    			bmc.messageReceived(m);
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

    		// Lo mandiamo anche al BMCComputer, se non era per lui
    		if ((bmcComputer != null) && 
    				(rcpt != bmcComputer.getIntAddress())) { 
    	   		bmcComputer.messageReceived(m);
    		}
    	}
    }

    /**
     * Invia un messaggio e attende una risposta dal destinatario, se il
     * messaggio lo richiede.
     * 
     * @return true se il messaggio di risposta e' arrivato, o se l'invio e'
     * andato a buon fine.
     */
    public boolean sendMessage(Message m) {
    	if (bmcComputer != null) {
    		return bmcComputer.sendMessage((EDSMessage)m);
    	} else {
    		logger.error("Il connector non ha un BMCComputer!");
    		return false;
    	}
    }
    
    public int getRetryTimeout() {
    	return RETRY_TIMEOUT;
    }
        
}
