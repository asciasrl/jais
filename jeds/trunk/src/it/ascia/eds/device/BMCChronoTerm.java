/**
 * Copyright (C) 2008 ASCIA S.R.L.
 */
package it.ascia.eds.device;


import it.ascia.ais.AISException;
import it.ascia.ais.port.SeasonPort;
import it.ascia.ais.port.SlaveStatePort;
import it.ascia.ais.port.StatePort;
import it.ascia.ais.port.TemperaturePort;
import it.ascia.ais.port.TemperatureSetpointPort;
import it.ascia.ais.port.NullPort;
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
	public static final String stateStrings[] = {
		"temp_t0", // 0
		"temp_t1", // 1
		"temp_t2", // 2
		"temp_t3", // 3
		"stp_up", // 4
		"stp_dn", // 5
		null, // 6
		null, // 7
		"summer", // 8
		"winter", // 9
		null, // 10
		null, // 11
		"chrono", // 12
		"manual", // 13
		null, // 14
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
	public BMCChronoTerm(String address, int model, int version, String name) throws AISException {
		super(address, model, version, name);
		addPort(new TemperaturePort(port_temperature));
		addPort(new TemperatureSetpointPort(port_setpoint));

		StatePort statePort = new StatePort(port_state,stateStrings);
		addPort(statePort);
		addPort(new SlaveStatePort(stateStrings[STATE_CHRONO],statePort));
		addPort(new SlaveStatePort(stateStrings[STATE_MANUAL],statePort));
		addPort(new SlaveStatePort(stateStrings[STATE_OFF],statePort));

		SeasonPort seasonPort = new SeasonPort(port_season); 
		addPort(seasonPort);
		addPort(new SlaveStatePort(stateStrings[STATE_SUMMER_MODE],seasonPort));
		addPort(new SlaveStatePort(stateStrings[STATE_WINTER_MODE],seasonPort));		

		addPort(new NullPort(stateStrings[STATE_TEMP_ANTIFREEZE]));
		addPort(new NullPort(stateStrings[STATE_TEMP_SETPOINT_MINUS]));
		addPort(new NullPort(stateStrings[STATE_TEMP_SETPOINT_PLUS]));
		addPort(new NullPort(stateStrings[STATE_TEMP_T1]));
		addPort(new NullPort(stateStrings[STATE_TEMP_T2]));
		addPort(new NullPort(stateStrings[STATE_TEMP_T3]));

	}
	
	/* (non-Javadoc)
	 * @see it.ascia.eds.device.BMC#receiveMessage(it.ascia.eds.msg.Message)
	 */
	public void messageReceived(EDSMessage m) throws AISException {
		switch (m.getMessageType()) {
		case EDSMessage.MSG_COMANDO_BROADCAST:
			// TODO invalidare solo le porte giuste
			getPort(port_state).invalidate();
			getPort(port_setpoint).invalidate();			
			getPort(port_season).invalidate();			
			break;
		case EDSMessage.MSG_VARIAZIONE_INGRESSO:
			// TODO invalidare solo le porte giuste
			getPort(port_state).invalidate();
			getPort(port_setpoint).invalidate();			
			getPort(port_season).invalidate();			
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
		switch (m.getMessageType()) {
		case EDSMessage.MSG_RISPOSTA_STATO_TERMOSTATO:
		case EDSMessage.MSG_LETTURA_SET_POINT:
			// Questo messaggio contiene il nostro stato
			break;
		default:
			super.messageSent(m);
		}
	}
	
	public String getInfo() {
		return getName() + ": BMC cronotermostato (modello " + model + ")";
	}

	/**
	 * Aggiorna lo stato del termostato.
	 */
	public boolean updateTermStatus() {
		RichiestaStatoTermostatoMessage m = new RichiestaStatoTermostatoMessage(getIntAddress(), getBMCComputerAddress());		
		if (getConnector().sendMessage(m)) {
			RispostaStatoTermostatoMessage tm = (RispostaStatoTermostatoMessage) m.getResponse();
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
				// FIXME Il modo temporizzato e' proprio "OFF" ?
				state = STATE_OFF;
				break;
			}
			setPortValue(port_state,getStateAsString(state));
			setPortValue(port_temperature,new Double(temperature));
			return true;
		} else {
			return false;
		}

	}
	
	/**
	 * Aggiorna il set point corrente.
	 */
	public boolean updateSetPoint() {
		RichiestaSetPointMessage m = new RichiestaSetPointMessage(getIntAddress(), getBMCComputerAddress(), false);	
		if (getConnector().sendMessage(m)) {
			CronotermMessage ctm = (CronotermMessage) m.getResponse();
			double setPoint = ctm.getSetPoint();
			setPortValue(port_setpoint,new Double(setPoint));
			setPortValue(port_season,ctm.isWinter() ? getStateAsString(STATE_WINTER_MODE) : getStateAsString(STATE_SUMMER_MODE));
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public boolean updatePort(String portId) throws AISException {
		return updateTermStatus() && updateSetPoint();
	}

	/*
	public long updatePort(String portId) {
		if (portId.equals(stateStrings[STATE_TEMP_SETPOINT_MINUS])) {
			setPortValue(portId,new Boolean(false));
			return 0;
		}
		if (portId.equals(stateStrings[STATE_TEMP_SETPOINT_PLUS])) {
			setPortValue(portId,new Boolean(false));
			return 0;
		}
		// TODO Gli stati T1,T2,T3 sarebbe bello poterli memorizzare, invece di gestirli solo come porte di comando
		if (portId.equals(stateStrings[STATE_TEMP_ANTIFREEZE])) {
			setPortValue(portId,new Boolean(false));
			return 0;
		}
		if (portId.equals(stateStrings[STATE_TEMP_T1])) {
			setPortValue(portId,new Boolean(false));
			return 0;
		}
		if (portId.equals(stateStrings[STATE_TEMP_T2])) {
			setPortValue(portId,new Boolean(false));
			return 0;
		}
		if (portId.equals(stateStrings[STATE_TEMP_T3])) {
			setPortValue(portId,new Boolean(false));
			return 0;
		}
		if (portId.equals(stateStrings[STATE_SUMMER_MODE])) {
			setDependentPort(portId,port_season,SEASON_SUMMER);
			return 0;
		}
		if (portId.equals(stateStrings[STATE_WINTER_MODE])) {
			setDependentPort(portId,port_season,SEASON_WINTER);
			return 0;
		}
		if (portId.equals(stateStrings[STATE_CHRONO])) {
			setDependentPort(portId,port_state,portId);
			return 0;
		}
		if (portId.equals(stateStrings[STATE_MANUAL])) {
			setDependentPort(portId,port_state,portId);
			return 0;
		}
		if (portId.equals(stateStrings[STATE_OFF])) {
			setDependentPort(portId,port_state,portId);
			return 0;
		}
		return updateStatus();
	}
	*/
		
	/**
	 * Imposta il set-point.
	 * 
	 * @param temperature temperatura di set-point da impostare
	 * 
	 * @return true se il BMC ha risposto (ACK)
	 */
	/*
	public boolean setSetPoint(double temperature) {
		ImpostaSetPointMessage m;
		m = new ImpostaSetPointMessage(getIntAddress(), getBMCComputerAddress(), temperature);
		return getConnector().sendMessage(m);
	}
	*/
	
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

	/*
	public String getStatus(String portId, long timestamp) throws AISException {
		DevicePort p = getPort(portId);
		if (p.getTimeStamp() >= timestamp) {
			return p.getFullAddress() + "=" + p.getValue();
		} else {
			return "";
		}
	}
	*/

	protected int getDigitalOutputPortsNumber() {
		return 0;
	}
	
	protected int getDigitalInputPortsNumber() {
		return 0;
	}
	
	public boolean sendPortValue(String portId, Object newValue) {
		for (int i = 0; i < stateStrings.length; i++) {
			if (portId.equals(stateStrings[i])) {
				return getConnector().sendMessage(new VariazioneIngressoMessage(getIntAddress(),getBMCComputerAddress(),i));
			}			
		}
		return false;
	}

}
