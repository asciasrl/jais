/**
 * Copyright (C) 2008 ASCIA S.R.L.
 */
package it.ascia.eds;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.log4j.Logger;

import it.ascia.ais.MessageInterface;
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
	protected int RETRY_TIMEOUT = 500;

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
     * Il nostro nome secondo AUI.
     */
    private String name;
    
    /**
     * Connettore per il BUS EDS.
     * 
     * @param name il nome del transport, che sara' la parte iniziale degli indirizzi
     * di tutti i Device collegati a questo transport.
     */
    public EDSConnector(String name) {
        devices = new HashMap();
        bmcComputer = null;
		mp = new MessageParser();
		logger = Logger.getLogger(getClass());
		this.name = name;
    }
    
    
    /**
     * Ritorna il nome di questa istanza di connettore.
     */
    public String getName() {
    	return name;
    }
    
    /**
     * Imposta il BMCComputer del transport.
     */
    public void setBMCComputer(BMCComputer bmcComputer) {
    	this.bmcComputer = bmcComputer;
    	devices.put(new Integer(bmcComputer.getAddress()), bmcComputer);
    }
    
    /**
     * Ritorna l'indirizzo del BMCComputer del transport.
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
    public void readData() {
    	while (transport.hasData()) {
    		try {
    			byte b = transport.readByte();
    			mp.push(b);
    			if (mp.isValid()) {
    				EDSMessage m = mp.getMessage();
//  				if (!m.getTipoMessaggio().equals("Aknowledge")) {
//  				System.out.println((new Date()).toString() + "\r\n" + m);
//  				}
    				if (m != null) {
    					dispatchMessage(m);
    				}
    				//mp.clear();
    			}
    		} catch (IOException e) {
    			logger.error("Errore di lettura: " + e.getMessage());
    		}
    	} // while hasData()
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
     */
    private void dispatchMessage(EDSMessage m) {
    	int rcpt = m.getRecipient();
    	int sender = m.getSender();
    	// logger.trace("Messaggio da " + sender + " per " + rcpt);
    	if (BroadcastMessage.class.isInstance(m)) { 
    		// Mandiamo il messaggio a tutti
    		Iterator it = devices.values().iterator();
    		while (it.hasNext()) {
    			BMC bmc = (BMC)it.next();
    			bmc.messageReceived(m);
    		}
    	} else { 
    		// Non e' un messaggio broadcast: va mandato al destinatario...
    		BMC bmc = (BMC)devices.get(new Integer(rcpt));
    		if (bmc != null) {
    			bmc.messageReceived(m);
    		} else {
    			/*logger.error("Ricevuto un messaggio per il BMC " + 
    					rcpt + " che non conosco:");*/    		}
    		// ...e al mittente
    		bmc = (BMC)devices.get(new Integer(sender));
    		if (bmc != null) {
    			bmc.messageSent(m);
    		} else {
    			/*logger.error("Ricevuto un messaggio inviato dal BMC " + 
    					rcpt + " che non conosco:");*/
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
    public boolean sendMessage(MessageInterface m) {
    	if (bmcComputer != null) {
    		return bmcComputer.sendMessage((EDSMessage)m);
    	} else {
    		logger.error("Il transport non ha un BMCComputer!");
    		return false;
    	}
    }
    
    /**
     * Aggiunge un Device collegato al bus.
     * 
     * @param device il Device da aggiungere.
     * 
     * @throws un'EDSException se esiste gia' un device con lo stesso indirizzo.
     */
    public void addDevice(BMC device) throws EDSException {
    	String deviceAddress = device.getAddress();
    	if (getDevices(deviceAddress).length != 0) {
    		throw new EDSException("Un BMC con indirizzo " + deviceAddress +
    				" esiste gia'.");
    	}
    	devices.put(new Integer(deviceAddress), device);
    }
    
    /*
    public Device[] getDevices(String address) {
    	Collection values = devices.values();
    	if (address.equals("*")) {
    		return (BMC[]) values.toArray(new BMC[values.size()]);
    	}
    	List devices = new LinkedList();
    	Iterator it = values.iterator();
    	while (it.hasNext()) {
    		BMC device =  (BMC)it.next();
    		if (device.getAddress().equals(address)) {
    			devices.add(device);
    		}
    	}
    	return (BMC[]) devices.toArray(new BMC[devices.size()]);
    }
    */
    
    public int getRetryTimeout() {
    	return RETRY_TIMEOUT;
    }
        
}
