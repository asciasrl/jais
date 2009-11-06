package it.ascia.dxp;

import it.ascia.ais.AISException;
import it.ascia.ais.Connector;
import it.ascia.ais.Device;
import it.ascia.ais.Message;
import it.ascia.ais.port.DigitalInputPort;
import it.ascia.ais.port.DigitalOutputPort;

public abstract class DominoDevice extends Device {

	public DominoDevice(Connector connector, String address)
			throws AISException {
		super(connector, address);
	}
	
	public void addPort(String portId, String portName) {
		if (portId.startsWith("i")) {
			addPort(new DigitalInputPort(this,portId,portName));
		} else if (portId.startsWith("o")) {
			addPort(new DigitalOutputPort(this,portId,portName));
		} 
		logger.fatal("Id porta scorretto:"+portId);
	}

	public abstract void messageReceived(Message m);

	public abstract void messageSent(Message m);

	public boolean isUnreachable() {
		// TODO Auto-generated method stub
		return false;
	}

	public void setReachable() {
		// TODO Auto-generated method stub
		
	}

	public void setUnreachable() {
		// TODO Auto-generated method stub
		
	}
	
}
