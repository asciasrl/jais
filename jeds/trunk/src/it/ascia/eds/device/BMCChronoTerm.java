/**
 * Copyright (C) 2008 ASCIA S.R.L.
 */
package it.ascia.eds.device;

import java.util.Vector;

import it.ascia.eds.Bus;
import it.ascia.eds.msg.Message;

/**
 * Un BMC cronotermostato.
 * 
 * Modelli: 127
 * 
 * @author arrigo
 */
public class BMCChronoTerm extends BMC {
	/**
	 * Ingresso termometro
	 */
	int temperature;
	
	/**
	 * Costruttore
	 * @param address indirizzo del BMC
	 * @param model numero del modello
	 */
	public BMCChronoTerm(int address, int model, Bus bus) {
		super(address, model, bus);
		switch(model) {
		case 127:
			break;
		default: // This should not happen(TM)
			System.err.println("Errore: modello di BMCChronoTerm sconosciuto:" +
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
		return "BMC cronotermostato (modello " + model + ")";
	}

	public void updateStatus() {
		System.err.println("updateStatus() non implementato");
	}
}
