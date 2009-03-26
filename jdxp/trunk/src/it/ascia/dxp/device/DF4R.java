package it.ascia.dxp.device;

import it.ascia.ais.AISException;
import it.ascia.ais.Connector;
import it.ascia.ais.Message;
import it.ascia.dxp.DominoDevice;
import it.ascia.dxp.msg.ComandoUsciteMessage;
import it.ascia.dxp.msg.RichiestaStatoUsciteMessage;

public class DF4R extends DominoDevice {

	public DF4R(Connector connector, String address) throws AISException {
		super(connector, address);
		for (int i = 1; i <= 4; i++) {
			addPort("o"+address+"."+new Integer(i).toString());			
		}		
	}

	public String getInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	public long updatePort(String portId) throws AISException {
		int i = portId.indexOf(".");
		if (i > 0) {
			int d = (new Integer(portId.substring(1,i))).intValue();
			RichiestaStatoUsciteMessage m = new RichiestaStatoUsciteMessage(d);
			getConnector().queueMessage(m);
		}
		return 100;
	}

	public boolean writePort(String portId, Object newValue)
			throws AISException {
		// TODO Auto-generated method stub
		int i = portId.indexOf(".");
		if (i > 0) {
			int d = (new Integer(portId.substring(1,i))).intValue();
			int uscita = (new Integer(portId.substring(i+1))).intValue();			
			boolean attiva = (new Boolean((String)newValue)).booleanValue();
			ComandoUsciteMessage m = new ComandoUsciteMessage(d,uscita,attiva);
			return getConnector().sendMessage(m);
		}
		return false;
	}

	public void messageReceived(Message m) {
		// TODO Auto-generated method stub
		
	}

	public void messageSent(Message m) {
		// TODO Auto-generated method stub
		
	}

}
