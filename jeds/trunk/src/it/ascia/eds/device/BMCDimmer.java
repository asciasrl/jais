/**
 * Copyright (C) 2008 ASCIA S.R.L.
 */
package it.ascia.eds.device;

import it.ascia.eds.Bus;
import it.ascia.eds.msg.ComandoBroadcastMessage;
import it.ascia.eds.msg.ComandoUscitaDimmerMessage;
import it.ascia.eds.msg.ComandoUscitaMessage;
import it.ascia.eds.msg.Message;
import it.ascia.eds.msg.PTPRequest;
import it.ascia.eds.msg.RichiestaStatoMessage;
import it.ascia.eds.msg.RispostaAssociazioneUscitaMessage;
import it.ascia.eds.msg.RispostaStatoDimmerMessage;
import it.ascia.eds.msg.VariazioneIngressoMessage;

/**
 * Un BMC dimmer con 1 o 2 porte di output.
 * 
 * Modelli: 101, 102, 103, 104, 106, 1110
 * 
 * Tutti metodi di set assumono che un canale a 0% sia spento.
 * 
 * Questo BMC risponde ai comandi broadcast, ai comandi di impostazione uscita
 * per dimmer e non.
 * 
 * @author arrigo
 */
public class BMCDimmer extends BMC {

	/**
	 * Numero di canali in uscita
	 */
	private int outPortsNum;
	/**
	 * Uscite. Possono assumere un valore 0-100 oppure -1 (OFF).
	 */
	int[] outPorts;
	/**
	 * La conoscenza delle uscite puo' essere errata?
	 * 
	 * Questa array ha un elemento per ogni porta.
	 */
	boolean dirty[];
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
			dirty = new boolean[outPortsNum];
		}
		for (int i = 0; i < outPortsNum; i++) {
			dirty[i] = true;
		}
	}
	
	/* (non-Javadoc)
	 * @see it.ascia.eds.device.BMC#receiveMessage(it.ascia.eds.msg.Message)
	 */
	public void messageReceived(Message m) {
		int uscita, valore;
		ComandoUscitaDimmerMessage cmdDimmer;
		ComandoUscitaMessage cmd;
		ComandoBroadcastMessage bmsg;
		VariazioneIngressoMessage vmsg;
		int[] ports;
//		System.out.println("Ricevuto un messaggio di tipo " + m.getTipoMessaggio());
		switch (m.getMessageType()) {
		case Message.MSG_COMANDO_USCITA_DIMMER:
			// L'attuazione viene richiesta, non sappiamo se sara' 
			// effettuata
			cmdDimmer = (ComandoUscitaDimmerMessage) m;
			uscita = cmdDimmer.getOutputPortNumber();
			valore = cmdDimmer.getValue();
			dirty[uscita] = true;
			outPorts[uscita] = valore;
			break;
		case Message.MSG_COMANDO_USCITA:
			// L'attuazione viene richiesta, non sappiamo se sara' 
			// effettuata.
			cmd = (ComandoUscitaMessage) m;
			uscita = cmd.getOutputPortNumber();
			valore = cmd.getPercentage();
			if (!cmd.isActivation()) {
				valore = 0;
			}
			dirty[uscita] = true;
			outPorts[uscita] = valore;
			break;
		case Message.MSG_COMANDO_BROADCAST:
			// Messaggio broadcast: potrebbe interessare alcune porte.
			bmsg = (ComandoBroadcastMessage) m;
			ports = getBoundOutputs(bmsg.getCommandNumber());
			if (ports.length > 0) {
				for (int i = 0; i < ports.length; i++) {
					dirty[ports[i]] = true;
				}
			}
			break;
		case Message.MSG_VARIAZIONE_INGRESSO:
			// Qualcuno ha premuto un interruttore, e la cosa ci interessa.
			vmsg = (VariazioneIngressoMessage) m;
			dirty[vmsg.getOutputNumber()] = true;
			break;
		}
	}
	
	public void messageSent(Message m) {
		RispostaStatoDimmerMessage r;
		RispostaAssociazioneUscitaMessage ra;
		switch(m.getMessageType()) {
		case Message.MSG_RISPOSTA_STATO_DIMMER:
			r = (RispostaStatoDimmerMessage)m;
			// Il RispostaStatoMessage da' sempre 8 valori. Dobbiamo
			// prendere solo quelli effettivamente presenti sul BMC
			int temp[];
			int i;
			temp = r.getOutputs();
			for (i = 0; i < outPortsNum; i++) {
				outPorts[i] = temp[i];
				dirty[i] = false;
			}
			break;
		case Message.MSG_RISPOSTA_ASSOCIAZIONE_BROADCAST: 
			ra = (RispostaAssociazioneUscitaMessage) m;
			// Stiamo facendo un discovery delle associazioni.
			if (ra.getComandoBroadcast() != 0) {
				System.out.println("L'uscita " + ra.getUscita() + " " +
					"e' legata al comando broadcast " + 
					ra.getComandoBroadcast());
				bindOutput(ra.getComandoBroadcast(), ra.getUscita());					
			}
			break;
		} // switch(tipo del messaggio)
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
			if (dirty[i]) {
				System.out.print("?");
			}
		}
		System.out.println();
		// Stampiamo anche i binding, ordinati per segnale
	 	for (i = 1; i < 32; i++) {
	 		int outputs[] = getBoundOutputs(i);
	 		if (outputs.length != 0) {
	 			System.out.print("Il messaggio broadcast " + i +
	 					" e' legato alle porte: ");
	 			for (int j = 0; j < outputs.length; j++) {
	 				System.out.print(outputs[j] + " ");
	 			}
	 			System.out.println();
	 		} // Se ci sono messaggi legati
	 	}
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
		bus.sendMessage(m);
	}
	
	/**
	 * Manda un messaggio per impostare un canale del dimmer.
	 * 
	 * La velocità di accensione/spegnimento e' quella di default.
	 * 
	 * @return true se è arrivato un ACK del messaggio
	 *
	 * @param output il numero dell'uscita (di solito 0 o 1)
	 * @param value il valore da impostare (da 0 a 100, dove 0 e' OFF)
	 */
	public boolean setOutput(int output, int value) {
		boolean retval = false;
		if ((output >= 0) && (output <= outPortsNum)) {
			if ((value >= 0) && (value <= 100)) {
				ComandoUscitaMessage m;
				m = new ComandoUscitaMessage(getAddress(), 
						bus.getBMCComputerAddress(), 0, output, value, 
						(value > 0)? 1 : 0);
				retval = bus.sendMessage(m);
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
	 * Imposta istantaneamente il valore di un canale.
	 * 
	 * Rispetto a setOutput(), questo metodo invia un messaggio che non richiede
	 * risposta, quindi e' piu' rapido.
	 * 
	 * @param output il numero dell'uscita (di solito 0 o 1)
	 * @param value il valore da impostare (da 0 a 100, dove 0 e' OFF)
	 */
	public void setOutputRealTime(int output, int value) {
		if ((output >= 0) && (output <= outPortsNum)) {
			if ((value >= 0) && (value <= 100)) {
				ComandoUscitaDimmerMessage m;
				m = new ComandoUscitaDimmerMessage(getAddress(), 
						bus.getBMCComputerAddress(), output, value);
				bus.sendMessage(m);
			} else {
				System.err.println("Valore non valido per canale dimmer: " +
						value);
			}
		} else {
			System.err.println("Porta dimmer non valida: " + output);
		}
	}
	
	/**
	 * Questo BMC non ha ingressi.
	 */
	public int getFirstInputPortNumber() {
		return 0;
	}
	
	/**
	 * Tutti i BMC hanno 4 caselle per uscita, tranne i Dimmer che ne hanno 8.
	 */
	public int getCaselleNumber() {
		return 8;
	}

	public int getOutPortsNumber() {
		return outPortsNum;
	}
}
