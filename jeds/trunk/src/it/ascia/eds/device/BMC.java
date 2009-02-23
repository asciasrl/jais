/**
 * Copyright (C) 2008 ASCIA S.R.L.
 */
package it.ascia.eds.device;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;

import it.ascia.ais.Controller;
import it.ascia.ais.Device;
import it.ascia.ais.DeviceEvent;
import it.ascia.eds.*;
import it.ascia.eds.msg.EDSMessage;
import it.ascia.eds.msg.PTPRequest;
import it.ascia.eds.msg.RichiestaStatoMessage;

/**
 * Un BMC, vero o simulato.
 * 
 * <p>I BMC devono poter aggiornare il proprio stato. Questo deve avvenire 
 * inviando messaggi sul bus che abbiano come mittente il BMCComputer.</p>
 *
 * <p>Ciascun BMC puo' avere piu' ingressi e piu' uscite. Ciascuna porta ha un
 * nome, che puo' essere specificato oppure viene generato automaticamente.</p>
 * 
 * <p>Anche il BMC ha un nome, che deve essere essere univoco. I costruttori 
 * hanno l'obbligo di generare un nome (possibilmente) univoco se l'utente non 
 * lo fornisce.</p>
 * 
 * @author arrigo
 */
public abstract class BMC extends Device {
	/**
	 * Il modello di questo BMC.
	 */
	protected int model;
	/**
	 * Il bus a cui il dispositivo e' collegato.
	 */
	//protected EDSConnector connector;

	/**
	 * Il nome che AUI da' a questo BMC.
	 */
	protected String name;
	
	/**
	 * Binding tra messaggi broadcast e porte di output.
	 * 
	 * <p>Questo e' un'array di Set di Integer, indicizzato per numero di
	 * messaggio broadcast.</p>
	 */
	private Set broadcastBindingsBySignal[];
	/**
	 * Binding tra porte di output e messaggi broadcast.
	 * 
	 * <p>Questo e' un'array di Set di Integer. Contiene gli stessi valori di
	 * {@link #broadcastBindingsBySignal} ma indicizzati per numero di porta.</p>
	 * 
	 * <p>L'ordine di binding e' importante. La superclasse utilizzata deve
	 * tenerne conto.</p>
	 */
	private Set broadcastBindingsByPort[];

	/**
	 * Il nostro logger.
	 */
	protected Logger logger;
	
	/**
	 * Costruttore.
	 * 
	 * @param address l'indirizzo di questo BMC
	 * @param model il modello di questo BMC
	 * @param name il nome di questo BMC (dal file di configurazione)
	 */
	public BMC(int address, int model, String name) {
		this.address = String.valueOf(address);
		this.model = model;
		this.name = name;
		broadcastBindingsBySignal = new Set[32];
		broadcastBindingsByPort = new Set[getOutPortsNumber()];
		for (int i = 0; i < broadcastBindingsBySignal.length; i++) {
			broadcastBindingsBySignal[i] = new HashSet();
		}
		for (int i = 0; i < getInPortsNumber(); i++) {
			addPort(getInputPortId(i));
		}
		for (int i = 0; i < getOutPortsNumber(); i++) {
			// Usiamo un tipo di Set che mantenga l'ordinamento
			broadcastBindingsByPort[i] = new LinkedHashSet();
			addPort(getOutputPortId(i));
		}
		logger = Logger.getLogger(getClass());
	}
	
    /**
     * Ritorna l'indirizzo del BMCComputer del connettore.
     * 
     * <p>Questo metodo e' utile per i BMC, quando devono richiedere 
     * informazioni sul proprio stato. I messaggi che inviano devono partire 
     * "a nome" del BMCComputer.</p>
     */
    public int getBMCComputerAddress() {
    	return ((EDSConnector)connector).getBMCComputerAddress();
    }
	
	/**
	 * Ritorna l'indirizzo (int) di questo BMC sul bus EDS.
	 */
	public int getIntAddress() {
		return Integer.parseInt(address);
	}
	
	/**
	 * Ritorna il nome di questo BMC.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Factory method for creating BMCs and adding them to the connector.
	 * 
	 * @param bmcAddress the address on the transport.
	 * @param model the model number of the BMC.
	 * @param name the BMC name from the configuration file. Set it to null if 
	 * you want it to be auto-generated.
	 * @param transport the transport the BMC is connected to.
	 * @param isReal vero se questo BMC è fisicamente presente sul transport. Alcuni
	 * BMC possono essere simulati.
	 * 
	 * @return the newly created BMC or null if the model is unknown.
	 * 
	 * @throws an exception if the address is already in use by another BMC.
	 */
	public static BMC createBMC(int bmcAddress, int model, String name, boolean isReal) 
		throws EDSException {
		Logger logger = Logger.getLogger("BMC.createBMC");
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
			bmc = new BMCStandardIO(bmcAddress, model, name);
			break;
		case 41:
		case 61:
		case 81:
			if (name == null) {
				name = "IR" + bmcAddress;
			}
			bmc = new BMCIR(bmcAddress, model, name);
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
			bmc = new BMCDimmer(bmcAddress, model, name);
			break;
		case 131:
			if (name == null) {
				name = "IntIR" + bmcAddress;
			}
			bmc = new BMCIntIR(bmcAddress, model, name);
			break;
		case 152:
		case 154:
		case 156:
		case 158:
			if (name == null) {
				name = "ScenarioManager" + bmcAddress;
			}
			bmc = new BMCScenarioManager(bmcAddress, model, name);
			break;
		case 121:
			if (name == null) {
				name = "TemperatureSensor" + bmcAddress; 
			}
			bmc = new BMCTemperatureSensor(bmcAddress, model, name);
			break;
		case 127:
			if (name == null) {
				name = "ChronoTerm" + bmcAddress;
			}
			bmc = new BMCChronoTerm(bmcAddress, model, name);
			break;
		default:
			logger.error("Modello di BMC sconosciuto: " + 
					model);
			bmc = null;
		}
		return bmc;
	}	
		
	
	/** 
	 * Il transport ha ricevuto un messaggio per questo BMC.
	 * 
	 * <p>Questo metodo deve leggere il contenuto del messaggio e aggiornare lo 
	 * stato interno.</p>
	 * 
	 * <p>Dovrebbe essere chiamato solo dal transport.</p>
	 * 
	 * @param m il messaggio ricevuto.
	 */
	public abstract void messageReceived(EDSMessage m);
	
	/** 
	 * Il BMC (fisico) ha inviato un messaggio sul transport.
	 * 
	 * <p>Questo metodo deve leggere il contenuto del messaggio e aggiornare lo 
	 * stato interno.</p>
	 * 
	 * <p>Dovrebbe essere chiamato solo dal transport.</p>
	 * 
	 * @param m il messaggio inviato.
	 */
	public abstract void messageSent(EDSMessage m);
	
	/**
	 * Ritorna una descrizione del BMC.
	 */
	public abstract String getInfo();
	
	/**
	 * Aggiorna la rappresentazione interna delle porte.
	 * 
	 * <p>Manda un messaggio al BMC mettendo come mittente il bmcComputer. 
	 * Quando arrivera' la risposta, receiveMessage() aggiornera' le 
	 * informazioni.</p>
	 * 
	 * <p>Il metodo di default manda un RichiestaStatoMessage per BMC.</p>
	 */
	public void updateStatus() {
		PTPRequest m;
		m = new RichiestaStatoMessage(getIntAddress(), 
				((EDSConnector)connector).getBMCComputerAddress(), 0);
		connector.sendMessage(m);
	}
	
	/**
	 * Ritorna il numero del primo ingresso.
	 * 
	 * <p>Questo metodo e' necessario perche' quasi tutti i modelli di BMC hanno
	 * gli ingressi numerati a partire da 1. Gli ingressi delle porte a
	 * infrarossi, invece, possono valere anche 0.</p>
	 */
	public abstract int getFirstInputPortNumber();

	
	/**
	 * Ritorna il numero di ingressi.
	 * 
	 * <p>Attenzione: questa funzione viene chiamata dal costruttore di BMC! 
	 * Quindi <i>non</i> deve contare su eventuali elaborazioni fatte dal
	 * costruttore della sottoclasse!</p>
	 */
	public abstract int getInPortsNumber();
	
	/**
	 * Ritorna il numero di uscite.
	 * 
	 * <p>Attenzione: questa funzione viene chiamata dal costruttore di BMC! 
	 * Quindi <i>non</i> deve contare su eventuali elaborazioni fatte dal
	 * costruttore della sottoclasse!</p>
	 */
	public abstract int getOutPortsNumber();

	
	/**
	 * Stampa una descrizione dello stato del BMC (facoltativa).
	 * 
	 * <p>Questa funzione ha senso solo se implementata dalle sottoclassi.</p>
	 * 
	 * <p>NOTA: per le singole porte, il nome da visualizzare deve essere quello
	 * generato da getInputCompactName() e getOutputCompactName().</p>
	 */
	public void printStatus() {
		logger.error("printStatus() non implementata");
	}
	
	/**
	 * Imposta il nome assegnato a una  porta di ingresso.
	 * 
	 * @param number il numero della porta (inizia da 0)
	 * @param name il nome da assegnare.
	 */
	public void setInputName(int number, String name) {
		setPortName(getInputPortId(number), name);
	}
	
	/**
	 * Sets the name of an output port.
	 * 
	 * @param number the port number.
	 * @param name the name to assign.
	 */
	public boolean setOutputName(int number, String portName) {
		return setPortName(getOutputPortId(number), portName);
	}
			
	/**
	 * Genera un nome compatto per una porta di ingresso.
	 */
	protected static String getInputPortId(int number) {
		return "Inp" + (number + 1);
	}

	/**
	 * Ritorna il nome di una porta di ingresso.
	 * 
	 * @param number il numero della porta di ingresso (a partire da 0).
	 */
	public String getInputName(int number) {
		String portId = getInputPortId(number);
		return getPortName(portId);
	}


	/**
	 * Genera un nome compatto per una porta di uscita.
	 */
	protected static String getOutputPortId(int number) {
		return "Out" + (number + 1);
	}
	
	/**
	 * Ritorna il nome di una porta di uscita.
	 *
	 * @return null se la porta non esiste
	 */
	public String getOutputName(int number) {
		String portId = getOutputPortId(number);
		return getPortName(portId);
	}

	/**
	 * Ritorna il numero di una porta di uscita a partire dal nome compatto.
	 * 
	 * @return il numero della porta, oppure -1 se non e' stata trovata.
	 */
	public int getOutputNumberFromPortId(String portId) {
		int retval = -1;
		int max = getOutPortsNumber();
		// TODO Non e' il massimo dell'efficienza, ma funziona.
		for (int i = 0; (i < max) && (retval == -1); i++) {
			if (portId.equals(getOutputPortId(i))) {
				retval = i;
			}
		}
		return retval;
	}

	/**
	 * Ritorna il numero di "caselle" disponibili per ciascuna uscita.
	 * 
	 * <p>Una casella serve a registrare l'associazione di un'uscita a un 
	 * comando broadcast.</p>
	 * 
	 * <p>Tutti i BMC hanno 4 caselle per uscita, tranne i Dimmer che ne hanno 
	 * 8. Questo metodo deve essere quindi sovrascritto da BMCDimmer.</p>
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
		broadcastBindingsBySignal[message].add(new Integer(outPortNumber));
		broadcastBindingsByPort[outPortNumber].add(new Integer(message));
	}
	
	/**
	 * Ritorna le porte di uscita che sono collegate a un messaggio broadcast.
	 * 
	 * @param message il numero del messaggio broadcast (1-31)
	 * @return un'array di interi: le porte
	 */
	protected int[] getBoundOutputs(int message) {
		Set ports = broadcastBindingsBySignal[message];
		int retval[] = new int[ports.size()];
		Iterator it = ports.iterator();
		int i = 0;
		while (it.hasNext()) {
			retval[i] = (((Integer)it.next()).intValue());
			i++;
		}
		return retval;
	}
	
	/**
	 * Ritorna i messaggi broadcast a cui una porta risponde.
	 * 
	 * @param port il numero della porta
	 * @return un'array di interi: i messaggi
	 */
	protected int[] getBoundMessages(int port) {
		Set signals = broadcastBindingsByPort[port];
		int retval[] = new int[signals.size()];
		Iterator it = signals.iterator();
		int i = 0;
		while (it.hasNext()) {
			retval[i] = (((Integer)it.next()).intValue());
			i++;
		}
		return retval;
	}
	
	/**
	 * Verifica se un'uscita e' legata a un messaggio broadcast.
	 *
	 * @param outputPort il numero dell'uscita.
	 * @param message il numero del comando broadcast.
	 * 
	 * @return true se l'uscita risponde al comando.
	 */
	protected boolean outputIsBound(int outputPort, int message) {
		Set ports = broadcastBindingsBySignal[message];
		return ports.contains(new Integer(outputPort));
	}

	/**
	 * Genera un evento di tipo DeviceEvent per il Connector di questo BMC.
	 * 
	 * @param port nome della porta che ha cambiato valore.
	 * @param value nuovo valore assunto dalla porta.
	 */
	protected void generateEvent(String port, String value) {
		DeviceEvent event = new DeviceEvent(this, port, value);
		connector.onDeviceEvent(event);
	}
	
	public String toString() {
		return getInfo();
	}
	
}
