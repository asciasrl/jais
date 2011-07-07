package it.ascia.dmx;

import org.apache.log4j.Logger;

import it.ascia.ais.Message;
import it.ascia.ais.MessageParser;

public class DMXStageProfiMK3 extends MessageParser {

	Logger logger;
	
	public DMXStageProfiMK3() {
		logger = Logger.getLogger(getClass());
	}

	public void dispatchMessage(Message m) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String dumpBuffer() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void push(int b) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isBusy() {
		// TODO Auto-generated method stub
		return false;
	}


}
