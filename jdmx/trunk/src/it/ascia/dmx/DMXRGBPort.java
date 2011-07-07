package it.ascia.dmx;

import it.ascia.ais.DevicePort;

public class DMXRGBPort extends DevicePort {

	private DMXChannelPort R;
	private DMXChannelPort G;
	private DMXChannelPort B;

	public DMXRGBPort(String portId, DMXChannelPort R, DMXChannelPort G, DMXChannelPort B ) {
		super(portId);
		this.R = R;
		this.G = G;
		this.B = B;
	}

	@Override
	protected Object normalize(Object newValue) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return newValue;
	}
	
	@Override
	public String getCachedValue() {
		return R.getCachedValue() + "," + G.getCachedValue() + "," + B.getCachedValue();
	}
	
	@Override
	public boolean writeValue(Object newValue) {
		if (String.class.isInstance(newValue)) {
			String[] RGB = ((String)newValue).split(",");
			return R.writeValue(Integer.parseInt(RGB[0])) &
				G.writeValue(Integer.parseInt(RGB[1])) &
				B.writeValue(Integer.parseInt(RGB[2]));			
		} else {
			return false;
		}
	}
	

}
