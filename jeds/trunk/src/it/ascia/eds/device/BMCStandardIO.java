/**
 * Copyright (C) 2008 ASCIA S.R.L.
 */
package it.ascia.eds.device;

import it.ascia.ais.AISException;
import it.ascia.ais.DevicePort;
import it.ascia.ais.port.BlindPort;
import it.ascia.eds.EDSConnector;
import it.ascia.eds.msg.ComandoBroadcastMessage;
import it.ascia.eds.msg.ComandoUscitaMessage;
import it.ascia.eds.msg.EDSMessage;
import it.ascia.eds.msg.PTPRequest;
import it.ascia.eds.msg.RichiestaAssociazioneUscitaMessage;
import it.ascia.eds.msg.RichiestaStatoMessage;
import it.ascia.eds.msg.RichiestaUscitaMessage;
import it.ascia.eds.msg.RispostaAssociazioneUscitaMessage;
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
	 * @param bmcAddress
	 *            indirizzo del BMC
	 * @param model
	 *            numero del modello
	 * @throws AISException 
	 */
	public BMCStandardIO(String bmcAddress, int model, int version, String name) throws AISException {
		super(bmcAddress, model, version, name);
	}

	/*
	public void addPort(String portId, String portName) {
		if (portId.startsWith("Inp")) {
			addPort(new DigitalInputPort(this, portId, portName));
		} else if (portId.startsWith("Out")) {
			addPort(new DigitalOutputPort(this, portId, portName));		
		} else {
			throw(new AISException("Id porta scorretto: "+portId));
		}
	}
	*/

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
			// L'attuazione viene richiesta, non sappiamo se sara'
			// effettuata. Quindi non aggiorniamo il timestamp.
			invalidate(portId);
			break;
		case EDSMessage.MSG_COMANDO_BROADCAST:
			// Messaggio broadcast: potrebbe interessare alcune porte.
			ComandoBroadcastMessage bmsg = (ComandoBroadcastMessage) m;
			int ports[] = getBoundOutputs(bmsg.getCommandNumber());
			if (ports.length > 0) {
				for (int i = 0; i < ports.length; i++) {
					invalidate(getOutputPortId(ports[i]));
				} // cicla sulle porte interessate
			} // if ports.length > 0
			break;
		case EDSMessage.MSG_VARIAZIONE_INGRESSO:
			// Qualcuno ha premuto un interruttore, e la cosa ci interessa.
			VariazioneIngressoMessage vmsg = (VariazioneIngressoMessage) m;
			int port = vmsg.getOutputNumber();
			// Non sappiamo che succede. Ipotizziamo un toggle e non 
			// aggiorniamo il timestamp.
			invalidate(getOutputPortId(port));
			break;
		default:
			super.messageReceived(m);			
		} // switch (m.getMessageType())
	}

	/**
	 * Questo metodo gestisce un messaggio mandato da un BMC reale.
	 * Aggiorna la rappresentazione interna del BMC allineandola a quella del reale.
	 * @throws AISException 
	 */
	public void messageSent(EDSMessage m) throws AISException {
		switch (m.getMessageType()) {
			case EDSMessage.MSG_RISPOSTA_STATO:
				// Messaggio gestito da updateStatus()
				break;
			case EDSMessage.MSG_RISPOSTA_ASSOCIAZIONE_BROADCAST:
				// Stiamo facendo un discovery delle associazioni.
				RispostaAssociazioneUscitaMessage mrisp = (RispostaAssociazioneUscitaMessage) m;
				int gruppo = mrisp.getComandoBroadcast();
				if (gruppo > 0) {
					int outPortNumber = mrisp.getUscita();
					// alcuni BMC hanno un Bug per cui non mettono nella risposta il numero di uscita/casella
					RichiestaAssociazioneUscitaMessage mrich = (RichiestaAssociazioneUscitaMessage) mrisp.getRequest();
					if (mrich != null) {
						outPortNumber = mrich.getUscita();
					}
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
						setOutputTimer(uscita,t);
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
							addBlindPort(uscita);
						}
					}
				}
				break;
			case EDSMessage.MSG_VARIAZIONE_INGRESSO:
				if (getVersion() >= 74) {
					getPort(getInputPortId(((VariazioneIngressoMessage)m).getInputNumber())).setValue(((VariazioneIngressoMessage)m).isClose(),0);
				} else {
					for (int i = 0; i < getDigitalInputPortsNumber(); i++) {
						getPort(getInputPortId(i)).invalidate();
					}					
				}
				break;
			case EDSMessage.MSG_ACKNOWLEDGE:
				// messaggi ignorati silentemente
				break;
			default:
				super.messageSent(m);
		}
	}

	
	/**
	 * Aggiunge al Device una porta di tipo Blind<br/>
	 * Normalmente la prima uscita della coppia e' quella che comanda l'apertura.
	 * 
	 * <p>Esempio 1:</p>
	 * <ul>
	 * <li>uscita = 4 (la quinta del dispositivo, terza coppia)</li>
	 * <li>identificatore nuova porta: Blind3</li>  
	 * <li>porta di apertura: Out5</li>
	 * <li>porta di chiusura: Out6</li>
	 * </ul>
	 * 
	 * <p>Esempio 2:</p>
	 * <ul>
	 * <li>uscita = 5 (la sesta del dispositivo, terza coppia)</li>
	 * <li>identificatore nuova porta: Blind3</li>  
	 * <li>porta di apertura: Out6</li>
	 * <li>porta di chiusura: Out5</li>
	 * </ul>
	 * @param uscita Numero della uscita che effettua la apertura 
	 */
	public void addBlindPort(int uscita) {
		int shift = (uscita % 2);
		String blindPortId = "Blind"+((uscita - shift) / 2 + 1);
		if (! havePort(blindPortId)) {
			String openPortId = "Out" + (uscita + 1);
			String closePortId = "Out" + (uscita - 2 * shift + 2);
			addPort(new BlindPort(blindPortId,getPort(closePortId),getPort(openPortId)));							
			logger.info("Aggiunta porta virtuale "+blindPortId+": open="+openPortId+" close="+closePortId);
		}
	}

	public String getInfo() {
		return getName() + ": BMC Standard I/O (modello " + model + " revisione " + getVersion() + ") con "
				+ getDigitalInputPortsNumber() + " ingressi e " + getDigitalOutputPortsNumber()
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
		for (i = 0; i < getDigitalInputPortsNumber(); i++) {
			x = (Boolean) getPortValue(getInputPortId(i));
			if (x == null) {
				System.out.print("?");
			} else {
				System.out.print(x.booleanValue() ? 1 : 0);
			}
		}
		System.out.println();
		System.out.print("Uscite:   ");
		for (i = 0; i < getDigitalOutputPortsNumber(); i++) {
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
	public int getDigitalInputPortsNumber() {
		switch (model) {
		case 2:
		case 4:
		case 6:
		case 8:
			return 0;
		case 20:
		case 22:
			return 2;
		case 40:
			return 4;
		case 60:
			return 6;
		case 80:
			return 8;
		case 44:
			return 4;
		case 88:
		case 98:
			return 8;
		default: // This should not happen(TM)
			logger.error("Errore: modello di BMCStandardIO sconosciuto:"
					+ model);
			return 0;
		}
	}

	/**
	 * Ritorna il numero di uscite digitali a partire dal modello.
	 */
	public int getDigitalOutputPortsNumber() {
		switch (model) {
		case 2:
		case 22:
			return 2;
		case 4:
			return 4;
		case 6:
			return 6;
		case 8:
			return 8;
		case 20:
		case 21:
		case 40:
		case 60:
		case 80:
			return 0;
		case 44:
			return 4;
		case 88:
		case 98:
			return 8;
		default: // This should not happen(TM)
			logger.error("Errore: modello di BMCStandardIO sconosciuto:"
					+ model);
			return 0;
		}
	}


	public boolean updatePort(String portId) throws AISException {
		if (portId.startsWith("Blind")) {
			getPortValue(portId);
			return true;
		} else {
			return updateStatus();
		}
	}
	
	protected boolean updateStatus() {
		EDSConnector connector = (EDSConnector) getConnector();
		PTPRequest m = new RichiestaStatoMessage(getIntAddress(), 
				connector.getMyAddress(), 0);
		if (connector.sendMessage(m)) {			
			RispostaStatoMessage r = (RispostaStatoMessage) m.getResponse();
			// Il RispostaStatoMessage da' sempre 8 valori. Dobbiamo
			// prendere solo quelli effettivamente presenti sul BMC
			boolean temp[];
			int i;
			String portId;
			temp = r.getInputs();
			for (i = 0; i < getDigitalInputPortsNumber(); i++) {
				portId = getInputPortId(i); 
				setPortValue(portId, new Boolean(temp[i]));
			}
			temp = r.getOutputs();
			for (i = 0; i < getDigitalOutputPortsNumber(); i++) {
				portId = getOutputPortId(i); 
				DevicePort p = getPort(portId);
				Boolean newValue = new Boolean(temp[i]);
				Boolean oldValue = (Boolean) p.getCachedValue();
				if (outTimers[i] > 0 && oldValue != null && (p.isDirty() || newValue.equals(new Boolean(true)) || ! newValue.equals(oldValue))) {
					p.setValue(newValue,outTimers[i]);						
				} else {
					p.setValue(newValue);
				}
			}			
			return true;
		} else {
			return false;			
		}
	}


	/**
	 * @see BMC.sendPortValue
	 * @param newValue Boolean
	 */
	public boolean sendPortValue(String portId, Object newValue) throws AISException {
		DevicePort p = getPort(portId);
		int intValue = 0;
		if (Boolean.class.isInstance(newValue)) {
			intValue = ((Boolean)newValue).booleanValue() ? 1 : 0;
		} else {
			throw new AISException(getAddress() + " tipo valore non valido: " + newValue.getClass().getCanonicalName());
		}
		int portNumber = getOutputNumberFromPortId(portId);
		if (portNumber == -1) {
			throw new AISException("Porta non valida: " + portId);
		}
		ComandoUscitaMessage m = new ComandoUscitaMessage(getIntAddress(), getBMCComputerAddress(), 0, portNumber, 0, intValue);
		return getConnector().sendMessage(m);
	}

}
