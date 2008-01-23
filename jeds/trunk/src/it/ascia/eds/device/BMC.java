/**
 * Copyright (C) 2008 ASCIA S.R.L.
 */
package it.ascia.eds.device;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import it.ascia.eds.*;
import it.ascia.eds.msg.Message;

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
	 * 
	 * Nel file di configurazione, gli ingressi partono da 1 per quasi
	 * tutti i tipi di dispositivi. Per gli altri, la numerazione degli ingressi
	 * puo' seguire logiche diverse (ad es. dando significato ai singoli bit).
	 * 
	 * Da un punto di vista di occupazione di memoria, un Vector sarebbe
	 * piu' svantaggioso nel caso peggiore.
	 */
	Map inPortsNames;
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
		inPortsNames = new HashMap();
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
	 * Il bus ha ricevuto un messaggio per questo BMC.
	 * 
	 * Questo metodo deve leggere il contenuto del messaggio e aggiornare lo 
	 * stato interno.
	 * 
	 * Dovrebbe essere chiamato solo dal bus.
	 * 
	 * @param m il messaggio ricevuto.
	 */
	public abstract void messageReceived(Message m);
	
	/** 
	 * Il BMC (fisico) ha inviato un messaggio sul bus.
	 * 
	 * Questo metodo deve leggere il contenuto del messaggio e aggiornare lo 
	 * stato interno.
	 * 
	 * Dovrebbe essere chiamato solo dal bus.
	 * 
	 * @param m il messaggio inviato.
	 */
	public abstract void messageSent(Message m);
	
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
	 * Ritorna il numero del primo ingresso.
	 * 
	 * Questo metodo e' necessario perche' quasi tutti i modelli di BMC hanno
	 * gli ingressi numerati a partire da 1. Gli ingressi delle porte a
	 * infrarossi, invece, possono valere anche 0.
	 */
	protected abstract int getFirstInputPortNumber();
	
	/**
	 * Stampa una descrizione dello stato del BMC (facoltativa).
	 * 
	 * Questa funzione ha senso solo se implementata dalle sottoclassi.
	 */
	public void printStatus() {
		System.out.println("printStatus() non implementata");
	}
	
	/**
	 * Imposta il nome assegnato a una  porta di ingresso.
	 * 
	 * @param number il numero della porta; verra' compensato se il file di
	 * configurazione inizia a contare da 1.
	 * @param name il nome da assegnare.
	 */
	public void setInputName(int number, String name) {
		// Compensiamo per i BMC che numerano a partire da 1
		number -= getFirstInputPortNumber();
		inPortsNames.put(new Integer(number), name);
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
	 * 
	 * @param number il numero della porta di ingresso (a partire da 0).
	 */
	public String getInputName(int number) {
		String retval;
		retval = (String) inPortsNames.get(new Integer(number));
		if (retval == null) {
			retval = "Ingresso" + number;
			// setInputName compensa se iniziamo da 1 -- dobbiamo prevenirlo
			setInputName(number + getFirstInputPortNumber(), retval);
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
			retval = null;
		}
		if (retval == null) {
			retval = "Uscita" + number;
			setOutputName(number, retval);
		}
		return retval;
	}
}
