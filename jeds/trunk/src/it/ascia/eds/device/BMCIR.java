/**
 * Copyright (C) 2008 ASCIA S.R.L.
 */
package it.ascia.eds.device;

import java.util.Vector;

import it.ascia.eds.Bus;
import it.ascia.eds.EDSException;
import it.ascia.eds.msg.Message;
import it.ascia.eds.msg.RispostaStatoMessage;

/**
 * Un BMC con porte di input + 1 a infrarossi.
 * 
 * Gli ingressi sono "mappati" su porte di ingresso. Quindi gli ingressi
 * logici sono sempre 8. Alcuni possono essere _anche_ fili.
 * 
 * Modelli: 41, 61, 81
 * 
 * @author arrigo
 */
public class BMCIR extends BMC {

	/**
	 * Numero di porte in ingresso
	 */
	private final int inPortsNum = 8;
	/**
	 * Ingressi
	 */
	boolean inPorts[];
	/**
	 * Ingresso IR
	 */
	int irInput;
	
	/**
	 * Costruttore
	 * @param address indirizzo del BMC
	 * @param model numero del modello
	 */
	public BMCIR(int address, int model, Bus bus, String name) {
		super(address, model, bus, name);
		switch(model) {
		case 41:
		case 61:
		case 81:
			break;
		default: // This should not happen(TM)
			logger.error("Errore: modello di BMCIR sconosciuto:" + model);
		}
		inPorts = new boolean[inPortsNum];
	}
	
	/* (non-Javadoc)
	 * @see it.ascia.eds.device.BMC#receiveMessage(it.ascia.eds.msg.Message)
	 */
	public void messageReceived(Message m) {
		// TODO
	}
	
	public void messageSent(Message m) {
		switch (m.getMessageType()) {
		case Message.MSG_RISPOSTA_STATO: {
			RispostaStatoMessage r;
			r = (RispostaStatoMessage)m;
			// Di questo messaggio ci interessano solo gli ingressi.
			boolean temp[];
			int i;
			temp = r.getInputs();
			for (i = 0; i < inPortsNum; i++) {
				inPorts[i] = temp[i];
			}
		}
		break;
		}
	}
	
	public String getInfo() {
		return getName() + ": BMC IR (modello " + model + ") con " + 
			inPortsNum + " porte di input";
	}

	// Attenzione: chiama sempre updateStatus() !
	public String getStatus(String port) {
		String retval = "";
		String busName = bus.getName();
		int i;
		String compactName = busName + "." + getAddress();
		updateStatus();
		for (i = 0; i < inPortsNum; i++) {
			if (port.equals("*") || port.equals(getInputCompactName(i))) {
				retval += compactName + ":" + getInputCompactName(i) + "=" + 
					(inPorts[i]? "ON" : "OFF") + "\n";
			}
		}
	 	return retval;
	
	}
	
	/**
	 * Questo e' uno dei pochi BMC con gli ingressi che possono avere
	 * indirizzo 0.
	 */
	public int getFirstInputPortNumber() {
		return 0;
	}

	public int getOutPortsNumber() {
		return 0;
	}

	public void setPort(String port, String value) throws EDSException {
		throw new EDSException("Not implemented.");
	}
}
