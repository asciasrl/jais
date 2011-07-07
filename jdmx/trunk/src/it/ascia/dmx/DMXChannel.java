package it.ascia.dmx;

import it.ascia.ais.AISException;
import it.ascia.ais.Device;

/**
 * DMX Single channel interface
 * Il canale e' il dispositivo DMX elementare
 * @author Sergio
 *
 */
public class DMXChannel extends Device {
	
	private int channel;

	public DMXChannel(int i) {
		super("Channel" + i);
		channel = i;
		addPort(new DMXChannelPort("out"));
	}

	@Override
	public boolean sendPortValue(String portId, Object newValue)
			throws AISException {
		if (portId == "value") {
			DMXSetMessage m = new DMXSetMessage(channel, ((Integer)newValue).intValue());
			return getConnector().sendMessage(m);
		} else {
			return false;
		}
	}

	@Override
	public long updatePort(String portId) throws AISException {
		DMXGetMessage m = new DMXGetMessage(channel);
		if (getConnector().sendMessage(m)) {
			return 10;
		} else {
			return 0;
		}
	}

}
