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
	public BMCIntIR(int address, int model, Bus bus) {
		super(address, model, bus, "BMCIntIR");
		if (model != 131) {
			System.err.println("Errore: modello di BMC Int IR sconosciuto:" + 
					model);
		}
	}
	
	/* (non-Javadoc)
	 * @see it.ascia.eds.device.BMC#receiveMessage(it.ascia.eds.msg.Message)
	 */
	public void receiveMessage(Message m) {
		// TODO
	}
	
	public String getInfo() {
		return "BMC Int IR (modello " + model + ")";
	}

	public void updateStatus() {
		System.err.println("updateStatus non implementato su BMCIntIR");
	}

	public String getStatus() { // TODO
		return name;
	}
}
