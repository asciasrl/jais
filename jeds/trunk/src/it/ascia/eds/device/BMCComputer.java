/**
 * COPYRIGHT (C) 2008 ASCIA S.R.L.
 */
package it.ascia.eds.device;

import java.util.*;

import it.ascia.eds.msg.Message;
import it.ascia.eds.msg.RichiestaModelloMessage;
import it.ascia.eds.msg.RispostaModelloMessage;
import it.ascia.eds.Bus;

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
	 * Il nostro indirizzo sul bus.
	 */
	private int address;
	/**
	 * Il nostro bus.
	 */
	private Bus bus;
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
		super(address, -1, bus);
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
			BMC bmc;
			int model;
			RispostaModelloMessage risposta = (RispostaModelloMessage) m;
			model = risposta.getModello();
			switch(model) {
			case 88:
			case 8:
			case 40:
			case 60:
			case 44:
				bmc = new BMCStandardIO(address, model, bus);
				break;
			case 41:
			case 61:
			case 81:
				bmc = new BMCIR(address, model, bus);
				break;
			case 101:
			case 102:
			case 103:
			case 104:
			case 106:
			case 111:
				bmc = new BMCDimmer(address, model, bus);
				break;
			case 131:
				bmc = new BMCIntIR(address, model, bus);
				break;
			case 152:
			case 154:
			case 156:
			case 158:
				bmc = new BMCScenarioManager(address, model, bus);
				break;
			case 127:
				bmc = new BMCChronoTerm(address, model, bus);
				break;
			default:
				System.err.println("Modello di BMC sconosciuto: " + 
						model);
			bmc = null;
			}
			if (bmc != null) {
				bus.addDevice(bmc);
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
}
