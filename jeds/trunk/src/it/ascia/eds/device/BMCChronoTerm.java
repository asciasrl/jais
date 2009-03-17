/**
 * Copyright (C) 2008 ASCIA S.R.L.
 */
package it.ascia.eds.device;


import it.ascia.ais.AISException;
import it.ascia.ais.Connector;
import it.ascia.ais.DevicePort;
import it.ascia.eds.EDSConnector;
import it.ascia.eds.msg.CronotermMessage;
import it.ascia.eds.msg.ImpostaSetPointMessage;
import it.ascia.eds.msg.EDSMessage;
import it.ascia.eds.msg.RichiestaSetPointMessage;
import it.ascia.eds.msg.RichiestaStatoTermostatoMessage;
import it.ascia.eds.msg.RispostaStatoTermostatoMessage;
import it.ascia.eds.msg.VariazioneIngressoMessage;

/**
 * Un BMC cronotermostato.
 * 
 * Modelli: 127.
 * 
 * @author arrigo
 */
public class BMCChronoTerm extends BMC {
	/**
	 * Temperatura antigelo.
	 */
	public static final int STATE_TEMP_ANTIFREEZE = 0;
	/**
	 * Temperatura T1.
	 */
	public static final int STATE_TEMP_T1 = 1;
	/**
	 * Temperatura T2.
	 */
	public static final int STATE_TEMP_T2 = 2;
	/**
	 * Temperatura T3.
	 */
	public static final int STATE_TEMP_T3 = 3;
	/**
	 * Temperatura di set point + 0.5°C.
	 */
	public static final int STATE_TEMP_SETPOINT_PLUS = 4;
	/**
	 * Temperatura di set point - 0.5°C.
	 */
	public static final int STATE_TEMP_SETPOINT_MINUS = 5;
	/**
	 * Modalita' estate.
	 */
	public static final int STATE_SUMMER_MODE = 8;
	/**
	 * Modalita' inverno.
	 */
	public static final int STATE_WINTER_MODE = 9;
	/**
	 * Modo cronotermostato
	 */
	public static final int STATE_CHRONO = 12;
	/**
	 * Manuale.
	 */
	public static final int STATE_MANUAL = 13;
	/**
	 * Spento.
	 */
	public static final int STATE_OFF = 15;
	/**
	 * Array per ottenere una rappresentazione testuale dello stato.
	 */
	private static final String stateStrings[] = {
		"antifreeze", // 0
		"temp_t1", // 1
		"temp_t2", // 2
		"temp_t3", // 3
		"setpoint_plus_half", // 4
		"setpoint_minus_half", // 5
		"unknown", // 6
		"unknown", // 7
		"summer", // 8
		"winter", // 9
		"unknown", // 10
		"unknown", // 11
		"chrono", // 12
		"manual", // 13
		"unknown", // 14
		"off"}; // 15 
	
	/**
	 * Nome compatto della porta "temperatura"
	 */
	private static final String port_temperature = "temp";
	/**
	 * Nome compatto della porta "set point".
	 */
	private static final String port_setpoint = "setPoint";
	/**
	 * Nome compatto della porta "stato del termostato".
	 */
	private static final String port_state = "state";
	
	private static final String port_season = "season";
	
	/**
	 * Costruttore
	 * @param address indirizzo del BMC
	 * @param model numero del modello
	 * @throws AISException 
	 */
	public BMCChronoTerm(Connector connector, String address, int model, String name) throws AISException {
		super(connector, address, model, name);
		switch(model) {
		case 127:
			break;
		default: // This should not happen(TM)
			logger.error("Errore: modello di BMCChronoTerm sconosciuto:" +
					model);
		}
		addPort(port_setpoint);
		addPort(port_state);
		addPort(port_temperature);
		addPort(port_season);
	}
	
	/* (non-Javadoc)
	 * @see it.ascia.eds.device.BMC#receiveMessage(it.ascia.eds.msg.Message)
	 */
	// TODO: reagire a messaggi broadcast
	public void messageReceived(EDSMessage m) throws AISException {
		switch (m.getMessageType()) {
		case EDSMessage.MSG_VARIAZIONE_INGRESSO: {
			// Qualcuno ha premuto un interruttore. Siamo dirty, finche' non
			// vedremo un acknowledge.
			VariazioneIngressoMessage var = 
				(VariazioneIngressoMessage) m;
			setPortValue(port_state,new Integer(var.getChronoTermState()));
		}
		break;
		case EDSMessage.MSG_IMPOSTA_SET_POINT: {
			// Si vuole cambiare il set point
			ImpostaSetPointMessage set = (ImpostaSetPointMessage) m;
			setPortValue(port_setpoint,new Double(set.getSetPoint()));
		}
		break;
		}
	}
	
	public void messageSent(EDSMessage m) throws AISException {
		// TODO logger.trace("messageSent:0");
		switch (m.getMessageType()) {
		case EDSMessage.MSG_RISPOSTA_STATO_TERMOSTATO:
			// TODO logger.trace("messageSent:1.1");
			updating = true;
			// Questo messaggio contiene il nostro stato
			RispostaStatoTermostatoMessage tm = (RispostaStatoTermostatoMessage) m;
			double temperature = tm.getChronoTermTemperature();
			int state = 0;
			switch (tm.getMode()) {
			case RispostaStatoTermostatoMessage.MODE_ANTI_FREEZE:
				state = STATE_TEMP_ANTIFREEZE;
				break;
			case RispostaStatoTermostatoMessage.MODE_CHRONO:
				state = STATE_CHRONO;
				break;
			case RispostaStatoTermostatoMessage.MODE_MANUAL:
				state = STATE_MANUAL;
				break;
			case RispostaStatoTermostatoMessage.MODE_TIME:
				state = STATE_CHRONO;
				break;
			}
			setPortValue(port_state,getStateAsString(state));
			setPortValue(port_temperature,new Double(temperature));
			updating = false;
			// TODO logger.trace("messageSent:1.4");
			break;
		case EDSMessage.MSG_LETTURA_SET_POINT:
			updating = true;
			CronotermMessage ctm = (CronotermMessage) m;
			double setPoint = ctm.getSetPoint();
			setPortValue(port_setpoint,new Double(setPoint));
			setPortValue(port_season,ctm.isWinter() ? "inverno" : "estate");
			updating = false;
		break;
		}
		// TODO logger.trace("messageSent:3");	
	}
	
	public String getInfo() {
		return getName() + ": BMC cronotermostato (modello " + model + ")";
	}

	/**
	 * Aggiorna lo stato del termostato.
	 */
	public void updateTermStatus() {
		getConnector().sendMessage(new RichiestaStatoTermostatoMessage(getIntAddress(), getBMCComputerAddress()));
	}
	
	/**
	 * Aggiorna il set point corrente.
	 */
	public void updateSetPoint() {
		getConnector().sendMessage(new RichiestaSetPointMessage(getIntAddress(), getBMCComputerAddress()));
	}
	
	public long updateStatus() {
		// FIXME calcolare il timeout oggettivamente
		long timeout = 2 * 2 * ((EDSConnector)getConnector()).getRetryTimeout();		
		if (updating) {
			logger.trace("update in corso, richiesta omessa");
			return timeout;
		}		
		updateTermStatus();
		updateSetPoint();
		return timeout;
	}
	
	/**
	 * Imposta il set-point.
	 * 
	 * @param temperature temperatura di set-point da impostare
	 * 
	 * @return true se il BMC ha risposto (ACK)
	 */
	public boolean setSetPoint(double temperature) {
		ImpostaSetPointMessage m;
		m = new ImpostaSetPointMessage(getIntAddress(), getBMCComputerAddress(), temperature);
		return getConnector().sendMessage(m);
	}
	
	/**
	 * Imposta lo stato del cronotermostato.
	 * 
	 * <p>Invia un messaggio mettendo il BMCComputer come mittente.</p>
	 * 
	 * @param state una delle costanti statiche di questa classe.
	 * 
	 * @return true se il BMC ha risposto (ACK)
	 */
	public boolean setState(int state) {
		VariazioneIngressoMessage m;
		m = new VariazioneIngressoMessage(getIntAddress(), getBMCComputerAddress(),state);
		return getConnector().sendMessage(m);
	}
	
	/**
	 * Ritorna lo stato del cronotermostato sotto forma di stringa.
	 */
	public static String getStateAsString(int state) {
		// Sanity check
		if ((state >= 0) && (state < stateStrings.length)) {
			return stateStrings[state];
		}
		return null;
	}

	public String getStatus(String portId, long timestamp) throws AISException {
		DevicePort p = getPort(portId);
		if (p.getTimeStamp() >= timestamp) {
			return p.getFullAddress() + "=" + p.getValue();
		} else {
			return "";
		}
	}

	// TODO Eliminare getFirstInputPortNumber()
	public int getFirstInputPortNumber() {
		return 1;
	}

	// TODO Eliminare getOutPortsNumber()
	public int getOutPortsNumber() {
		return 0;
	}
	
	/**
	 * Permette l'impostazione di temperatura o stato.
	 * 
	 * <p>Il valore di temperatura non viene verificato.</p>
	 * 
	 * <p>Lo stato puo' essere indicato come valore testuale o numerico.</p>
	 * 
	 * @param port nome della porta.
	 * @param value valore da impostare.
	 */
	public void setPort(String port, String value) throws AISException {
		boolean success = false;
		if (port.equals(port_setpoint)) {
			// Impostiamo il setpoint
			double setPoint;
			try {
				setPoint = Double.parseDouble(value);
			} catch (NumberFormatException e) {
				throw new AISException("Invalid temperature: " + value);
			}
			success = setSetPoint(setPoint);
		} else if (port.equals(port_state)) {
			// Impostiamo lo stato
			int requiredState = -1;
			try {
				requiredState = Integer.parseInt(value);
			} catch (NumberFormatException e) {
				// Non e' un intero, proviamo la stringa.
				for (int i = 0; 
					(i < stateStrings.length) && (requiredState == -1); 
					i++) {
					if (stateStrings[i].equals(value)) {
						requiredState = i;
					}
				}
			} // catch
			if (requiredState != -1) {
				success = setState(requiredState);
			} else {
				throw new AISException("Invalid state: " + value);
			}
		} else {
			// Non impostiamo niente: la richiesta e' errata.
			throw new AISException("Invalid port: " + port);
		}
		if (!success) {
			throw new AISException("Il device non risponde");
		}
	}

	public int getInPortsNumber() {
		return 0;
	}

}
