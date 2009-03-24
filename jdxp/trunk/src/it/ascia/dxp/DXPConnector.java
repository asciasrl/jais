package it.ascia.dxp;

import it.ascia.ais.AISException;
import it.ascia.ais.Connector;
import it.ascia.ais.Controller;
import it.ascia.ais.Message;
import it.ascia.dxp.device.*;

public class DXPConnector extends Connector {

	/**
	 * MessageParser per la lettura dei messaggi in ingresso.
	 */
	protected DXPMessageParser mp;
	
	public DXPConnector(String name, Controller controller) {
		super(name, controller);
		mp = new DXPMessageParser();
	}

	public void received(int b) {
		mp.push(b);
		if (mp.isValid()) {
			DXPMessage m = mp.getMessage();
			if (m != null) {
				receiveQueue.offer(m);
			}
		}    	
	}

	public boolean sendMessage(Message m) {
		// TODO Auto-generated method stub
		return false;
	}

	protected void dispatchMessage(Message m) throws AISException {
		// TODO Auto-generated method stub
		
	}

	public void addModule(String model, String address) throws AISException {
		DominoDevice d = null;
		if (model.equals("DF4I")) {
			d = new DF4I(this, address);
		} else if (model.equals("DF4IV")) {
			d = new DF4IV(this, address);
		} else if (model.equals("DF8IL")) {
			d = new DF8IL(this, address);
		} else if (model.equals("DFIR")) {
			d = new DFIR(this, address);
		} else if (model.equals("DF4R")) {
			d = new DF4R(this, address);
		} else if (model.equals("DFTA")) {
			d = new DFTA(this, address);
		} else if (model.equals("DFTP")) {
			d = new DFTP(this, address);
		} else if (model.equals("DFCT")) {
			d = new DFCT(this, address);
		} else if (model.equals("DFGSM2")) {
			d = new DFGSM2(this, address);
		} else {
			logger.error("Modello sconosciuto: "+model);
		}
		if (d != null) {
			logger.info("Aggiunto modulo "+model+" "+d.getFullAddress());
		}
	}

}
