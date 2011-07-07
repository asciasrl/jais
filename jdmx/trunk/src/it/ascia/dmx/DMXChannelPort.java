package it.ascia.dmx;

import it.ascia.ais.DevicePort;
import it.ascia.ais.port.IntegerPort;

/**
 * Porta che corrisponde ad un canale DMX
 * @author Sergio
 *
 */
public class DMXChannelPort extends IntegerPort {
	
	private DMXChannel channelDevice = null;
	private String channelPortId;

	/**
	 * Porta virtuale
	 * @param portId Nome di questa porta
	 * @param channel Canale corrispondente alla porta
	 */
	public DMXChannelPort(String portId, DMXChannel channel) {
		super(portId,0,255);
		if (channel != null) {
			channelDevice = channel;
			// estre il nome della prima ed unica porta
			channelPortId = ((DevicePort[])channel.getPorts().toArray())[0].getPortId();
		}
	}

	/**
	 * Porta effettiva
	 * @param portId
	 */
	public DMXChannelPort(String portId) {
		this(portId,null);
	}
	
	@Override
	public long update() {
		if (channelDevice == null) {
			return super.update();
		} else {
			return channelDevice.updatePort(channelPortId);
		}
	}
	
	boolean sendChannelValue(Object newValue) {
		if (channelDevice == null) {
			return false;
		} else {
			return channelDevice.sendPortValue(channelPortId, newValue);
		}		
	}

}
