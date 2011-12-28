/**
 * Copyright (C) 2008 ASCIA S.R.L.
 */
package it.ascia.eds.device;

import it.ascia.ais.AISException;

/**
 * Un BMC con porte di input + 1 a infrarossi.
 * 
 * Gli ingressi sono "mappati" su porte di ingresso. Quindi gli ingressi
 * logici sono sempre 8. Alcuni possono essere _anche_ fili.
 * 
 * Modelli: 41, 61, 81
 * 
 * @author arrigo
 */
public class BMCIR extends BMCStandardIO {

	/**
	 * Costruttore
	 * @param address indirizzo del BMC
	 * @param model numero del modello
	 * @throws AISException 
	 */
	public BMCIR(String address, int model, int version, String name) throws AISException {
		super(address, model, version, name);
	}
	
	public String getInfo() {
		return getName() + ": BMC IR (modello " + model + ") con " 
			+ getDigitalInputPortsNumber() + " ingressi digitali, " 
			+ getDigitalOutputPortsNumber() + " uscite digitali e "
			+ getIrPortsNumber() + " ingressi infrarossi";
	}
	
	/**
	 * @return Numero di ingressi IR
	 */
	public int getIrPortsNumber() {
		return 8;
	}
	
	public int getDigitalInputPortsNumber() {
		switch (model) {
		case 21:
			return 2;
		case 41:
			return 4;
		case 61:
			return 6;
		case 81:
		case 89:
			return 8;
		default: // This should not happen(TM)
			logger.error("Errore: modello di BMCIR sconosciuto:"
					+ model);
			return 0;
		}
	}
	
	public int getDigitalOutputPortsNumber() {
		switch (model) {
		case 21:
		case 41:
		case 61:
		case 81:
			return 0;
		case 89:
			return 8;
		default: // This should not happen(TM)
			logger.error("Errore: modello di BMCIR sconosciuto:"
					+ model);
			return 0;
		}
	}
	
	public void setInputName(int number, String name) throws AISException {
		if (number < getDigitalInputPortsNumber()) {
			setPortDescription(getInputPortId(number), name);
		}
		if (number <= getIrPortsNumber()) {
			// TODO Aggiungere anche porte IR ?
		}		
	}

	
}
