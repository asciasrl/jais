/**
 * Copyright (C) 2008 ASCIA S.R.L.
 */
package it.ascia.eds.device;

import java.util.Vector;

import it.ascia.eds.Bus;
import it.ascia.eds.EDSException;
import it.ascia.eds.msg.Message;
import it.ascia.eds.msg.PTPRequest;
import it.ascia.eds.msg.RichiestaStatoMessage;
import it.ascia.eds.msg.RispostaStatoMessage;

/**
 * Una centralina scenari.
 * 
 * Modelli: 152, 154, 156, 158
 * 
 * @author arrigo
 */
public class BMCScenarioManager extends BMC {
	/**
	 * Numero ingressi digitali.
	 */
	private int inPortsNum;
	/**
	 * Numero di porte in uscita (sempre 8).
	 */
	private final int outPortsNum = 8;
	/**
	 * Ingressi.
	 */
	boolean inPorts[];
	/**
	 * Uscite.
	 */
	private boolean outPorts[];
	
	/**
	 * Costruttore
	 * @param address indirizzo del BMC
	 * @param model numero del modello
	 */
	public BMCScenarioManager(int address, int model, Bus bus, String name) {
		super(address, model, bus, name);
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
			inPorts = new boolean[inPortsNum];
		}
		outPorts = new boolean[outPortsNum];
	}
	
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
			int i;
			temp = r.getOutputs();
			for (i = 0; i < outPortsNum; i++) {
				outPorts[i] = temp[i];
			}
			temp = r.getInputs();
			for (i = 0; i < inPortsNum; i++) {
				inPorts[i] = temp[i];
			}
		}
		break;
		}
	}
	
	public String getInfo() {
		return getName() + ": BMC centralina scenari (modello " + model + ")" +
			" con " + inPortsNum + " ingressi digitali";
	}

	// Attenzione: chiama sempre updateStatus() !
	public String getStatus(String port, String busName) {
		String retval = "";
		int i;
		String compactName = busName + "." + getAddress();
		updateStatus();
		for (i = 0; i < inPortsNum; i++) {
			if (port.equals("*") || port.equals(getInputCompactName(i))) {
				retval += compactName + ":" + getInputCompactName(i) + "=" + 
					(inPorts[i]? "ON" : "OFF") + "\n";
			}
		}
	 	for (i = 0; i < outPortsNum; i++) {
	 		if (port.equals("*") || port.equals(getOutputCompactName(i))) {
	 			retval += compactName + ":" + getOutputCompactName(i) + "=" + 
	 				(outPorts[i]? "ON" : "OFF") + "\n";
	 		}
		}
	 	return retval;
	}
	
	public int getFirstInputPortNumber() {
		return 1;
	}

	public int getOutPortsNumber() {
		return 8;
	}

	public void setPort(String port, String value) throws EDSException {
		throw new EDSException("Not implemented.");
	}

}
