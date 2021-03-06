/**
 * Copyright (C) 2008 ASCIA S.R.L.
 */
package it.ascia.eds.device;

import it.ascia.ais.AISException;
import it.ascia.ais.Connector;
import it.ascia.eds.msg.EDSMessage;
import it.ascia.eds.msg.RispostaStatoMessage;

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
	public BMCIR(Connector connector, String address, int model, String name) throws AISException {
		super(connector, address, model, name);
	}
	
	public String getInfo() {
		return getName() + ": BMC IR (modello " + model + ") con " 
			+ getInPortsNumber() + " ingressi digitali, " 
			+ getOutPortsNumber() + " uscite digitali e "
			+ getIrPortsNumber() + " ingressi infrarossi";
	}
	
	/**
	 * @return Numero di ingressi IR
	 */
	public int getIrPortsNumber() {
		return 8;
	}
	
	public int getInPortsNumber() {
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
	
	public int getOutPortsNumber() {
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
	
}
