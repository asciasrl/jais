/**
 * Copyright (C) 2008 ASCIA S.R.L.
 */
package it.ascia.eds.device;

import it.ascia.ais.AISException;
import it.ascia.eds.EDSConnector;
import it.ascia.eds.EDSException;
import it.ascia.eds.msg.AcknowledgeMessage;
import it.ascia.eds.msg.ComandoBroadcastMessage;
import it.ascia.eds.msg.ComandoUscitaMessage;
import it.ascia.eds.msg.EDSMessage;
import it.ascia.eds.msg.RichiestaAssociazioneUscitaMessage;
import it.ascia.eds.msg.RichiestaStatoMessage;
import it.ascia.eds.msg.RispostaAssociazioneUscitaMessage;
import it.ascia.eds.msg.RispostaModelloMessage;
import it.ascia.eds.msg.RispostaStatoMessage;
import it.ascia.eds.msg.VariazioneIngressoMessage;

/**
 * Un BMC con ingressi e uscite digitali, simulato o reale.
 * 
 * <p>
 * Modelli: 88, 8, 40, 60, 44.
 * </p>
 * 
 * <p>
 * Reagisce ai messaggi di comando uscita, discovery, variazione ingresso,
 * attuazione broadcast. Se il BMC e' simulato, risponde anche a questi
 * messaggi.
 * </p>
 * 
 * @author arrigo
 */
public class BMCStandardIO extends BMC {
	/**
	 * Numero di uscite digitali.
	 */
	protected int outPortsNum;
	/**
	 * Questo BMC e' fisicamente presente sul transport?
	 * 
	 * <p>
	 * Se questo attributo e' false, allora bisogna simulare l'esistenza di
	 * questo BMC.
	 * </p>
	 */
	private boolean isReal;
	/**
	 * Numero di ingressi digitali.
	 */
	private int inPortsNum;
	/**
	 * Ingressi.
	 */
	private boolean[] inPorts;
	/**
	 * Timestamp di aggiornamento degli ingressi.
	 */
	private long inPortsTimestamps[];
	/**
	 * Uscite.
	 */
	private boolean[] outPorts;
	/**
	 * I valori di outPorts non sono aggiornati.
	 */
	private boolean[] outPortsDirty;
	/**
	 * Timestamp di aggiornamento delle uscite.
	 */
	private long outPortsTimestamps[];

	/**
	 * Costruttore.
	 * 
	 * <p>
	 * Il BMC funzionera' come la controparte virtuale di un BMC reale. Per
	 * renderlo un BMC simulato bisogna chiamare il metodo 
	 * {@link #makeSimulated}.
	 * </p>
	 * 
	 * @param address
	 *            indirizzo del BMC
	 * @param model
	 *            numero del modello
	 */
	public BMCStandardIO(int address, int model, String name) {
		super(address, model, name);
		this.isReal = true; // fino a prova contraria!
		inPortsNum = getInPortsNumber();
		outPortsNum = getOutPortsNumber();
		if (inPortsNum > 0) {
			inPorts = new boolean[inPortsNum];
			inPortsTimestamps = new long[inPortsNum];
			for (int i = 0; i < inPortsNum; i++) {
				inPortsTimestamps[i] = 0;
			}
		}
		if (outPortsNum > 0) {
			outPorts = new boolean[outPortsNum];
			outPortsDirty = new boolean[outPortsNum];
			outPortsTimestamps = new long[outPortsNum];
			for (int i = 0; i < outPortsNum; i++) {
				outPortsDirty[i] = true;
				outPortsTimestamps[i] = 0;
			}
		}
	}

	public void messageReceived(EDSMessage m) {
		long currentTime = System.currentTimeMillis();
		switch (m.getMessageType()) {
		case EDSMessage.MSG_COMANDO_USCITA: {
			ComandoUscitaMessage cmd = (ComandoUscitaMessage) m;
			int uscita = cmd.getOutputPortNumber();
			boolean oldValue = outPorts[uscita];
			// TODO gestire configurazione delle uscite (passo-passo, ecc.)
			outPorts[uscita] = cmd.isActivation();
			if (isReal) {
				// L'attuazione viene richiesta, non sappiamo se sara'
				// effettuata. Quindi non aggiorniamo il timestamp.
				outPortsDirty[uscita] = true;
			} else {
				// Siamo noi che decidiamo: avvisiamo il listener e mandiamo
				// l'ack
				logger.debug("Impostata la porta " + uscita + " a "
						+ cmd.isActivation());
				AcknowledgeMessage ack = new AcknowledgeMessage(cmd);
				connector.sendMessage(ack);
			}
			if (outPorts[uscita] != oldValue) {
				outPortsTimestamps[uscita] = currentTime;
				generateEvent(uscita, true);
			}
		}
			break;
		case EDSMessage.MSG_COMANDO_BROADCAST: {
			// Messaggio broadcast: potrebbe interessare alcune porte.
			ComandoBroadcastMessage bmsg = (ComandoBroadcastMessage) m;
			int ports[] = getBoundOutputs(bmsg.getCommandNumber());
			if (ports.length > 0) {
				for (int i = 0; i < ports.length; i++) {
					if (isReal) {
						// Non sappiamo bene che succede
						outPortsDirty[ports[i]] = true;
					} else {
						// Decidiamo noi che cosa succede
						int portNum = ports[i];
						outPorts[portNum] ^= true;
						outPortsTimestamps[portNum] = currentTime;
						generateEvent(portNum, true);
						logger.debug("La porta " + ports[i]	+ 
								" risponde a un comando broadcast e diventa: " +
								outPorts[portNum]);
					}
				} // cicla sulle porte interessate
			} // if ports.length > 0
		}
			break;
		case EDSMessage.MSG_VARIAZIONE_INGRESSO: {
			// Qualcuno ha premuto un interruttore, e la cosa ci interessa.
			VariazioneIngressoMessage vmsg = (VariazioneIngressoMessage) m;
			int port = vmsg.getOutputNumber();
			if (isReal) {
				// Non sappiamo che succede. Ipotizziamo un toggle e non 
				// aggiorniamo il timestamp.
				outPorts[port] ^= true;
				outPortsDirty[port] = true;
			} else {
				// Decidiamo noi cosa succede
				outPorts[port] ^= true;
				logger.debug("La porta " + port	+ " risponde alla variazione " +
						"di un ingresso e diventa: " + outPorts[port]);
			}
			outPortsTimestamps[port] = currentTime;
			generateEvent(port, true);
		}
		break;
		case EDSMessage.MSG_RICHIESTA_MODELLO: {
			// Ci chiedono chi siamo...
			if (!isReal) {
				// ...dobbiamo rispondere!
				RispostaModelloMessage answer;
				answer = new RispostaModelloMessage(m.getSender(),
						getIntAddress(), model, 1);
				connector.sendMessage(answer);
			}
		}
			break;
		case EDSMessage.MSG_RICHIESTA_ASSOCIAZIONE_BROADCAST: {
			// Ci chiedono se abbiamo uscite associate a comandi broadcast...
			if (!isReal) {
				// ...dobbiamo rispondere!
				RichiestaAssociazioneUscitaMessage question;
				RispostaAssociazioneUscitaMessage answer;
				int messages[], message, casella;
				question = (RichiestaAssociazioneUscitaMessage) m;
				messages = getBoundMessages(question.getUscita());
				// Usiamo la casella come indice dell'array
				casella = question.getCasella();
				if (casella < messages.length) {
					message = messages[casella];
				} else { // Diciamo che non c'e' binding
					message = 0;
				}
				// FIXME: diciamo sempre che sono attivazioni.
				answer = new RispostaAssociazioneUscitaMessage(question, 0,
						true, message);
				connector.sendMessage(answer);
			}
		}
			break;
		case EDSMessage.MSG_RICHIESTA_STATO:
			// Ci chiedono il nostro stato...
			if (!isReal) {
				// ...dobbiamo rispondere!
				RichiestaStatoMessage question = (RichiestaStatoMessage) m;
				RispostaStatoMessage answer;
				answer = new RispostaStatoMessage(question, outPorts, inPorts);
				connector.transport.write(answer.getBytesMessage());
			}
		} // switch (m.getMessageType())
	}

	public void messageSent(EDSMessage m) {
		// Il messaggio inviato ci interessa solo se non siamo stati noi a
		// generarlo, cioe' se il BMC e' reale.
		if (isReal) {
			switch (m.getMessageType()) {
			case EDSMessage.MSG_RISPOSTA_STATO: {
				long currentTime = System.currentTimeMillis();
				RispostaStatoMessage r;
				r = (RispostaStatoMessage) m;
				// Il RispostaStatoMessage da' sempre 8 valori. Dobbiamo
				// prendere solo quelli effettivamente presenti sul BMC
				boolean temp[], mustAlert;
				int i;
				temp = r.getOutputs();
				for (i = 0; i < outPortsNum; i++) {
					mustAlert = (outPorts[i] != temp[i]);
					outPorts[i] = temp[i];
					if (mustAlert) {
						outPortsTimestamps[i] = currentTime;
						generateEvent(i, true);
					}
					outPortsDirty[i] = false;
				}
				temp = r.getInputs();
				for (i = 0; i < inPortsNum; i++) {
					mustAlert = (inPorts[i] != temp[i]);
					inPorts[i] = temp[i];
					if (mustAlert) {
						inPortsTimestamps[i] = currentTime;
						generateEvent(i, false);
					}
				}
			}
				break;
			case EDSMessage.MSG_RISPOSTA_ASSOCIAZIONE_BROADCAST: {
				// Stiamo facendo un discovery delle associazioni.
				RispostaAssociazioneUscitaMessage r = (RispostaAssociazioneUscitaMessage) m;
				if (r.getComandoBroadcast() != 0) {
					bindOutput(r.getComandoBroadcast(), r.getUscita());
				}
			}
				break;
			}
		} // if isReal
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
	 * Manda un messaggio di "variazione ingresso" per impostare il valore di 
	 * un'uscita.
	 * 
	 * <p>Questo metodo deve essere usato quando si vuole simulare la pressione
	 * di un interruttore da parte dell'utente.</p>
	 * 
	 * <p>
	 * Se questo oggetto corrisponde a un BMC "vero", allora manda un messaggio
	 * con mittente il BMCComputer. Altrimenti, l'uscita viene impostata
	 * direttamente.
	 * </p>
	 * 
	 * <p>Il valore dell'uscita viene impostato se la trasmissione ha successo
	 * (o se il BMC e' virtuale).</p>
	 * 
	 * @param port
	 *            numero della porta
	 * @param value
	 *            valore (true: acceso)
	 * @return true se l'oggetto ha risposto (ACK)
	 * 
	 * @see #setOutPort
	 */
	public boolean setOutputVariation(int port, boolean value) {
		boolean retval = false;
		if ((port >= 0) && (port < outPortsNum)) {
			if (isReal) {
				VariazioneIngressoMessage m;
				m = new VariazioneIngressoMessage(getIntAddress(), 
						getBMCComputerAddress(), value, port, 1);
				/* ComandoUscitaMessage m;
				m = new ComandoUscitaMessage(getIntAddress(), 
						connector.getBMCComputerAddress(), port, value); */
				retval = connector.sendMessage(m);
				outPortsDirty[port] = true;
			} else { // The easy way
				retval = true;
			}
			if (retval) {
				boolean oldValue = outPorts[port];
				outPorts[port] = value;
				if (oldValue != value) {
					outPortsTimestamps[port] = System.currentTimeMillis();
					generateEvent(port, true);
				}
			}
		} else {
			logger.error("Numero porta non valido: " + port);
		}
		return retval;
	}

	/**
	 * Manda un messaggio per impostare direttamente il valore di un'uscita.
	 * 
	 * <p>
	 * Se questo oggetto corrisponde a un BMC "vero", allora manda un messaggio
	 * con mittente il BMCComputer. Altrimenti, l'uscita viene impostata
	 * direttamente.
	 * </p>
	 * 
	 * @param port
	 *            numero della porta
	 * @param value
	 *            valore (true: acceso)
	 * @return true se l'oggetto ha risposto (ACK)
	 * 
	 * @see #setOutputVariation
	 */
	public boolean setOutPort(int port, boolean value) {
		boolean retval = false;
		int intValue = (value) ? 1 : 0;
		if ((port >= 0) && (port < outPortsNum)) {
			if (isReal) {
				ComandoUscitaMessage m;
				m = new ComandoUscitaMessage(getIntAddress(), getBMCComputerAddress(), 0, port, 0, intValue);
				retval = connector.sendMessage(m);
			} else { // The easy way
				outPorts[port] = value;
				retval = true;
			}
			if (retval) {
				outPortsTimestamps[port] = System.currentTimeMillis();
				generateEvent(port, true);
			}
		} else {
			logger.error("Numero porta non valido: " + port);
		}
		return retval;
	}

	public String getInfo() {
		return getName() + ": BMC Standard I/O (modello " + model + ") con "
				+ inPortsNum + " ingressi e " + outPortsNum
				+ " uscite digitali";
	}

	/**
	 * Stampa una descrizione dello stato del BMC.
	 */
	public void printStatus() {
		int i;
		System.out.print("Ingressi: ");
		for (i = 0; i < inPortsNum; i++) {
			System.out.print(inPorts[i] ? 1 : 0);
		}
		System.out.println();
		System.out.print("Uscite:   ");
		for (i = 0; i < outPortsNum; i++) {
			System.out.print(outPorts[i] ? 1 : 0);
			if (outPortsDirty[i])
				System.out.print("?");
			else
				System.out.print(" ");
		}
		System.out.println();
		// Stampiamo anche i binding, ordinati per segnale
		for (i = 1; i < 32; i++) {
			int outputs[] = getBoundOutputs(i);
			if (outputs.length != 0) {
				System.out.print("Il messaggio broadcast " + i
						+ " e' legato alle porte: ");
				for (int j = 0; j < outputs.length; j++) {
					System.out.print(outputs[j] + " ");
				}
				System.out.println();
			} // Se ci sono messaggi legati
		}
	}

	/**
	 * Ritorna true se almeno un dato e' contrassegnato come "dirty".
	 */
	public boolean hasDirtyCache() {
		boolean retval = false;
		for (int i = 0; (i < outPortsNum) && !retval; i++) {
			retval = retval || outPortsDirty[i];
		}
		return retval;
	}

	public String getStatus(String port, long timestamp) {
		String retval = "";
		String busName = connector.getName();
		int i;
		String compactName = busName + "." + getAddress();
		updateStatus();
		/*
		if (hasDirtyCache()) {
			updateStatus();
		}
		*/
		for (i = 0; i < inPortsNum; i++) {
			if ((timestamp <= inPortsTimestamps[i]) && 
					(port.equals("*") || port.equals(getInputPortId(i)))) {
				retval += compactName + ":" + getInputPortId(i) + "="
						+ (inPorts[i] ? "ON" : "OFF") + "\n";
			}
		}
		for (i = 0; i < outPortsNum; i++) {
			if ((timestamp <= outPortsTimestamps[i]) && 
					(port.equals("*") || port.equals(getOutputPortId(i)))){
				retval += compactName + ":" + getOutputPortId(i) + "="
						+ (outPorts[i] ? "ON" : "OFF") + "\n";
			}
		}
		return retval;
	}

	public int getFirstInputPortNumber() {
		return 1;
	}

	/**
	 * Ritorna il numero di ingressi digitali, a partire dal modello.
	 */
	public int getInPortsNumber() {
		switch (model) {
		case 88:
			return 8;
		case 8:
			return 0;
		case 40:
			return 4;
		case 60:
			return 6;
		case 44:
			return 4;
		default: // This should not happen(TM)
			logger.error("Errore: modello di BMCStandardIO sconosciuto:"
					+ model);
			return 0;
		}
	}

	/**
	 * Ritorna il numero di uscite digitali a partire dal modello.
	 */
	public int getOutPortsNumber() {
		switch (model) {
		case 88:
			return 8;
		case 8:
			return 8;
		case 40:
			return 0;
		case 60:
			return 0;
		case 44:
			return 4;
		default: // This should not happen(TM)
			logger.error("Errore: modello di BMCStandardIO sconosciuto:"
					+ model);
			return 0;
		}
	}

	/**
	 * Imposta il valore di una porta.
	 */
	public void poke(String port, String value) throws EDSException {
		int portNumber;
		boolean success;
		boolean boolValue;
		if (value.equals("1") || value.toUpperCase().equals("ON")) {
			boolValue = true;
		} else if (value.equals("0") || value.toUpperCase().equals("OFF")) {
			boolValue = false;
		} else {
			throw new EDSException("Valore non valido: " + value);
		}
		portNumber = getOutputNumberFromPortId(port);
		if (portNumber == -1) {
			throw new EDSException("Porta non valida: " + port);
		}
		success = setOutputVariation(portNumber, boolValue);
		if (!success) {
			throw new EDSException("Il device non risponde.");
		}
	}

	/**
	 * Avvisa il DeviceListener che una porta e' cambiata.
	 * 
	 * <p>
	 * Questa funzione deve essere chiamata dopo che il	valore della porta viene
	 * cambiato.
	 * </p>
	 * 
	 * @param port numero della porta
	 * @param isOutput true se si tratta di un'uscita, false se e' un ingresso.
	 */
	private void generateEvent(int port, boolean isOutput) {
		String newValue, portId;
		boolean val;
		if (isOutput) {
			val = outPorts[port];
			portId = getOutputPortId(port);
		} else {
			val = inPorts[port];
			portId = getInputPortId(port);
		}
		if (val) {
			newValue = "on";
		} else {
			newValue = "off";
		}
		super.generateEvent(portId, newValue);
	}

	/**
	 * Cambia la modalita' di funzionamento del BMC in "simulazione".
	 * 
	 * <p>
	 * Dopo la chiamata di questo metodo, il BMC si comportera' come un BMC
	 * simulato, cioe' rispondera' "di propria iniziativa" ai messaggi che
	 * riceve.
	 * </p>
	 * 
	 * @param listener
	 *            il DeviceListener a cui si comunicheranno
	 */
	public void makeSimulated() {
		isReal = false;
		// Togliamo tutti gli eventuali "dirty"
		for (int i = 0; i < outPortsDirty.length; i++) {
			outPortsDirty[i] = false;
		}
	}

	public String peek(String portId) throws AISException {
		// TODO Auto-generated method stub
		return null;
	}

}
