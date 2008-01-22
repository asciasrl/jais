/**
 * Copyright (C) 2008 ASCIA S.R.L.
 */
package it.ascia.eds.device;

import java.util.Vector;

import it.ascia.eds.*;
import it.ascia.eds.msg.Message;
import it.ascia.eds.msg.RispostaModelloMessage;

/**
 * Un BMC.
 * 
 * I BMC devono poter aggiornare il proprio stato. Questo deve avvenire inviando
 * messaggi sul bus che abbiano come mittente il BMCComputer.
 * 
 * Ciascun BMC puo' avere piu' ingressi e piu' uscite. Ciascuna porta ha un
 * nome, che puo' essere specificato oppure viene generato automaticamente.
 * 
 * Anche il BMC ha un nome, che deve essere essere univoco. I costruttori hanno
 * l'obbligo di generare un nome (possibilmente) univoco se l'utente non lo
 * fornisce.
 * 
 * @author arrigo
 */
public abstract class BMC implements Device {
	/**
	 * Il bus a cui il BMC e' collegato
	 */
	protected Bus bus;
	/**
	 * L'indirizzo sul bus
	 */
	protected int address;
	/**
	 * Il modello di questo BMC
	 */
	protected int model;
	/**
	 * Il nome che AUI da' a questo BMC
	 */
	protected String name;
	/**
	 * I nomi delle porte di ingresso.
	 */
	Vector inPortsNames;
	/**
	 * I nomi delle porte di uscita.
	 */
	Vector outPortsNames;
	
	/**
	 * Costruttore. Deve essere usato dalle sottoclassi.
	 * 
	 * @param address l'indirizzo di questo BMC
	 * @param model il modello di questo BMC
	 * @param name il nome di questo BMC (dal file di configurazione)
	 */
	public BMC(int address, int model, Bus bus, String name) {
		this.bus = bus;
		this.address = address;
		this.model = model;
		this.name = name;
		inPortsNames = new Vector();
		outPortsNames = new Vector();
	}
	
	/**
	 * Ritorna l'indirizzo di questo BMC.
	 */
	public int getAddress() {
		return address;
	}
	
	/**
	 * Ritorna il nome di questo BMC.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Factory method for creating BMCs and adding them to the bus.
	 * 
	 * @param bmcAddress the address on the bus.
	 * @param model the model number of the BMC.
	 * @param name the BMC name from the configuration file. Set it to null if 
	 * you want it to be auto-generated.
	 * @param bus the bus the BMC is connected to.
	 * 
	 * @returns the newly created BMC or null if the model is unknown.
	 * 
	 * @throws an exception if the address is already in use by another BMC.
	 */
	public static BMC createBMC(int bmcAddress, int model, String name, Bus bus) 
		throws EDSException {
		BMC bmc;
		switch(model) {
		case 88:
		case 8:
		case 40:
		case 60:
		case 44:
			if (name == null) {
				name = "StandardIO" + bmcAddress;
			}
			bmc = new BMCStandardIO(bmcAddress, model, bus, name);
			break;
		case 41:
		case 61:
		case 81:
			if (name == null) {
				name = "IR" + bmcAddress;
			}
			bmc = new BMCIR(bmcAddress, model, bus, name);
			break;
		case 101:
		case 102:
		case 103:
		case 104:
		case 106:
		case 111:
			if (name == null) {
				name = "Dimmer" + bmcAddress;
			}
			bmc = new BMCDimmer(bmcAddress, model, bus, name);
			break;
		case 131:
			if (name == null) {
				name = "IntIR" + bmcAddress;
			}
			bmc = new BMCIntIR(bmcAddress, model, bus, name);
			break;
		case 152:
		case 154:
		case 156:
		case 158:
			if (name == null) {
				name = "ScenarioManager" + bmcAddress;
			}
			bmc = new BMCScenarioManager(bmcAddress, model, bus, name);
			break;
		case 127:
			if (name == null) {
				name = "ChronoTerm" + bmcAddress;
			}
			bmc = new BMCChronoTerm(bmcAddress, model, bus, name);
			break;
		default:
			System.err.println("Modello di BMC sconosciuto: " + 
					model);
			bmc = null;
		}
		if (bmc != null) {
			bus.addDevice(bmc);
		}
		return bmc;
	}	
		
	
	/** 
	 * Il BMC ha inviato o ricevuto un messaggio.
	 * 
	 * Questo metodo deve leggere il contenuto del messaggio e aggiornare lo 
	 * stato interno.
	 * 
	 * Dovrebbe essere chiamato solo dal bus. Deve essere chiamato sia per i
	 * messaggi inviati, sia per quelli ricevuti.
	 * 
	 * @param m il messaggio ricevuto
	 */
	public abstract void receiveMessage(Message m);
	
	/**
	 * Ritorna una descrizione del BMC.
	 */
	public abstract String getInfo();
	
	/**
	 * Ritorna lo stato del BMC in formato utile per AUI.
	 */
	public abstract String getStatus();
	
	/**
	 * Aggiorna la rappresentazione interna delle porte.
	 * 
	 * Manda un messaggio al BMC mettendo come mittente il bmcComputer. Quando 
	 * arrivera' la risposta, receiveMessage() aggiornera' le informazioni.
	 */
	public abstract void updateStatus();
	
	/**
	 * Stampa una descrizione dello stato del BMC (facoltativa).
	 * 
	 * Questa funzione ha senso solo se implementata dalle sottoclassi.
	 */
	public void printStatus() {
		System.out.println("printStatus() non implementata");
	}
	
	/**
	 * Sets the name of an input port.
	 * 
	 * @param number the port number.
	 * @param name the name to assign.
	 */
	public void setInputName(int number, String name) {
		if (inPortsNames.size() < number + 1) {
			inPortsNames.setSize(number + 1);
		}
		inPortsNames.set(number, name);
	}
	
	/**
	 * Sets the name of an output port.
	 * 
	 * @param number the port number.
	 * @param name the name to assign.
	 */
	public void setOutputName(int number, String name) {
		if (outPortsNames.size() < number + 1) {
			outPortsNames.setSize(number + 1);
		}
		outPortsNames.set(number, name);
	}
	
	/**
	 * Ritorna il nome di una porta di uscita.
	 * 
	 * Se il nome non esiste, viene impostato automaticamente.
	 */
	public String getInputName(int number) {
		String retval;
		try {
			retval = (String) inPortsNames.get(number);
		} catch (ArrayIndexOutOfBoundsException e) {
			retval = "Ingresso" + number;
			setInputName(number, retval);
		}
		return retval;
	}
	
	/**
	 * Ritorna il nome di una porta di ingresso.
	 * 
	 * Se il nome non esiste, viene impostato automaticamente.
	 */
	public String getOutputName(int number) {
		String retval;
		try {
			retval = (String) outPortsNames.get(number);
		} catch (ArrayIndexOutOfBoundsException e) {
			retval = "Uscita" + number;
			setOutputName(number, retval);
		}
		return retval;
	}
}
