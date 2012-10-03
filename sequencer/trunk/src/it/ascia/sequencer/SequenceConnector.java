package it.ascia.sequencer;

import it.ascia.ais.ConnectorInterface;
import it.ascia.ais.DevicePort;
import it.ascia.ais.Message;
import it.ascia.ais.SimpleConnector;

public class SequenceConnector extends SimpleConnector implements ConnectorInterface {

	public SequenceConnector(String name) {
		super(name);
	}

	@Override
	public boolean sendMessage(Message m) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void queueUpdate(DevicePort devicePort) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public boolean isAlive() {
		return true;
	}

}
