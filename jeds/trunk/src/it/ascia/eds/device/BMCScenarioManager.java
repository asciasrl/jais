/**
 * Copyright (C) 2008 ASCIA S.R.L.
 */
package it.ascia.eds.device;

import it.ascia.ais.AISException;
import it.ascia.ais.Connector;
import it.ascia.ais.TriggerPort;

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

	/**
	 * Costruttore
	 * @param connector 
	 * @param address indirizzo del BMC
	 * @param model numero del modello
	 * @throws AISException 
	 */
	public BMCScenarioManager(Connector connector, String address, int model, String name) throws AISException {
		super(connector, address, model, name);
		// aggiunge le porte per l'attivazione delle scene
		for (int i = 1; i < getSceneNumber(); i++) {
			addPort(new TriggerPort(this,"Scene"+i));
		}
	}
	
	public String getInfo() {
		return getName() + ": BMC centralina scenari (modello " + model + ")" +
			" con " + getInPortsNumber() + " ingressi digitali";
	}
	
	public int getFirstInputPortNumber() {
		return 1;
	}

	public int getOutPortsNumber() {
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
		case 164:
			return 8;
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
	
	public int getInPortsNumber() {
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
		case 164:
			return 8;
		case 165:
			return 0;
		default: // This should not happen(TM)
			logger.error("Errore: modello di centralina scenari " + 
					"sconosciuto: " + model);
			return 0;
		}
	}
	
	public int getOutputNumberFromPortId(String portId) {
		if (portId.startsWith("Scene")) {
			int retval = -1;		
			int max = getSceneNumber();
			// TODO Non e' il massimo dell'efficienza, ma funziona.
			for (int i = 0; (i < max) && (retval == -1); i++) {
				if (portId.equals(getScenePortId(i))) {
					retval = i;
				}
			}
			return retval;
		} else {
			return super.getOutputNumberFromPortId(portId);
		}
	}

	/**
	 * Genera il nome per una porta di attivazione scenario.
	 */
	private String getScenePortId(int number) {
		return "Scene" + (number +1);
	}

}
