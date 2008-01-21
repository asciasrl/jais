/**
 * Copyright (C) 2008 ASCIA S.R.L.
 */
package it.ascia.eds.device;

import it.ascia.eds.Bus;
import it.ascia.eds.msg.ComandoUscitaMessage;
import it.ascia.eds.msg.Message;
import it.ascia.eds.msg.RichiestaStatoMessage;
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
	 * I valori di outPorts non sono aggiornati.
	 */
	private boolean dirtyOutPorts;
	
	/**
	 * Costruttore
	 * @param address indirizzo del BMC
	 * @param model numero del modello
	 */
	public BMCStandardIO(int address, int model, Bus bus) {
		super(address, model, bus, "StandardIO");
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
		dirtyOutPorts = true;
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
				dirtyOutPorts = false;
				temp = r.getInputs();
				for (i = 0; i < inPortsNum; i++) {
					inPorts[i] = temp[i];
				}
			} // if il sender sono io
		} else if (ComandoUscitaMessage.class.isInstance(m)) {
			if (m.getRecipient() == getAddress()) {
				// Ci chiedono di cambiare le porte in uscita.
				dirtyOutPorts = true;
			}
		}
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
	
	/**
	 * Imposta il valore di un'uscita.
	 * 
	 * Manda un messaggio con mittente il BMCComputer.
	 *
	 * @param port numero della porta
	 * @param value valore (true: acceso)
	 * @returns true se l'oggetto ha risposto (ACK)
	 */
	public boolean setOutPort(int port, boolean value) {
		boolean retval = false;
		int intValue = (value)? 1 : 0;
		if ((port >= 0) && (port < outPortsNum)) {
			ComandoUscitaMessage m;
			m = new ComandoUscitaMessage(getAddress(),
					bus.getBMCComputerAddress(), 0, port, 0, intValue);
			retval = bus.sendPTPMessage(m);
		} else {
			System.err.println("Numero porta non valido: " + port);
		}
		return retval;
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
	 	if (dirtyOutPorts) System.out.print("?");
	 	System.out.println();
	}

	public String getStatus() {
		String retval = "";
		int i;
		for (i = 0; i < inPortsNum; i++) {
			retval += name + "." + i + "=" + (inPorts[i]? "ON" : "OFF") + "\n";
		}
	 	for (i = 0; i < outPortsNum; i++) {
	 		retval += name + "." + i + "=" + (outPorts[i]? "ON" : "OFF") + "\n";
		}
	 	return retval;
	}
}
