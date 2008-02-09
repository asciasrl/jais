/**
 * Copyright (C) 2008 ASCIA S.R.L.
 */
package it.ascia.eds.device;

import it.ascia.eds.Bus;
import it.ascia.eds.EDSException;
import it.ascia.eds.msg.Message;
import it.ascia.eds.msg.RispostaStatoMessage;

/**
 * Un BMC con una porta a infrarossi.
 * 
 * Modelli: 131
 * 
 * @author arrigo
 */
public class BMCIntIR extends BMC {
	/**
	 * Ingresso IR.
	 */
	private boolean irInput;
	
	/**
	 * Costruttore.
	 * 
	 * @param address indirizzo del BMC
	 * @param model numero del modello
	 */
	public BMCIntIR(int address, int model, Bus bus, String name) {
		super(address, model, bus, name);
		if (model != 131) {
			logger.error("Errore: modello di BMC Int IR sconosciuto:" + 
					model);
		}
	}
	
	/* (non-Javadoc)
	 * @see it.ascia.eds.device.BMC#messageReceived(it.ascia.eds.msg.Message)
	 */
	public void messageReceived(Message m) {
		// TODO
	}
	
	public void messageSent(Message m) {
		switch (m.getMessageType()) {
		case Message.MSG_RISPOSTA_STATO: {
			RispostaStatoMessage r;
			r = (RispostaStatoMessage)m;
			// Il RispostaStatoMessage da' sempre 8 valori. Dobbiamo
			// prendere solo quelli effettivamente presenti sul BMC
			boolean temp[];
			temp = r.getInputs();
			irInput = temp[0];
		}
		break;
		}
	}
	
	public String getInfo() {
		return getName() + ": BMC Int IR (modello " + model + ")";
	}

	public String getStatus(String port, String busName) { // TODO
		String compactName = busName + "." + getAddress();
		return compactName + ":" + getInputCompactName(0) +
			"=" + (irInput? "ON" : "OFF") + "\n";
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
