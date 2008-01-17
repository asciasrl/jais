/**
 * Copyright (C) 2008 ASCIA S.R.L.
 */
package it.ascia.eds.device;

import java.util.Vector;

import it.ascia.eds.msg.Message;

/**
 * Una centralina scenari.
 * 
 * Modelli: 152, 154, 156, 158
 * 
 * @author arrigo
 */
public class BMCScenarioManager extends BMC {

	/**
	 * Numero di porte in ingresso
	 */
	private int inPortsNum;
	/**
	 * Ingressi
	 */
	Vector inPorts;
	
	/**
	 * Costruttore
	 * @param address indirizzo del BMC
	 * @param model numero del modello
	 */
	public BMCScenarioManager(int address, int model) {
		super(address, model);
		switch(model) {
		case 152:
			inPortsNum = 2;
			break;
		case 154:
			inPortsNum = 4;
			break;
		case 156:
			inPortsNum = 6;
			break;
		case 158:
			inPortsNum = 8;
			break;
		default: // This should not happen(TM)
			System.err.println("Errore: modello di centralina scenari " + 
					"sconosciuto: " + model);
			inPortsNum = 0;
		}
		if (inPortsNum > 0) {
			inPorts = new Vector(inPortsNum);
		}
	}
	
	/* (non-Javadoc)
	 * @see it.ascia.eds.device.BMC#receiveMessage(it.ascia.eds.msg.Message)
	 */
	public void receiveMessage(Message m) {
		// TODO
	}
	
	public String getInfo() {
		return "BMC centralina scenari (modello " + model + ") con " + 
			inPortsNum + " porte di input";
	}
}
