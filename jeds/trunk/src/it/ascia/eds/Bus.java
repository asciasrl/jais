/**
 * Copyright (C) 2008 ASCIA S.R.L.
 */
package it.ascia.eds;

import java.io.IOException;
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
	 * Quanto tempo aspettare la risposta dopo l'invio di un messaggio.
	 * 
	 * Nel caso peggiore (1200 bps), la trasmissione di un messaggio richiede 
	 * 8 / 120 = 660 msec. In quello migliore (9600 bps), la trasmissione 
	 * richiede 82 msec. Questa costante deve tener conto del caso migliore.
	 */
	public static final int PING_WAIT = 200;
	/**
	 * Quante volte aspettare PING_WAIT prima di ritrasmettere.
	 * 
	 * Questo indica quante volte si attende PING_WAIT millisecondi, prima di
	 * riprovare a inviare un messaggio. Condizione da rispettare è che 
	 * PING_WAIT * WAIT_RETRIES sia maggiore del tempo più lungo previsto per 
	 * il round-trip di un messaggio.
	 * 
	 * All'attesa deve essere aggiunto un ritardo casuale.
	 */
	public static final int WAIT_RETRIES = 6;
	/**
	 * Quante volte provare a reinviare un messaggio che richiede una risposta.
	 * 
	 * Quando l'attesa supera PING_WAIT * WAIT_RETRIES, questa costante decide
	 * quanti tentativi di ri-invio effettuare.
	 */
	public static final int ACKMESSAGE_SEND_RETRIES = 2;
	/**
	 * Quante volte provare a reinviare un messaggio di richiesta stato 
	 * senza risposta.
	 * 
	 * Quando l'attesa supera PING_WAIT * WAIT_RETRIES, questa costante decide
	 * quanti tentativi di ri-invio effettuare.
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
	 * I device presenti nel bus.
	 */
    private Map devices;
    /**
     * Il BMC "finto" che corrisponde a questo computer.
     */
    private BMCComputer bmcComputer;
    
    public Bus() {
        devices = new HashMap();
        bmcComputer = null;
		mp = new MessageParser();
    }
    
    /**
     * Verifica se ci sono dati pronti da leggere.
     * 
     * @returns true se ci sono dati leggibili da readByte()
     */
    protected abstract boolean hasData();
    
    /**
     * Ritorna il prossimo byte ricevuto.
     * @throws IOException 
     * 
     * @returns il dato ricevuto.
     */
    protected abstract byte readByte() throws IOException;
    
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
    	devices.put(new Integer(bmcComputer.getAddress()), bmcComputer);
    }
    
    /**
     * Ritorna l'indirizzo del BMCComputer del bus.
     * 
     * Questo metodo è utile per i BMC, quando devono richiedere informazioni sul
     * proprio stato. I messaggi che inviano devono partire "a nome" del
     * BMCComputer.
     */
    public int getBMCComputerAddress() {
    	return bmcComputer.getAddress();
    }
    
    /**
     * Legge e interpreta i dati in arrivo.
     * 
     * Questa funzione deve essere chiamata dalla sottoclasse, quando ci
     * sono dati pronti da leggere con readByte().
     * 
     * I messaggi decodificati vengono passati a dispatchMessage().
     */
    protected void readData() {
    	while (hasData()) {
    		try {
    			byte b = readByte();
    			mp.push(b);
    			if (mp.isValid()) {
    				Message m = mp.getMessage();
//  				if (!m.getTipoMessaggio().equals("Aknowledge")) {
//  				System.out.println((new Date()).toString() + "\r\n" + m);
//  				}
    				dispatchMessage(m);
    				//mp.clear();
    			}
    		} catch (IOException e) {
    			System.err.println("Errore di lettura: " + e.getMessage());
    		}
    	} // while hasData()
    }

    /**
     * Invia un messaggio a tutti i BMC destinatari e al mittente.
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
    	// System.out.println("Messaggio da " + sender + " per " + rcpt);
    	if (BroadcastMessage.class.isInstance(m)) { 
    		// Mandiamo il messaggio a tutti
    		Iterator it = devices.values().iterator();
    		while (it.hasNext()) {
    			Device bmc = (Device)it.next();
    			bmc.messageReceived(m);
    		}
    	} else { 
    		// Non e' un messaggio broadcast: va mandato al destinatario...
    		Device bmc = (Device)devices.get(new Integer(rcpt));
    		if (bmc != null) {
    			bmc.messageReceived(m);
    		} else {
    			/*System.err.println("Ricevuto un messaggio per il BMC " + 
    					rcpt + " che non conosco:");
    			System.err.println((new Date()).toString() + "\r\n" + m);*/
    		}
    		// ...e al mittente
    		bmc = (Device)devices.get(new Integer(sender));
    		if (bmc != null) {
    			bmc.messageSent(m);
    		} else {
    			/*System.err.println("Ricevuto un messaggio inviato dal BMC " + 
    					rcpt + " che non conosco:");
    			System.err.println((new Date()).toString() + "\r\n" + m);*/
    		}
    		// Lo mandiamo anche al BMCComputer, se non era per lui
    		if ((bmcComputer != null) && (rcpt != bmcComputer.getAddress())) { 
    	   		bmcComputer.messageReceived(m);
    		}
    	}
    }

    /**
     * Invia un messaggio e attende una risposta dal destinatario.
     * 
     * @returns true se il messaggio di risposta è arrivato.
     */
    public boolean sendPTPRequest(PTPRequest m) {
    	if (bmcComputer != null) {
    		return bmcComputer.sendPTPRequest(m);
    	} else {
    		System.err.println("Il bus non ha un BMCComputer!");
    		return false;
    	}
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
     * 
     * @throws un'EDSException se esiste gia' un device con lo stesso indirizzo.
     */
    public void addDevice(Device device) throws EDSException {
    	int deviceAddress = device.getAddress();
    	if (getDevice(deviceAddress) != null) {
    		throw new EDSException("Un BMC con indirizzo " + deviceAddress +
    				" esiste gia'.");
    	}
    	devices.put(new Integer(deviceAddress), device);
    }
}
