package it.ascia.dmx;

import org.apache.log4j.Logger;

import it.ascia.ais.Connector;
import it.ascia.ais.Message;
import it.ascia.ais.MessageParser;
import it.ascia.ais.Transport;


public class DMXStageProfiMK3 extends MessageParser {

	Logger logger;
	
	public DMXStageProfiMK3() {
		logger = Logger.getLogger(getClass());
	}

	public boolean sendMessage(Message m, Transport t) {
		logger.info(m);
		t.write(m.getBytesMessage());
		return true;
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
