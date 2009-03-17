/**
 * Copyright (C) 2008 ASCIA S.R.L.
 */
package it.ascia.eds.device;

import it.ascia.ais.AISException;
import it.ascia.ais.Connector;

/**
 * Una centralina scenari.
 * 
 * Modelli: 152, 154, 156, 158
 * 
 * @author arrigo
 */
public class BMCScenarioManager extends BMCStandardIO {

	/**
	 * Numero di porte in ingresso
	 */
	private int inPortsNum;

	/**
	 * Numero di porte in ingresso
	 */
	private int outPortsNum = 0;

	/**
	 * Costruttore
	 * @param connector 
	 * @param address indirizzo del BMC
	 * @param model numero del modello
	 * @throws AISException 
	 */
	public BMCScenarioManager(Connector connector, String address, int model, String name) throws AISException {
		super(connector, address, model, name);
		switch(model) {
		case 152:
			inPortsNum = 2;
			break;
		case 154:
			inPortsNum = 4;
			break;
		case 156:
			inPortsNum = 6;
			break;
		case 158:
			inPortsNum = 8;
			break;
		default: // This should not happen(TM)
			logger.error("Errore: modello di centralina scenari " + 
					"sconosciuto: " + model);
			inPortsNum = 0;
		}
	}
	
	public String getInfo() {
		return getName() + ": BMC centralina scenari (modello " + model + ")" +
			" con " + inPortsNum + " ingressi digitali";
	}
	
	public int getFirstInputPortNumber() {
		return 1;
	}

	public int getOutPortsNumber() {
		return outPortsNum;
	}
	/**
	 * Gli scenari non sono attivabili con comandi broadcast 
	 */
	public int getCaselleNumber() {
		return 0;
	}
	
	public int getInPortsNumber() {
		return inPortsNum;
	}

}
