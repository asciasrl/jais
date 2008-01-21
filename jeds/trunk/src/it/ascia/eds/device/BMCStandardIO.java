/**
 * Copyright (C) 2008 ASCIA S.R.L.
 */
package it.ascia.eds.device;

import java.util.Vector;

import it.ascia.eds.Bus;
import it.ascia.eds.msg.Message;
import it.ascia.eds.msg.RichiestaStatoMessage;
import it.ascia.eds.msg.RispostaModelloMessage;
import it.ascia.eds.msg.RispostaStatoMessage;

/**
 * Un BMC con porte di input e output.
 * 
 * Modelli: 88, 8, 40, 60, 44
 * 
 * @author arrigo
 */
public class BMCStandardIO extends BMC {

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
	private boolean [] inPorts;
	/**
	 * Uscite
	 */
	private boolean [] outPorts;
	
	/**
	 * Costruttore
	 * @param address indirizzo del BMC
	 * @param model numero del modello
	 */
	public BMCStandardIO(int address, int model, Bus bus) {
		super(address, model, bus);
		switch(model) {
		case 88:
			inPortsNum = outPortsNum = 8;
			break;
		case 8:
			inPortsNum = 0;
			outPortsNum = 8;
			break;
		case 40:
			inPortsNum = 4;
			outPortsNum = 0;
			break;
		case 60:
			inPortsNum = 6;
			outPortsNum = 0;
			break;
		case 44:
			inPortsNum = 4;
			outPortsNum = 4;
		break;
		default: // This should not happen(TM)
			System.err.println("Errore: modello di BMCStandardIO sconosciuto:" +
					model);
			inPortsNum = outPortsNum = 0;
		}
		if (inPortsNum > 0) {
			inPorts = new boolean[inPortsNum];
		}
		if (outPortsNum > 0) {
			outPorts = new boolean[outPortsNum];
		}
	}
	
	/* (non-Javadoc)
	 * @see it.ascia.eds.device.BMC#receiveMessage(it.ascia.eds.msg.Message)
	 */
	public void receiveMessage(Message m) {
		if (RispostaStatoMessage.class.isInstance(m)) {
			RispostaStatoMessage r = (RispostaStatoMessage)m;
			if (m.getSender() == getAddress()) {
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
			} // if il sender sono io
		} // if RispostaStatoMessage
	}
	
	/**
	 * Ritorna lo stato degli ingressi.
	 * 
	 * @returns un'array di booleani: true vuol dire acceso.
	 */
	public boolean[] getInputs() {
		return inPorts;
	}
	
	/**
	 * Ritorna lo stato delle uscite.
	 * 
	 * @returns un'array di booleani: true vuol dire acceso.
	 */
	public boolean[] getOutputs() {
		return outPorts;
	}
	
	public String getInfo() {
		return "BMC Standard I/O (modello " + model + ") con " + inPortsNum + 
			" porte di input e " + outPortsNum + " porte di output";
	}
	
	/**
	 * Aggiorna la rappresentazione interna delle porte.
	 * 
	 * Manda un messaggio al BMC mettendo come mittente il bmcComputer. Quando 
	 * arrivera' la risposta, receiveMessage() aggiornera' le informazioni.
	 */
	public void updateStatus() {
		Message m;
		m = new RichiestaStatoMessage(getAddress(), bus.getBMCComputerAddress(),
				0);
		bus.sendPTPMessage(m);
	}
	
	/**
	 * Stampa una descrizione dello stato del BMC.
	 */
	public void printStatus() {
		int i;
		System.out.print("Ingressi: ");
		for (i = 0; i < inPortsNum; i++) {
			System.out.print(inPorts[i]? 1 : 0);
		}
		System.out.println();
	 	System.out.print("Uscite:   ");
	 	for (i = 0; i < outPortsNum; i++) {
			System.out.print(outPorts[i]? 1 : 0);
		}
	 	System.out.println();
	}
}
