package it.ascia.dmx;

public class DMXRGB extends DMXFixture {
	
	public DMXRGB(int i) {
		super("RGB",i,3,new String[]{"R","G","B"});
		addPort(new DMXRGBPort("RGB",(DMXChannelPort)getPort("R"),(DMXChannelPort)getPort("G"),(DMXChannelPort)getPort("B")));
		//TODO addPort(new DMXHSBPort("RGB",getPort("RGB"));
	}
		
}
