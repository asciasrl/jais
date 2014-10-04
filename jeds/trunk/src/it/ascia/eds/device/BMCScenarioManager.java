/**
 * Copyright (C) 2008 ASCIA S.R.L.
 */
package it.ascia.eds.device;

import it.ascia.ais.AISException;
import it.ascia.ais.port.ScenePort;
import it.ascia.eds.msg.ComandoUscitaMessage;
import it.ascia.eds.msg.EDSMessage;
import it.ascia.eds.msg.VariazioneIngressoMessage;

/**
 * Una centralina scenari.
 * 
 * Modelli: 152, 154, 156, 158 (obsoleti)
 * Modelli: 161,162,163,164,165
 * 
 * @author arrigo
 * @author sergio
 * 
 * TODO: gestire lo stato di attivazione degli scenari ?
 */
public class BMCScenarioManager extends BMCStandardIO {

	private static final String SCENE_PORTID_PREFIX = "Scene";
	/**
	 * Costruttore
	 * @param connector 
	 * @param address indirizzo del BMC
	 * @param model numero del modello
	 * @throws AISException 
	 */
	public BMCScenarioManager(String address, int model, int version, String name) throws AISException {
		super(address, model, version, name);
		// aggiunge le porte per l'attivazione delle scene
		for (int i = 1; i <= getSceneNumber(); i++) {
			addPort(new ScenePort(SCENE_PORTID_PREFIX+i));
		}
	}

	/**
	 * Nessun discover da effettuare, le uscite non sono programmabili
	 */
	public void discover() {
	}

	public String getInfo() {
		return getName() + ": "+getSceneNumber()+" scenari " +
			", " + getDigitalInputPortsNumber() + " ingressi e " + getDigitalOutputPortsNumber() + " uscite.";
	}
	
	public int getDigitalOutputPortsNumber() {
		switch(model) {
		case 152:
		case 161:
		case 154:
		case 162:
		case 156:
		case 158:
		case 163:
		case 164:
		case 165:
			return 0;
		default: // This should not happen(TM)
			logger.error("Errore: modello di centralina scenari " + 
					"sconosciuto: " + model);
			return 0;
		}
	}
	
	/**
	 * @return Numero di scenari
	 */
	public int getSceneNumber() {
		switch(model) {
		case 152:
		case 161:
			return 2;
		case 154:
		case 162:
			return 4;
		case 156:
			return 6;
		case 158:
		case 163:
			return 8;
		case 164:
		case 165:
			return 16;
		default: // This should not happen(TM)
			logger.error("Errore: modello di centralina scenari " + 
					"sconosciuto: " + model);
			return 0;
		}

	}

	/**
	 * Gli scenari non sono attivabili con comandi broadcast 
	 */
	public int getCaselleNumber() {
		return 0;
	}
	
	public int getDigitalInputPortsNumber() {
		switch(model) {
		case 152:
		case 161:
		case 154:
		case 162:
		case 156:
		case 158:
		case 163:
		case 164:
		case 165:
			return 0;
		default: // This should not happen(TM)
			logger.error("Errore: modello di centralina scenari " + 
					"sconosciuto: " + model);
			return 0;
		}
	}
	
	/**
	 * Restituisce il numero di scenario dall'id della porta
	 * Scene1 -> 0
	 * @param portId
	 * @return
	 */
	private int getSceneNumberFromPortId(String portId) {
		int l = SCENE_PORTID_PREFIX.length();
		if (portId.startsWith(SCENE_PORTID_PREFIX) && portId.length() > l) {
			try {
				int retval = new Integer(portId.substring(l)).intValue();
				if (retval < 1) {
					throw(new AISException("Port "+portId+" specifiy non positive scene"));				
				}
				if (retval > getSceneNumber()) {
					throw(new AISException("Port "+portId+" specifiy a scene over max scene number"));								
				}
				return retval - 1;
			} catch (NumberFormatException e) {
				throw(new AISException("Port "+portId+" is not a valid id for Scene port"));
			}
		} else {
			throw(new AISException("Port "+portId+" is not a valid id for Scene port"));
		}
	}

	/**
	 * Genera il nome per una porta di attivazione scenario.
	 * 0 -> Scene1
	 */
	private String getScenePortId(int number) {
		return SCENE_PORTID_PREFIX + (number +1);
	}

	public boolean sendPortValue(String portId, Object newValue) {
		if (portId.startsWith(SCENE_PORTID_PREFIX)) {
			return getConnector().sendMessage(new ComandoUscitaMessage(getIntAddress(), getBMCComputerAddress(), getSceneNumberFromPortId(portId)));
		} else {
			return super.sendPortValue(portId, newValue);
		}
	}
	
	public void messageSent(EDSMessage m) {
		switch (m.getMessageType()) {
			case EDSMessage.MSG_COMANDO_USCITA:
			case EDSMessage.MSG_COMANDO_USCITA_DIMMER:
				// messaggi ignorati silentemente
				break;
			default:
				super.messageSent(m);
		}
	}
	
	public void messageReceived(EDSMessage m) throws AISException {
		int port;
		switch (m.getMessageType()) {
		case EDSMessage.MSG_COMANDO_USCITA:
			ComandoUscitaMessage cmd = (ComandoUscitaMessage) m;
			port = cmd.getScenePortNumber();
			logger.info("Attivata scena "+getAddress()+":"+getScenePortId(port));
			if (port > (getDigitalOutputPortsNumber() - 1)) {
				if (port > (getSceneNumber() -1 )) {
					throw(new AISException("Numero di scena non valido:"+port));
				}
			} else {
				super.messageReceived(m);
			}
			break;
		case EDSMessage.MSG_VARIAZIONE_INGRESSO:
			VariazioneIngressoMessage vmsg = (VariazioneIngressoMessage) m;
			port = vmsg.getScenePortNumber();
			if (vmsg.isClose()) {
				logger.info("Attivata scena "+getAddress()+":"+getScenePortId(port));
			}
			if (port > (getDigitalOutputPortsNumber() - 1)) {
				if (port > (getSceneNumber() -1 )) {
					throw(new AISException("Numero di scena non valido:"+port));
				}
			} else {
				super.messageReceived(m);
			}
			break;
		default:
			super.messageReceived(m);
		}
	}

}
