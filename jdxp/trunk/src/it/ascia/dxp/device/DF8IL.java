package it.ascia.dxp.device;

import it.ascia.ais.AISException;
import it.ascia.ais.Connector;

public class DF8IL extends DF4IV {

	protected int getNumInputs() {
		return 2;
	}

	protected int getNumVirtuals() {
		return 2;
	}

	public DF8IL(Connector connector, String address) throws AISException {
		super(connector, address);
	}

	/*
	public String getInfo() {
		// TODO Auto-generated method stub
		return null;
	}
	*/

	/*
	public long updatePort(String portId) throws AISException {
		int i = portId.indexOf(".");
		if (i > 0) {
			int d = (new Integer(portId.substring(1,i))).intValue();
			char tipo = portId.substring(0,1).toCharArray()[0];
			logger.trace("portId "+portId+" d="+d+" tipo="+tipo);
			if (tipo == 'i') { 
				RichiestaStatoIngressiMessage m = new RichiestaStatoIngressiMessage(d);
				getConnector().queueMessage(m);
			} else if (tipo == 'v') {
				RichiestaStatoUsciteMessage m = new RichiestaStatoUsciteMessage(d);
				getConnector().queueMessage(m);
			} else {
				throw(new AISException("Porta tipo "+tipo+" non valida"));				
			}
		}
		return 100;
	}

	public boolean writePort(String portId, Object newValue)
			throws AISException {
		// TODO Auto-generated method stub
		return false;
	}

	public void messageReceived(Message m) {
		// TODO Auto-generated method stub
		
	}

	public void messageSent(Message m) {
		// TODO Auto-generated method stub
		
	}
	*/

}
