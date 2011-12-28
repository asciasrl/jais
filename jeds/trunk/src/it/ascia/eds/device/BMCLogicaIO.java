package it.ascia.eds.device;

import it.ascia.ais.AISException;

public class BMCLogicaIO extends BMCStandardIO {

	public BMCLogicaIO(String bmcAddress, int model, int version, String name) throws AISException {
		super(bmcAddress, model, version, name);
	}

	public String getInfo() {
		return getName() + ": BMC Logica I/O (modello " + model + ") con "
				+ getDigitalInputPortsNumber() + " ingressi e " + getDigitalOutputPortsNumber()
				+ " uscite digitali";
	}
	
	public int getDigitalInputPortsNumber() {
		switch (model) {
			case 141:
			case 142:
			case 143:
			case 144:
				return 0;
			case 145:
			case 149:
				return 2;
			case 146:
			case 150:
				return 4;
			case 147:
				return 6;
			case 148:
			case 151:
				return 8;
			default: // This should not happen(TM)
				throw(new AISException("modello di BMCRalaysIO sconosciuto:" + model));
		}
	}
	
	public int getDigitalOutputPortsNumber() {
		switch (model) {
			case 141:
			case 149:
				return 2;
			case 142:
			case 150:
				return 4;
			case 143:
				return 6;
			case 144:
			case 151:
				return 8;
			case 145:
			case 146:
			case 147:
			case 148:
				return 0;
			default: // This should not happen(TM)
				throw(new AISException("modello di BMCRalaysIO sconosciuto:" + model));
		}
	}

}
