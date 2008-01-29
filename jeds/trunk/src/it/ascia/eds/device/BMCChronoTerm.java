/**
 * Copyright (C) 2008 ASCIA S.R.L.
 */
package it.ascia.eds.device;

import it.ascia.eds.Bus;
import it.ascia.eds.msg.CronotermMessage;
import it.ascia.eds.msg.ImpostaSetPointMessage;
import it.ascia.eds.msg.Message;
import it.ascia.eds.msg.RichiestaSetPointMessage;
import it.ascia.eds.msg.RichiestaStatoTermostatoMessage;
import it.ascia.eds.msg.TemperatureMessage;
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
	 * Crono.
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
	 * Ingresso termometro.
	 */
	double temperature;
	/**
	 * La temperatura non e' aggiornata.
	 */
	boolean dirtyTemperature;
	/**
	 * Temperatura di set point del termostato.
	 */
	double setPoint;
	/**
	 * Il set point non è aggiornato.
	 */
	boolean dirtySetPoint;
	/**
	 * Stato del termostato.
	 */
	int state;
	/**
	 * Lo stato del termostato non e' aggiornato.
	 */
	boolean dirtyState;
	
	/**
	 * Costruttore
	 * @param address indirizzo del BMC
	 * @param model numero del modello
	 */
	public BMCChronoTerm(int address, int model, Bus bus, String name) {
		// FIXME: quante uscite ha un BMCChronoTerm?
		super(address, model, bus, name);
		switch(model) {
		case 127:
			break;
		default: // This should not happen(TM)
			System.err.println("Errore: modello di BMCChronoTerm sconosciuto:" +
					model);
		}
		state = STATE_OFF; // Un valore qualunque
		dirtyState = true;
		temperature = 0; // Di default fa molto freddo. ;-)
		dirtyTemperature = true;
		setPoint = 0; // Di default vogliamo che faccia molto freddo. ;-)
		dirtySetPoint = true;
	}
	
	/* (non-Javadoc)
	 * @see it.ascia.eds.device.BMC#receiveMessage(it.ascia.eds.msg.Message)
	 */
	// TODO: reagire a messaggi broadcast
	public void messageReceived(Message m) {
		switch (m.getMessageType()) {
		case Message.MSG_VARIAZIONE_INGRESSO: {
			// Qualcuno ha premuto un interruttore. Siamo dirty, finche' non
			// vedremo un acknowledge.
			VariazioneIngressoMessage var = 
				(VariazioneIngressoMessage) m;
			state = var.getChronoTermState();
			dirtyState = true;
		}
		break;
		case Message.MSG_IMPOSTA_SET_POINT: {
			// Si vuole cambiare il set point
			ImpostaSetPointMessage set = (ImpostaSetPointMessage) m;
			setPoint = set.getSetPoint();
			dirtySetPoint = true;
		}
		break;
		}
	}
	
	public void messageSent(Message m) {
		switch (m.getMessageType()) {
		case Message.MSG_TEMPERATURA: {
			// Questo messaggio contiene il nostro stato
			TemperatureMessage tm = (TemperatureMessage) m;
			temperature = tm.getTemperature();
			dirtyTemperature = false;
			switch (tm.getMode()) {
			case TemperatureMessage.MODE_ANTI_FREEZE:
				state = STATE_TEMP_ANTIFREEZE;
				break;
			case TemperatureMessage.MODE_CHRONO:
				state = STATE_CHRONO;
				break;
			case TemperatureMessage.MODE_MANUAL:
				state = STATE_MANUAL;
				break;
			case TemperatureMessage.MODE_TIME:
				state = STATE_CHRONO; // FIXME: che significa?
				break;
			}
			dirtyState = false;
		}
		break;
		case Message.MSG_LETTURA_SET_POINT: {
			// Vediamo qual e' il nostro set-point.
			CronotermMessage ctm = (CronotermMessage) m;
			setPoint = ctm.getSetPoint();
			dirtySetPoint = false;
		}
		break;
		}
	}
	
	public String getInfo() {
		return getName() + ": BMC cronotermostato (modello " + model + ")";
	}

	/**
	 * Aggiorna lo stato del termostato.
	 */
	public void updateTermStatus() {
		bus.sendMessage(new RichiestaStatoTermostatoMessage(getAddress(), 
				bus.getBMCComputerAddress()));
	}
	
	/**
	 * Aggiorna il set point corrente.
	 */
	public void updateSetPoint() {
		bus.sendMessage(new RichiestaSetPointMessage(getAddress(), 
			bus.getBMCComputerAddress()));
	}
	
	public void updateStatus() {
		updateTermStatus();
		updateSetPoint();
	}
	
	/**
	 * Imposta il set-point.
	 */
	public void setSetPoint(double temperature) {
		ImpostaSetPointMessage m;
		m = new ImpostaSetPointMessage(getAddress(), 
				bus.getBMCComputerAddress(), temperature);
		bus.sendMessage(m);
	}

	// TODO
	public String getStatus(String port) {
		return name;
	}

	public int getFirstInputPortNumber() {
		return 1;
	}

	public int getOutPortsNumber() {
		// FIXME: e' vero?
		return 11;
	}
	
	public void printStatus() {
		System.out.println("Stato: " + state + (dirtyState? "?" : ""));
		System.out.println("Temperatura: " + temperature + 
				(dirtyTemperature? "?" : ""));
		System.out.println("Set point: " + setPoint + 
				(dirtySetPoint? "?" : ""));
	}
}
