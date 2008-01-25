/**
 * Copyright (C) 2008 ASCIA S.R.L.
 */
package it.ascia.eds.device;

import it.ascia.eds.Bus;
import it.ascia.eds.msg.Message;

/**
 * Un BMC con una porta a infrarossi.
 * 
 * Modelli: 131
 * 
 * @author arrigo
 */
public class BMCIntIR extends BMC {

	/**
	 * Ingresso IR
	 */
	int irInput;
	
	/**
	 * Costruttore
	 * @param address indirizzo del BMC
	 * @param model numero del modello
	 */
	public BMCIntIR(int address, int model, Bus bus, String name) {
		super(address, model, bus, name);
		if (model != 131) {
			System.err.println("Errore: modello di BMC Int IR sconosciuto:" + 
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
		// TODO
	}
	
	public String getInfo() {
		return getName() + ": BMC Int IR (modello " + model + ")";
	}

	public void updateStatus() {
		System.err.println("updateStatus non implementato su BMCIntIR");
	}

	public String getStatus() { // TODO
		return name;
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
}
