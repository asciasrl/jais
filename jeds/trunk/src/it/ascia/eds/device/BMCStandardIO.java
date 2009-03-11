/**
 * Copyright (C) 2008 ASCIA S.R.L.
 */
package it.ascia.eds.device;

import it.ascia.ais.AISException;
import it.ascia.ais.Connector;
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
 * 
 * TODO gestire anche il caching (dirty) delle porte di ingresso
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
	//private boolean[] inPorts;
	/**
	 * Timestamp di aggiornamento degli ingressi.
	 */
	//private long inPortsTimestamps[];
	/**
	 * Uscite.
	 */
	//private boolean[] outPorts;
	/**
	 * I valori di outPorts non sono aggiornati.
	 */
	//private boolean[] outPortsDirty;
	/**
	 * Timestamp di aggiornamento delle uscite.
	 */
	//private long outPortsTimestamps[];

	/**
	 * Costruttore.
	 * 
	 * <p>
	 * Il BMC funzionera' come la controparte virtuale di un BMC reale. Per
	 * renderlo un BMC simulato bisogna chiamare il metodo 
	 * {@link #makeSimulated}.
	 * </p>
	 * @param connector 
	 * 
	 * @param bmcAddress
	 *            indirizzo del BMC
	 * @param model
	 *            numero del modello
	 * @throws AISException 
	 */
	public BMCStandardIO(Connector connector, String bmcAddress, int model, String name) throws AISException {
		super(connector, bmcAddress, model, name);
		this.isReal = true; // fino a prova contraria!
		inPortsNum = getInPortsNumber();
		outPortsNum = getOutPortsNumber();
		for (int i = 0; i < inPortsNum; i++) {
			addPort(getInputPortId(i));
		}
		for (int i = 0; i < outPortsNum; i++) {
			addPort(getOutputPortId(i));
		}
	}

	/**
	 * @throws AISException 
	 * 
	 */
	public void messageReceived(EDSMessage m) throws AISException {
		switch (m.getMessageType()) {
		case EDSMessage.MSG_COMANDO_USCITA:
			ComandoUscitaMessage cmd = (ComandoUscitaMessage) m;
			String portId = getOutputPortId(cmd.getOutputPortNumber());
			Boolean newValue = new Boolean(cmd.isActivation());
			// TODO gestire configurazione delle uscite tapparelle, con timer, ecc.
			if (isReal) {
				// L'attuazione viene richiesta, non sappiamo se sara'
				// effettuata. Quindi non aggiorniamo il timestamp.
				invalidate(portId);
			} else {
				// Siamo noi che decidiamo: avvisiamo il listener e mandiamo
				// l'ack
				logger.debug("Impostata la porta " + portId + " a " + newValue);
				getConnector().sendMessage(new AcknowledgeMessage(cmd));
			}
			break;
		case EDSMessage.MSG_COMANDO_BROADCAST:
			// Messaggio broadcast: potrebbe interessare alcune porte.
			ComandoBroadcastMessage bmsg = (ComandoBroadcastMessage) m;
			int ports[] = getBoundOutputs(bmsg.getCommandNumber());
			if (ports.length > 0) {
				for (int i = 0; i < ports.length; i++) {
					if (isReal) {
						// Non sappiamo bene che succede
						invalidate(getOutputPortId(ports[i]));
					} else {
						// Decidiamo noi che cosa succede
						generateEvent(getOutputPortId(i), new Boolean(bmsg.isActivation()));
					}
				} // cicla sulle porte interessate
			} // if ports.length > 0
			break;
		case EDSMessage.MSG_VARIAZIONE_INGRESSO:
			// Qualcuno ha premuto un interruttore, e la cosa ci interessa.
			VariazioneIngressoMessage vmsg = (VariazioneIngressoMessage) m;
			int port = vmsg.getOutputNumber();
			if (isReal) {
				// Non sappiamo che succede. Ipotizziamo un toggle e non 
				// aggiorniamo il timestamp.
				invalidate(getOutputPortId(vmsg.getOutputNumber()));
			} else {
				// Decidiamo noi cosa succede
				generateEvent(getOutputPortId(port), new Boolean(vmsg.isActivation()));
			}
			// TODO impostare a dirty tutte le porte del mittente
			break;
		case EDSMessage.MSG_RICHIESTA_MODELLO:
			// Ci chiedono chi siamo...
			if (!isReal) {
				// ...dobbiamo rispondere!
				RispostaModelloMessage answer;
				answer = new RispostaModelloMessage(m.getSender(),
						getIntAddress(), model, 1);
				getConnector().sendMessage(answer);
			}
			break;
		case EDSMessage.MSG_RICHIESTA_ASSOCIAZIONE_BROADCAST:
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
				getConnector().sendMessage(answer);
			}
			break;
		case EDSMessage.MSG_RICHIESTA_STATO:
			// Ci chiedono il nostro stato...
			if (!isReal) {
				// ...dobbiamo rispondere!
				RichiestaStatoMessage question = (RichiestaStatoMessage) m;
				// rispondiamo sempre tutti OFF
				RispostaStatoMessage answer = new RispostaStatoMessage(question, new boolean[outPortsNum], new boolean[inPortsNum]);
				getConnector().transport.write(answer.getBytesMessage());
			}
			break;
		} // switch (m.getMessageType())
	}

	/**
	 * Questo metodo gestisce un messaggio mandato da un BMC reale.
	 * Aggiorna la rappresentazione interna del BMC allineandola a quella del reale.
	 * @throws AISException 
	 */
	public void messageSent(EDSMessage m) throws AISException {
		// Il messaggio inviato ci interessa solo se non siamo stati noi a
		// generarlo, cioe' se il BMC e' reale.
		if (isReal) {
			switch (m.getMessageType()) {
			case EDSMessage.MSG_RISPOSTA_STATO:
				updating = true;
				RispostaStatoMessage r;
				r = (RispostaStatoMessage) m;
				// Il RispostaStatoMessage da' sempre 8 valori. Dobbiamo
				// prendere solo quelli effettivamente presenti sul BMC
				boolean temp[];
				int i;
				String portId;
				temp = r.getInputs();
				for (i = 0; i < inPortsNum; i++) {
					portId = getInputPortId(i); 
					setPortValue(portId, new Boolean(temp[i]));
				}
				temp = r.getOutputs();
				for (i = 0; i < outPortsNum; i++) {
					portId = getOutputPortId(i); 
					setPortValue(portId, new Boolean(temp[i]));
				}
				updating = false;
				break;
			case EDSMessage.MSG_RISPOSTA_ASSOCIAZIONE_BROADCAST: {
				// Stiamo facendo un discovery delle associazioni.
				RispostaAssociazioneUscitaMessage ra = (RispostaAssociazioneUscitaMessage) m;
				if (ra.getComandoBroadcast() != 0) {
					bindOutput(ra.getComandoBroadcast(), ra.getUscita());
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
	/*
	public boolean[] getInputs() {
		return inPorts;
	}
	*/

	/**
	 * Ritorna lo stato delle uscite.
	 * 
	 * @return un'array di booleani: true vuol dire acceso.
	 */
	/*
	public boolean[] getOutputs() {
		return outPorts;
	}
	*/
	
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
	/*
	public boolean setOutputVariation(int port, boolean value) {
		boolean retval = false;
		if ((port >= 0) && (port < outPortsNum)) {
			if (isReal) {
				VariazioneIngressoMessage m;
				m = new VariazioneIngressoMessage(getIntAddress(), 
						getBMCComputerAddress(), value, port, 1);
				// ComandoUscitaMessage m;
				//m = new ComandoUscitaMessage(getIntAddress(), 
				//		getConnector().getBMCComputerAddress(), port, value);
				retval = getConnector().sendMessage(m);
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
	*/

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
	 * @throws AISException 
	 * 
	 * @see #setOutputVariation
	 */
	public boolean setOutPort(int port, boolean value) throws AISException {
		boolean retval = false;
		int intValue = (value) ? 1 : 0;
		if ((port >= 0) && (port < outPortsNum)) {
			if (isReal) {
				ComandoUscitaMessage m;
				m = new ComandoUscitaMessage(getIntAddress(), getBMCComputerAddress(), 0, port, 0, intValue);
				retval = getConnector().sendMessage(m);
			} else { // The easy way
				setPortValue(getOutputPortId(port),new Boolean(value));
				retval = true;
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
	 * @throws AISException 
	 */
	public void printStatus() throws AISException {
		int i;
		System.out.print("Ingressi: ");
		Boolean x;
		for (i = 0; i < inPortsNum; i++) {
			x = (Boolean) getPortValue(getInputPortId(i));
			if (x == null) {
				System.out.print("?");
			} else {
				System.out.print(x.booleanValue() ? 1 : 0);
			}
		}
		System.out.println();
		System.out.print("Uscite:   ");
		for (i = 0; i < outPortsNum; i++) {
			x = (Boolean) getPortValue(getOutputPortId(i));
			if (x == null) {
				System.out.print("?");
			} else {
				System.out.print(x.booleanValue() ? 1 : 0);
			}
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
	 * Ritorna lo stato delle porte cambiate dopo il timestamp
	 * TODO spostare come metodo di Device
	 */
	/*
	public String getStatus(String port, long timestamp) throws AISException {
		String retval = "";
		int i;
		String compactName = getFullAddress();
		for (i = 0; i < inPortsNum; i++) {
			String portId = getInputPortId(i);
			if ((timestamp <= getPortValueTimestamp(portId)) && 
					(port.equals("*") || port.equals(portId))) {
				retval += compactName + ":" + portId + "="
						+ getPortValue(portId) + "\n";
			}
		}
		for (i = 0; i < outPortsNum; i++) {
			String portId = getOutputPortId(i);
			if ((timestamp <= getPortValueTimestamp(portId)) && 
					(port.equals("*") || port.equals(portId))){
				retval += compactName + ":" + portId + "="
						+ portId + "\n";
			}
		}
		return retval;
	}
	*/
	
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
	public void writePort(String portId, String value) throws AISException {
		int portNumber;
		boolean success;
		boolean boolValue;
		if (value.equals("1") || value.toUpperCase().equals("ON")) {
			boolValue = true;
		} else if (value.equals("0") || value.toUpperCase().equals("OFF")) {
			boolValue = false;
		} else {
			throw new AISException("Valore non valido: " + value);
		}
		portNumber = getOutputNumberFromPortId(portId);
		if (portNumber == -1) {
			throw new AISException("Porta non valida: " + portId);
		}
		success = setOutPort(portNumber, boolValue);
		if (!success) {
			throw new AISException("Il device non risponde.");
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
	 * @deprecated
	 */
	/*
	private void generateEvent(int port, boolean isOutput) {
		String newValue, portId;
		boolean val;
		if (isOutput) {
			val = outPorts[port];
			portId = getOutputPortId(port);
			if (val) {
				newValue = "Accesa";
			} else {
				newValue = "Spenta";
			}
		} else {
			val = inPorts[port];
			portId = getInputPortId(port);
			if (val) {
				newValue = "Chiuso";
			} else {
				newValue = "Aperto";
			}
		}
		super.generateEvent(portId, newValue);
	}
	*/

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
		/*
		for (int i = 0; i < outPortsDirty.length; i++) {
			outPortsDirty[i] = false;
		}
		*/
	}

	public long updatePort(String portId) {
		return updateStatus();
	}

	public void writePort(String portId, Object newValue) throws AISException {
		try {
			writePort(portId,(String)newValue);
		} catch (AISException e) {
			throw(new AISException(e.getMessage()));
		}		
	}

}
