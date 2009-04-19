/**
 * Copyright (C) 2008 ASCIA S.R.L.
 */
package it.ascia.eds.device;

import it.ascia.ais.AISException;
import it.ascia.ais.Connector;
import it.ascia.ais.DeviceBlindPort;
import it.ascia.ais.DevicePort;
import it.ascia.ais.DigitalInputPort;
import it.ascia.ais.DigitalOutputPort;
import it.ascia.eds.msg.AcknowledgeMessage;
import it.ascia.eds.msg.ComandoBroadcastMessage;
import it.ascia.eds.msg.ComandoUscitaMessage;
import it.ascia.eds.msg.EDSMessage;
import it.ascia.eds.msg.RichiestaAssociazioneUscitaMessage;
import it.ascia.eds.msg.RichiestaStatoMessage;
import it.ascia.eds.msg.RichiestaUscitaMessage;
import it.ascia.eds.msg.RispostaAssociazioneUscitaMessage;
import it.ascia.eds.msg.RispostaModelloMessage;
import it.ascia.eds.msg.RispostaStatoMessage;
import it.ascia.eds.msg.RispostaUscitaMessage;
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
 */
public class BMCStandardIO extends BMC {
	
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
	}

	public void addPort(String portId, String portName) {
		if (portId.startsWith("Inp")) {
			ports.put(portId, new DigitalInputPort(this, portId, portName));
		} else if (portId.startsWith("Out")) {
			ports.put(portId, new DigitalOutputPort(this, portId, portName));		
		} else {
			super.addPort(portId, portName);
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
						setPortValue(getOutputPortId(i), new Boolean(bmsg.isActivation()));
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
				setPortValue(getOutputPortId(port), new Boolean(vmsg.isActivation()));
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
				RispostaStatoMessage answer = new RispostaStatoMessage(question, new boolean[getOutPortsNumber()], new boolean[getInPortsNumber()]);
				getConnector().sendMessage(answer);
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
				for (i = 0; i < getInPortsNumber(); i++) {
					portId = getInputPortId(i); 
					setPortValue(portId, new Boolean(temp[i]));
				}
				temp = r.getOutputs();
				for (i = 0; i < getOutPortsNumber(); i++) {
					portId = getOutputPortId(i); 
					setPortValue(portId, new Boolean(temp[i]));
				}
				updating = false;
				break;
			case EDSMessage.MSG_RISPOSTA_ASSOCIAZIONE_BROADCAST:
				// Stiamo facendo un discovery delle associazioni.
				RispostaAssociazioneUscitaMessage mrisp = (RispostaAssociazioneUscitaMessage) m;
				int gruppo = mrisp.getComandoBroadcast();
				if (gruppo > 0) {
					int outPortNumber = mrisp.getUscita();
					int casella = mrisp.getCasellaBMC();
					// alcuni BMC hanno un Bug per cui non mettono nella risposta il numero di uscita/casella
					RichiestaAssociazioneUscitaMessage mrich = (RichiestaAssociazioneUscitaMessage) mrisp.getRequest();
					if (mrich != null) {
						outPortNumber = mrich.getUscita();
						casella = mrich.getCasella();
					}
					logger.info("Associazione uscita: "+getOutputPortId(outPortNumber)+" casella:"+casella+" al gruppo:"+gruppo);
					bindOutput(gruppo, outPortNumber);
				}
				break;
			case EDSMessage.MSG_RISPOSTA_USCITA:
				// Stiamo facendo un discovery delle configurazioni delle uscite.
				RispostaUscitaMessage ru = (RispostaUscitaMessage) m;
				long t = ru.getMillisecTimer();
				if (t > 0) {
					RichiestaUscitaMessage req = (RichiestaUscitaMessage) ru.getRequest();
					if (req != null) {
						int uscita = req.getUscita();
						DevicePort p = getPort(getOutputPortId(uscita));
						if (p.getCacheRetention() > t) {
							p.setCacheRetention(t);
						}
						logger.info("Uscita "+uscita+" timer di "+t+"mS");
					}
				}
				/**
				 * Creazione uscite virtuali per gestione tapparelle
				 */
				if (ru.getTipoUscita() == 14) {
					RichiestaUscitaMessage req = (RichiestaUscitaMessage) ru.getRequest();
					if (req != null) {
						int uscita = req.getUscita();
						if ((uscita % 2) == 0) {
							String blindPortId = "Blind"+(uscita / 2 + 1);
							if (! havePort(blindPortId)) {
								String openPortId = "Out" + (uscita + 1);
								String closePortId = "Out" + (uscita + 2);
								DeviceBlindPort blindPort = new DeviceBlindPort(this,blindPortId,closePortId,openPortId);
								addPort(blindPort);							
								logger.info("Aggiunta porta virtuale "+blindPortId+": open="+openPortId+" close="+closePortId);
							}
						}
					}
				}
				break;
			case EDSMessage.MSG_ACKNOWLEDGE:
				// TODO da gestire ?
				break;
			default:
				logger.warn("Messaggio non gestito: "+m);
			}
		} // if isReal
	}

	
	public String getInfo() {
		return getName() + ": BMC Standard I/O (modello " + model + ") con "
				+ getInPortsNumber() + " ingressi e " + getOutPortsNumber()
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
		for (i = 0; i < getInPortsNumber(); i++) {
			x = (Boolean) getPortValue(getInputPortId(i));
			if (x == null) {
				System.out.print("?");
			} else {
				System.out.print(x.booleanValue() ? 1 : 0);
			}
		}
		System.out.println();
		System.out.print("Uscite:   ");
		for (i = 0; i < getOutPortsNumber(); i++) {
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
	}

	public long updatePort(String portId) throws AISException {
		if (portId.startsWith("Blind")) {
			getPortValue(portId);
			return 0;
		} else {
			return updateStatus();
		}
	}

	/**
	 * @see BMC.sendPortValue
	 * @param newValue Boolean
	 */
	public boolean sendPortValue(String portId, Object newValue) throws AISException {
		DevicePort p = getPort(portId);
		if (isReal) {
			int intValue = 0;
			if (Boolean.class.isInstance(newValue)) {
				intValue = ((Boolean)newValue).booleanValue() ? 1 : 0;
			} else {
				throw new AISException(getFullAddress() + " tipo valore non valido: " + newValue.getClass().getCanonicalName());
			}
			int portNumber = getOutputNumberFromPortId(portId);
			if (portNumber == -1) {
				throw new AISException("Porta non valida: " + portId);
			}
			ComandoUscitaMessage m = new ComandoUscitaMessage(getIntAddress(), getBMCComputerAddress(), 0, portNumber, 0, intValue);
			if (getConnector().sendMessage(m)) {
				p.invalidate(); // FIXME non necessario, lo fa gia' DevicePort.writeValue()
				return true;
			}
		} else {
			p.setValue(newValue);
			return true;
		}
		return false;
	}

}
