/**
 * Copyright (C) 2008 ASCIA S.R.L.
 */
package it.ascia.eds.device;

import it.ascia.eds.Bus;
import it.ascia.eds.EDSException;
import it.ascia.eds.msg.ComandoBroadcastMessage;
import it.ascia.eds.msg.ComandoUscitaMessage;
import it.ascia.eds.msg.Message;
import it.ascia.eds.msg.PTPRequest;
import it.ascia.eds.msg.RichiestaStatoMessage;
import it.ascia.eds.msg.RispostaAssociazioneUscitaMessage;
import it.ascia.eds.msg.RispostaStatoMessage;
import it.ascia.eds.msg.VariazioneIngressoMessage;

/**
 * Un BMC con porte di input e output.
 * 
 * Modelli: 88, 8, 40, 60, 44.
 * 
 * Risponde ai messaggi di comando uscita, discovery, variazione ingresso,
 * attuazione broadcast.
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
	private boolean[] inPorts;
	/**
	 * Uscite
	 */
	private boolean[] outPorts;
	/**
	 * I valori di outPorts non sono aggiornati.
	 */
	private boolean[] dirty;
	
	/**
	 * Costruttore
	 * @param address indirizzo del BMC
	 * @param model numero del modello
	 */
	public BMCStandardIO(int address, int model, Bus bus, String name) {
		super(address, model, bus, name);
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
			dirty = new boolean[outPortsNum];
		}
	}
	
	public void messageReceived(Message m) {
		switch (m.getMessageType()) {
		case Message.MSG_COMANDO_USCITA: {
			// L'attuazione viene richiesta, non sappiamo se sara' 
			// effettuata.
			ComandoUscitaMessage cmd = (ComandoUscitaMessage) m;
			int uscita = cmd.getOutputPortNumber();
			outPorts[uscita] = cmd.isActivation();
			dirty[uscita] = true;
		}
		break;
		case Message.MSG_COMANDO_BROADCAST: {
			// Messaggio broadcast: potrebbe interessare alcune porte.
			ComandoBroadcastMessage bmsg = (ComandoBroadcastMessage) m;
			int ports[] = getBoundOutputs(bmsg.getCommandNumber());
			if (ports.length > 0) {
				for (int i = 0; i < ports.length; i++) {
					dirty[ports[i]] = true;
				}
			}
		}
		break;
		case Message.MSG_VARIAZIONE_INGRESSO: {
			// Qualcuno ha premuto un interruttore, e la cosa ci interessa.
			VariazioneIngressoMessage vmsg = (VariazioneIngressoMessage) m;
			dirty[vmsg.getOutputNumber()] = true;
		}
		break;
		} // switch (m.getMessageType())
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
				dirty[i] = false;
			}
			temp = r.getInputs();
			for (i = 0; i < inPortsNum; i++) {
				inPorts[i] = temp[i];
			}
		}
		break;
		case Message.MSG_RISPOSTA_ASSOCIAZIONE_BROADCAST: {
			// Stiamo facendo un discovery delle associazioni.
			RispostaAssociazioneUscitaMessage r = 
				(RispostaAssociazioneUscitaMessage) m;
			if (r.getComandoBroadcast() != 0) {
				bindOutput(r.getComandoBroadcast(), r.getUscita());
			}
		} 
		break;
		}
	}
	
	/**
	 * Ritorna lo stato degli ingressi.
	 * 
	 * @return un'array di booleani: true vuol dire acceso.
	 */
	public boolean[] getInputs() {
		return inPorts;
	}
	
	/**
	 * Ritorna lo stato delle uscite.
	 * 
	 * @return un'array di booleani: true vuol dire acceso.
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
	 * @return true se l'oggetto ha risposto (ACK)
	 */
	public boolean setOutPort(int port, boolean value) {
		boolean retval = false;
		int intValue = (value)? 1 : 0;
		if ((port >= 0) && (port < outPortsNum)) {
			ComandoUscitaMessage m;
			m = new ComandoUscitaMessage(getAddress(),
					bus.getBMCComputerAddress(), 0, port, 0, intValue);
			retval = bus.sendMessage(m);
		} else {
			System.err.println("Numero porta non valido: " + port);
		}
		return retval;
	}
	
	public String getInfo() {
		return getName() + ": BMC Standard I/O (modello " + model + ") con " + 
			inPortsNum + " porte di input e " + outPortsNum + " porte di " +
			"output";
	}
	
	/**
	 * Aggiorna la rappresentazione interna delle porte.
	 * 
	 * Manda un messaggio al BMC mettendo come mittente il bmcComputer. Quando 
	 * arrivera' la risposta, receiveMessage() aggiornera' le informazioni.
	 */
	public void updateStatus() {
		PTPRequest m;
		m = new RichiestaStatoMessage(getAddress(), bus.getBMCComputerAddress(),
				0);
		bus.sendMessage(m);
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
			if (dirty[i]) System.out.print("?");
			else System.out.print(" ");
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

	public String getStatus(String port, String busName) {
		String retval = "";
		int i;
		String compactName = busName + "." + getAddress();
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
		return outPorts.length;
	}

	public void setPort(String port, String value) throws EDSException {
		int portNumber;
		boolean boolValue;
		if (value.equals("1") || value.toUpperCase().equals("ON")) {
			boolValue = true;
		} else if (value.equals("0") || value.toUpperCase().equals("OFF")) {
			boolValue = false;
		} else {
			throw new EDSException("Valore non valido: " + value);
		}
		portNumber = getOutputNumberFromCompactName(port);
		if (portNumber == -1) {
			throw new EDSException("Porta non valida: " + port);
		}
		setOutPort(portNumber, boolValue);
	}
}
