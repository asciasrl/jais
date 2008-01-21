/**
 * Copyright (C) 2008 ASCIA S.R.L.
 */
package it.ascia.eds.device;

import java.util.Vector;

import it.ascia.eds.Bus;
import it.ascia.eds.msg.Message;

/**
 * Un BMC con porte di input + 1 a infrarossi.
 * 
 * Modelli: 41, 61, 81
 * 
 * @author arrigo
 */
public class BMCIR extends BMC {

	/**
	 * Numero di porte in ingresso
	 */
	private int inPortsNum;
	/**
	 * Numero di porte in uscita
	 */
	private int outPortsNum;
	/**
	 * Ingressi
	 */
	Vector inPorts;
	/**
	 * Ingresso IR
	 */
	int irInput;
	
	/**
	 * Costruttore
	 * @param address indirizzo del BMC
	 * @param model numero del modello
	 */
	public BMCIR(int address, int model, Bus bus) {
		super(address, model, bus);
		switch(model) {
		case 41:
			inPortsNum = 4;
			break;
		case 61:
			inPortsNum = 6;
			break;
		case 81:
			inPortsNum = 8;
			break;
		default: // This should not happen(TM)
			System.err.println("Errore: modello di BMCIR sconosciuto:" + model);
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
		return "BMC IR (modello " + model + ") con " + inPortsNum + 
			" porte di input";
	}
	
	public void updateStatus() {
		System.err.println("updateStatus non implementato su BMCIR");
	}
}
