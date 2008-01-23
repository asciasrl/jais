/**
 * Copyright (C) 2008 ASCIA S.R.L.
 */
package it.ascia.eds.device;

import it.ascia.eds.Bus;
import it.ascia.eds.msg.ComandoUscitaMessage;
import it.ascia.eds.msg.Message;
import it.ascia.eds.msg.PTPRequest;
import it.ascia.eds.msg.RichiestaStatoMessage;
import it.ascia.eds.msg.RispostaStatoDimmerMessage;

/**
 * Un BMC con 1 o 2 porte di output.
 * 
 * Modelli: 101, 102, 103, 104, 106, 111
 * 
 * TODO: distinguere tra canale a 0% e OFF.
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
	int[] outPorts;
	/**
	 * La conoscenza delle uscite puo' essere errata?
	 */
	boolean dirty;
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
	public BMCDimmer(int address, int model, Bus bus, String name) {
		super(address, model, bus, name);
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
			outPorts = new int[outPortsNum];
		}
		dirty = true;
	}
	
	/* (non-Javadoc)
	 * @see it.ascia.eds.device.BMC#receiveMessage(it.ascia.eds.msg.Message)
	 */
	public void messageReceived(Message m) {
//		System.out.println("Ricevuto un messaggio di tipo " + m.getTipoMessaggio());
		if (ComandoUscitaMessage.class.isInstance(m)) {
			// Qualcuno ha chiesto la modifica, non sappiamo se sara'
			// effettuata.
			dirty = true;			
		}	
	}
	
	public void messageSent(Message m) {
		if (RispostaStatoDimmerMessage.class.isInstance(m)) {
			RispostaStatoDimmerMessage r = (RispostaStatoDimmerMessage)m;
			// Il RispostaStatoMessage da' sempre 8 valori. Dobbiamo
			// prendere solo quelli effettivamente presenti sul BMC
			int temp[];
			int i;
			temp = r.getOutputs();
			for (i = 0; i < outPortsNum; i++) {
				outPorts[i] = temp[i];
			}
			dirty = false;
		}
	}
	
	public String getInfo() {
		String retval;
		retval = getName() + ": Dimmer (modello " + model + ", \"" + modelName +
			"\") con " + outPortsNum + " uscite"; 
		if (power >= 0) retval += " a " + power + " Watt";
		return retval;
	}
	
	public void printStatus() {
		int i;
		System.out.print("Uscite:");
		for (i = 0; i < outPortsNum; i++) {
			System.out.print(" " + outPorts[i]);
			if (dirty) {
				System.out.print("?");
			}
		}
		System.out.println();
	}
	
	public String getStatus() {
		int i;
		String retval = "";
		for (i = 0; i < outPortsNum; i++) {
			retval += name + "." + getOutputName(i) + "=" + outPorts[i] + "\n";
		}
		return retval;
	}
	
	public void updateStatus() {
		PTPRequest m;
		// Il protocollo permette di scegliere più uscite. Qui chiediamo solo le
		// prime due.
		m = new RichiestaStatoMessage(getAddress(), bus.getBMCComputerAddress(),
				3);
		bus.sendPTPRequest(m);
	}
	
	/**
	 * Manda un messaggio per impostare un canale del dimmer.
	 * 
	 * La velocità di accensione/spegnimento e' quella di default.
	 * 
	 * @return true se è arrivato un ACK del messaggio
	 *
	 * @param output il numero dell'uscita (di solito 0 o 1)
	 * @param value il valore da impostare (da 0 a 100)
	 */
	public boolean setOutput(int output, int value) {
		boolean retval = false;
		if ((output >= 0) && (output <= outPortsNum)) {
			if ((value >= 0) && (value <= 100)) {
				ComandoUscitaMessage m;
				m = new ComandoUscitaMessage(getAddress(), 
						bus.getBMCComputerAddress(), 0, output, value, 
						(value > 0)? 1 : 0);
				retval = bus.sendPTPRequest(m);
			} else {
				System.err.println("Valore non valido per canale dimmer: " +
						value);
			}
		} else {
			System.err.println("Porta dimmer non valida: " + output);
		}
		return retval;
	}

	/**
	 * Questo BMC non ha ingressi.
	 */
	protected int getFirstInputPortNumber() {
		return 0;
	}
	
}
