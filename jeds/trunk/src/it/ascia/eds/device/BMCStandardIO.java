/**
 * Copyright (C) 2008 ASCIA S.R.L.
 */
package it.ascia.eds.device;

import java.util.Vector;

import it.ascia.eds.Bus;
import it.ascia.eds.msg.Message;
import it.ascia.eds.msg.RichiestaStatoMessage;

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
	Vector inPorts;
	/**
	 * Uscite
	 */
	Vector outPorts;
	
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
			inPorts = new Vector(inPortsNum);
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
		return "BMC Standard I/O (modello " + model + ") con " + inPortsNum + 
			" porte di input e " + outPortsNum + " porte di output";
	}
	
	/**
	 * Aggiorna la rappresentazione interna delle porte.
	 * 
	 * Manda un messaggio al BMC mettendo come mittente il bmcComputer.
	 */
	public void updateStatus() {
		Message m;
		m = new RichiestaStatoMessage(getAddress(), bus.getBMCComputerAddress(),
				0);
		bus.sendPTPMessage(m);
	}
}
