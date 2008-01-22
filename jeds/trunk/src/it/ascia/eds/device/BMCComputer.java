/**
 * COPYRIGHT (C) 2008 ASCIA S.R.L.
 */
package it.ascia.eds.device;

import java.util.*;

import it.ascia.eds.msg.Message;
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
	 * Costruttore.
	 * 
	 * @param bus il bus a cui siamo collegati
	 * @param address l'indirizzo di questo device sul bus
	 */
	public BMCComputer(int address, Bus bus) {
		super(address, -1, bus, "Computer");
		inbox = new LinkedList();
		outbox = new LinkedList();
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
	public void receiveMessage(Message m) {
		if (RispostaModelloMessage.class.isInstance(m)) {
			RispostaModelloMessage risposta = (RispostaModelloMessage) m;
			try {
				BMC.createBMC(risposta.getSender(), risposta.getModello(), null,
						bus);
			} catch (EDSException e) {
				// Se il device e' gia' sul bus, non e' un errore.
			}
		}
		// Tutti i messaggi ricevuti devono finire nella inbox
		inbox.addLast(m);
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
    		if (bus.sendPTPMessage(new RichiestaModelloMessage(address, 
    				this.getAddress()))) {
    			retval = (BMC)bus.getDevice(address);
    		} else {
    			retval = null;
    		}
    	}
    	return retval;
    }

	public void updateStatus() {
		System.err.println("updateStatus non implementato su BMCComputer.");
	}

	public String getStatus() {
		return null;
	}
}
