package it.ascia.dmx;

import it.ascia.ais.AISException;
import it.ascia.ais.Device;
import it.ascia.ais.DevicePort;

public class DMXFixture extends Device {

	public DMXFixture(String name, int startChannel, int nChannels, String[] portIds) {
		super(name);
		/*
		if (portIds == null) {
			portIds = new String[nChannels];
			for (int i=0; i < nChannels; i++) {
				portIds[i] = "Channel" + (startChannel + i); 
			}
		}
		*/
		for (int i=0; i < nChannels; i++) {
			String portId = portIds[i];
			DMXChannel c = new DMXChannel(startChannel + i);
			DevicePort port = new DMXChannelPort(portId,c);
			this.addPort(port);
			getConnector().addDevice(c);
		}
	}

	/*
	public DMXFixture(String name, int startChannel, int nChannels) {
		this(name,startChannel,nChannels,null);
	}
	*/

	@Override
	public boolean sendPortValue(String portId, Object newValue)
			throws AISException {
		return ((DMXChannelPort)getPort(portId)).sendChannelValue(newValue);
	}

	@Override
	public boolean updatePort(String portId) throws AISException {
		DMXChannelPort port = (DMXChannelPort) getPort(portId);
		return port.update();
	}

}
