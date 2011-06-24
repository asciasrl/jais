package it.ascia.dmx;

import it.ascia.ais.AISException;
import it.ascia.ais.Device;
import it.ascia.ais.port.IntegerPort;

public class DMXRGB extends Device {
	
	private DMXChannel R,G,B;

	public DMXRGB(int i, DMXChannel r, DMXChannel g, DMXChannel b) {
		super("rgb"+i);
		R = r;
		G = g;
		B = b;
		addPort(new IntegerPort("R",0,255));
		addPort(new IntegerPort("G",0,255));
		addPort(new IntegerPort("B",0,255));
	}

	@Override
	public boolean sendPortValue(String portId, Object newValue)
			throws AISException {
		if (portId.equals("R")) {
			return R.sendPortValue("value", newValue);
		} else if (portId.equals("G")) {
			return G.sendPortValue("value", newValue);
		} else if (portId.equals("B")) {
			return B.sendPortValue("value", newValue);
		} else {
			return false;
		}
	}

	@Override
	public long updatePort(String portId) throws AISException {
		// TODO Auto-generated method stub
		return 0;
	}

}
