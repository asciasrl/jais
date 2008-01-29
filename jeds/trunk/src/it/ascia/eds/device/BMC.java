/**
 * Copyright (C) 2008 ASCIA S.R.L.
 */
package it.ascia.eds.device;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
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
	private Map inPortsNames;
	/**
	 * I nomi delle porte di uscita.
	 */
	private Vector outPortsNames;
	/**
	 * Binding tra messaggi broadcast e porte di output.
	 * 
	 * Questo e' un'array di Set di Integer, indicizzato per numero di
	 * messaggio broadcast.
	 */
	private Set broadcastBindings[];
	
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
		broadcastBindings = new Set[32];
		for (int i = 0; i < broadcastBindings.length; i++) {
			broadcastBindings[i] = new HashSet();
		}
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
	 * @return the newly created BMC or null if the model is unknown.
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
	public abstract int getFirstInputPortNumber();
	
	/**
	 * Ritorna il numero di uscite.
	 */
	public abstract int getOutPortsNumber();

	
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
	 * @param number il numero della porta (inizia da 0)
	 * @param name il nome da assegnare.
	 */
	public void setInputName(int number, String name) {
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
			retval = null;
		}
		if (retval == null) {
			retval = "Uscita" + number;
			setOutputName(number, retval);
		}
		return retval;
	}
	
	/**
	 * Ritorna il numero di "caselle" disponibili per ciascuna uscita.
	 * 
	 * Una casella serve a registrare l'associazione di un'uscita a un comando
	 * broadcast.
	 * 
	 * Tutti i BMC hanno 4 caselle per uscita, tranne i Dimmer che ne hanno 8.
	 * Questo metodo deve essere quindi sovrascritto da BMCDimmer.
	 */
	public int getCaselleNumber() {
		return 4;
	}
	
	/**
	 * Registra il binding tra uscita e messaggio broadcast.
	 *
	 * @param message numero del messaggio broadcast (1-31).
	 * @param outPortNumber numero della porta che risponde al messaggio.
	 */
	protected void bindOutput(int message, int outPortNumber) {
		broadcastBindings[message].add(new Integer(outPortNumber));
	}
	
	/**
	 * Ritorna le porte di uscita che sono collegate a un messaggio broadcast.
	 * 
	 * @param message il numero del messaggio broadcast (1-31)
	 * @returns un'array di interi: le porte
	 */
	protected int[] getBoundOutputs(int message) {
		Set ports = broadcastBindings[message];
		int retval[] = new int[ports.size()];
		Iterator it = ports.iterator();
		int i = 0;
		while (it.hasNext()) {
			retval[i] = (((Integer)it.next()).intValue());
			i++;
		}
		return retval;
	}
}
