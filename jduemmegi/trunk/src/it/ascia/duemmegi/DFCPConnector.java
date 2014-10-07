package it.ascia.duemmegi;

import it.ascia.ais.AISException;
import it.ascia.ais.Connector;
import it.ascia.ais.ConnectorInterface;
import it.ascia.ais.ControllerModule;
import it.ascia.ais.Message;
import it.ascia.duemmegi.domino.DominoDevice;
import it.ascia.duemmegi.domino.device.*;
import it.ascia.duemmegi.fxpxt.FXPXTMessageParser;
import it.ascia.duemmegi.fxpxt.FXPXTRequestMessage;

public class DFCPConnector extends Connector implements ConnectorInterface {

	/**
	 * MessageParser per la lettura dei messaggi in ingresso.
	 */
	protected FXPXTMessageParser mp;
	
	public DFCPConnector(long autoupdate, String name, ControllerModule module) {
		super(autoupdate,name);
		mp = new FXPXTMessageParser();
	}

	public boolean sendMessage(Message m) {
		if (FXPXTRequestMessage.class.isInstance(m)) {
			return sendRequestMessage((FXPXTRequestMessage)m);
		} else {
			try {
				transport.acquire();
				transport.write(m.getBytesMessage());
				transport.release();
				return true;
			} catch (InterruptedException e) {
				logger.error("Interrupted:",e);
				return false;
			}
		}
	}

	public boolean sendRequestMessage(FXPXTRequestMessage m) {
    	boolean received = false;
		try {
			if (!transport.tryAcquire()) {
				logger.trace("Start waiting for transport semaphore ...");
				transport.acquire();
				logger.trace("Done waiting for transport semaphore.");
			}
			if (request != null && FXPXTRequestMessage.class.isInstance(m)) {
				logger.debug("Messaggio gia' inviato in attesa di risposta: "+m);				
				return true;
			}
			request = m;
	    	synchronized (request) {
    			transport.write(m.getBytesMessage());
    			// si mette in attesa, ma se nel frattempo arriva la risposta viene avvisato
    	    	try {
    	    		request.wait((long)(100 * (1 + 0.2 * Math.random())));
    	    	} catch (InterruptedException e) {
    				logger.trace("sendRequestMessage wait:2");
    	    	}
		    	received = m.isAnswered();
		    	request = null;
	    	}
		} catch (InterruptedException e) {
			logger.error("Interrupted:",e);
		}
    	if (! received) {
    		logger.error("Messaggio non risposto: "+m);
    	}
		transport.release();
		return received;
	}


	public void addDevice(String model, String address) throws AISException {
		DominoDevice d = null;
		if (model.equals("DF4I")) {
			d = new DF4I(this, address);
		} else if (model.equals("DF4IV")) {
			d = new DF4IV(this, address);
		} else if (model.equals("DF8IL")) {
			d = new DF8IL(this, address);
		} else if (model.equals("DFIR")) {
			d = new DFIR(address);
		} else if (model.equals("DF4R")) {
			d = new DF4R(address);
		} else if (model.equals("DFTA")) {
			d = new DFTA(address);
		} else if (model.equals("DFTP")) {
			d = new DFTP(address);
		} else if (model.equals("DFCT")) {
			d = new DFCT(this, address);
		} else if (model.equals("DFGSM2")) {
			d = new DFGSM2(address);
		} else if (model.equals("DFDM")) {
			d = new DFDM(address);
		} else if (model.equals("DFDI")) {
			d = new DFDI(address);
		} else if (model.equals("DFDV")) {
			d = new DFDV(address);
		} else if (model.equals("DFTA")) {
			d = new DFTA(address);
		} else {
			logger.error("Modello sconosciuto: "+model);
		}
		if (d != null) {
			super.addDevice(d);
			logger.info("Aggiunto modulo "+model+" "+d.getDeviceAddress());
		}
	}

	@Override
	protected void dispatchMessage(Message m) throws AISException {
		// TODO Auto-generated method stub
		
	}

}
