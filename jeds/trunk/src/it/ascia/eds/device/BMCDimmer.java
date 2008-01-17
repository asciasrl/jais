/**
 * Copyright (C) 2008 ASCIA S.R.L.
 */
package it.ascia.eds.device;

import java.util.Vector;

import it.ascia.eds.msg.Message;

/**
 * Un BMC con 1 o 2 porte di output.
 * 
 * Modelli: 101, 102, 103, 104, 106, 111
 * 
 * @author arrigo
 */
public class BMCDimmer extends BMC {

	/**
	 * Numero di canali in uscita
	 */
	private int outPortsNum;
	/**
	 * Uscite
	 */
	Vector outPorts;
	/**
	 * Potenza in uscita [Watt] o -1 se l'uscita è 0-10 V
	 */
	private int power;
	/**
	 * Nome dato dal costruttore
	 */
	private String modelName;
	
	/**
	 * Costruttore
	 * @param address indirizzo del BMC
	 * @param model numero del modello
	 */
	public BMCDimmer(int address, int model) {
		super(address, model);
		switch(model) {
		case 101:
			outPortsNum = 2;
			power = 800;
			modelName = "Base low power";
			break;
		case 102:
			outPortsNum = 2;
			power = 800;
			modelName = "Evolution low power";
			break;
		case 103:
			outPortsNum = 2;
			power = 2000;
			modelName = "Base high power";
			break;
		case 104:
			outPortsNum = 2;
			power = 2000;
			modelName = "Evolution high power";
			break;
		case 106:
			outPortsNum = 1;
			power = -1;
			modelName = "sperimentale 0-10 V";
			break;
		case 111:
			outPortsNum = 1;
			power = -1;
			modelName = "0-10 V";
		break;
		default: // This should not happen(TM)
			System.err.println("Errore: modello di BMCDimmer sconosciuto:" +
					model);
			power = outPortsNum = 0;
			modelName = "Dimmer sconosciuto";
		}
		if (outPortsNum > 0) {
			outPorts = new Vector(outPortsNum);
		}
	}
	
	/* (non-Javadoc)
	 * @see it.ascia.eds.device.BMC#receiveMessage(it.ascia.eds.msg.Message)
	 */
	public void receiveMessage(Message m) {
		// TODO
	}
	
	public String getInfo() {
		String retval;
		retval = "Dimmer (modello " + model + ", \"" + modelName + "\") con " + 
			outPortsNum + " uscite"; 
		if (power >= 0) retval += " a " + power + " Watt";
		return retval;
	}
}
