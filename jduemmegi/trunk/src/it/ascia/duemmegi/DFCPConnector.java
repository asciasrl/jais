package it.ascia.duemmegi;

import java.util.Collection;
import java.util.HashMap;

import it.ascia.ais.AISException;
import it.ascia.ais.ConnectorInterface;
import it.ascia.ais.Device;
import it.ascia.ais.DevicePort;
import it.ascia.ais.Message;
import it.ascia.ais.PollingConnectorImpl;
import it.ascia.ais.ResponseMessage;
import it.ascia.ais.port.BooleanPort;
import it.ascia.duemmegi.domino.DominoDevice;
import it.ascia.duemmegi.domino.device.DF8IL;
import it.ascia.duemmegi.fxpxt.FXPXTMessageParser;
import it.ascia.duemmegi.fxpxt.FXPXTRequestMessage;
import it.ascia.duemmegi.fxpxt.ReadInputsRequestMessage;
import it.ascia.duemmegi.fxpxt.ReadOutputsRequestMessage;
import it.ascia.duemmegi.fxpxt.ReadOutputsResponseMessage;
import it.ascia.duemmegi.fxpxt.ReadRamRequestMessage;
import it.ascia.duemmegi.fxpxt.ReadRamResponseMessage;

public class DFCPConnector extends PollingConnectorImpl implements ConnectorInterface {

	/**
	 * MessageParser per la lettura dei messaggi in ingresso.
	 */
	protected FXPXTMessageParser mp;

	protected FXPXTRequestMessage request;
	
	protected long timeout = 5000;

	/**
	 * 
	 * @param name Name of this instance
	 * @param indirizzo DFCP ADDRESS = (1)
	 * @param autoupdate polling cycle in mS
	 */
	public DFCPConnector(String name, int indirizzo, long autoupdate) {
		super(name,autoupdate);
		this.indirizzo = indirizzo;
		mp = new FXPXTMessageParser();
	}
	
	/**
	 * Address of DFCP
	 */
	private int indirizzo;


	/*
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
	*/
	public void discover() {
		for (int address = 1; address <= 255; address++) {
			request = new ReadRamRequestMessage(indirizzo, 0xE800 + address*4, 4);
			if (sendMessage()) {
				ReadRamResponseMessage response = (ReadRamResponseMessage)request.getResponse();
				int modelId = response.getData()[0];
				addDevice(modelId, "i" + address);
				
			}
			
		}
		for (int address = 1; address <= 255; address++) {
			request = new ReadRamRequestMessage(indirizzo, 0xEC00 + address*4, 4);
			if (sendMessage()) {
				ReadRamResponseMessage response = (ReadRamResponseMessage)request.getResponse();
				int modelId = response.getData()[0];
				addDevice(modelId, "o" + address);				
			}
			
		}
		
	}


	private void addDevice(int modelId, String address) {
		if (getDevice(address) != null) {
			logger.info("Skip already existing device "+address);
			return;
		}
		switch (modelId) {
		case 0:
			// no device
			break;
		case 51:
			addDevice("DFDI",address);
			break;		
		case 65:
			addDevice("DF4I",address);
			break;
		case 69:
			addDevice("DF4I/V",address);
			break;
		case 68:
			addDevice("DFTP/I",address);
			break;
		case 82:
			addDevice("DF4RP",address);
			break;						
		case 133:
			addDevice("DF8IL",address);
			break;
		case 243:
			addDevice("DFDV",address);
			break;		
		default:
			logger.error("Unknow device: modelId="+modelId+" address="+address);
		}
	}

	public DominoDevice addDevice(String model, String address) throws AISException {
		logger.trace("Adding module model="+model+" address="+address);
		DominoDevice d = DominoDevice.CreateDevice(model, address, this);
		if (d != null) {
			super.addDevice(d);
			logger.info("Added module: "+d.toString());
		} else {
			logger.error("Unknow model: "+model+ " address:"+address);
		}
		return d;
	}


	@Override
	public void doUpdate() {
		logger.debug("Start polling cycle");
		long startTimeMillis = System.currentTimeMillis();
		int step = 31;
		for (int i = 1; i < 256; i+=step) {
			request = new ReadOutputsRequestMessage(indirizzo,i, step);
			if (sendMessage()) {
				processRequest();
			}
		}
		for (int i = 1; i < 256; i+=step) {
			request = new ReadInputsRequestMessage(indirizzo,i, step);
			sendMessage();			
		}
		logger.trace("Completed polling cycle in "+(System.currentTimeMillis() - startTimeMillis) + "mS");
	}
	
	/**
	 * Send the message store in this.request 
	 * @return true if the request was answered
	 */	
	private boolean sendMessage() {
		boolean res = false;
		try {
			if (!transport.tryAcquire()) {
				logger.trace("Start waiting for transport semaphore ...");
				transport.acquire();
				logger.trace("Done waiting for transport semaphore.");
			}
	    	synchronized (request) {
	    		long startTimeMillis = System.currentTimeMillis();
				transport.write(request.getBytesMessage());
				logger.trace("Request:" + request);
				if (!request.isAnswered()) {
	    			// si mette in attesa, ma se nel frattempo arriva la risposta viene avvisato
	    	    	try {
	    	    		request.wait(timeout);
	    	    	} catch (InterruptedException e) {
	    				logger.trace("interrupted!");
	    	    	}
				}
				if (request.isAnswered()) {
					res=true;
					logger.trace("Response received in "+(System.currentTimeMillis() - startTimeMillis) + "mS: " + request.getResponse());
				} else {
					logger.error("Response not received to: "+request);
				}
	    	}
		} catch (InterruptedException e) {
			logger.debug("Interrupted:",e);
		} catch (Exception e) {
			logger.error("Exception:",e);
		}
		transport.release();
		return res;
	}

	public void received(int b) {
		if (mp == null) {
			return;
		}
		mp.push(b);
		if (mp.isValid()) {
			Message m = mp.getMessage();
			//logger.debug("Ricevuto " + m);
	    	if (request != null 
	    			&& ResponseMessage.class.isInstance(m) 
	    			&& request.isAnsweredBy((ResponseMessage)m)) {
				// sveglia sendMessage
				synchronized (request) {
					request.setResponse((ResponseMessage)m);
					((ResponseMessage)m).setRequest(request);
		    		request.setAnswered(true);
					request.notify(); 						
				}
	    	}
		}
	}

	/**
	 * Process the response received with the request
	 */
	private void processRequest() {
		ResponseMessage response = request.getResponse();
		if (ReadOutputsResponseMessage.class.equals(response.getClass())) {
			HashMap<Integer, Integer[]> outputs = ((ReadOutputsResponseMessage)response).getOutputs();
			for (Integer addr : outputs.keySet()) {
				String deviceAddress = "o" + addr.toString();
				Device device = getDevice(deviceAddress);
				if (device == null) {
					continue;
				}
				Integer[] values = outputs.get(addr);
				Collection<DevicePort> ports = device.getPorts();
				for (int i = 1; i <= ports.size(); i++) {
					String portId = deviceAddress + "." + i;
					try {
						DevicePort port = device.getPort(portId);						
						// digital output
						if (BooleanPort.class.isInstance(port)) {
							Boolean newValue = ((values[1] >> (i-1)) & 0x01) == 1;
							logger.trace("deviceAddress="+deviceAddress+" portId="+portId+" newValue="+newValue);
							port.setValue(newValue);
						} 					
					} catch (Exception e) {
						continue;  // FIXME gestire le porte in maniera programmatica
					}					
				}
			}
		}
	}

}
