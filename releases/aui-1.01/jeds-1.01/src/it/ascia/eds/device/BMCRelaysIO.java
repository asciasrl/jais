package it.ascia.eds.device;

import it.ascia.ais.AISException;
import it.ascia.ais.Connector;

public class BMCRelaysIO extends BMCStandardIO {

	public BMCRelaysIO(Connector connector, String bmcAddress, int model,
			String name) throws AISException {
		super(connector, bmcAddress, model, name);
	}

	public String getInfo() {
		return getName() + ": BMC Relays I/O (modello " + model + ") con "
				+ getInPortsNumber() + " ingressi e " + getOutPortsNumber()
				+ " uscite a Relays";
	}
	
	public int getInPortsNumber() {
		switch (model) {
			case 91:
			case 92:
			case 93:
			case 94:
				return 0;
			case 95:
				return 2;
			case 96:
				return 4;
			case 97:
				return 8;
			case 99:
				return 8;
			default: // This should not happen(TM)
				throw(new AISException("modello di BMCRalaysIO sconosciuto:" + model));
		}
	}
	
	public int getOutPortsNumber() {
		switch (model) {
			case 91:
				return 2;
			case 92:
				return 4;
			case 93:
				return 6;
			case 94:
				return 8;
			case 95:
				return 2;
			case 96:
				return 4;
			case 97:
				return 8;
			case 99:
				return 8;
			default: // This should not happen(TM)
				throw(new AISException("modello di BMCRalaysIO sconosciuto:" + model));
		}
	}

}
