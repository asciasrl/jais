package it.ascia.bentel;

import java.util.Vector;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import it.ascia.ais.AISException;
import it.ascia.ais.Connector;
import it.ascia.ais.ControllerModule;
import it.ascia.ais.Message;
import it.ascia.bentel.msg.BentelKyoMessage;
import it.ascia.bentel.msg.ReadRealtimeStatusMessage;
import it.ascia.bentel.msg.ReadTypeVersionMessage;
import it.ascia.bentel.msg.ReadZonesDescriptionsMessage;

public class BentelKyoConnector extends Connector {
	
	private long t = 0;
	private long n = 0;
	private StringBuffer sb = new StringBuffer();
	private StringBuffer sbt = new StringBuffer();
	private LinkedBlockingQueue<Integer> buffer = new LinkedBlockingQueue<Integer>();
	private BentelKyoMessage message;
	private int tries;
	private int maxTries = 3;
	private long retryTimeout = 300;
	private long guardTime = 50;
	
	private BentelKyoUnit panel;
	
	public BentelKyoConnector(String name) {
		super(name);		
	}

	@Override
	protected void dispatchMessage(Message m) throws AISException {
		// TODO Auto-generated method stub

	}

	@Override
	public void received(int b) {
		long t1 = System.currentTimeMillis();
		if (t == 0) {
			t = t1;
		} else if ((t1 - t) > 50 && n > 0) {
			log();
		}
		sb.append(Message.b2h(b)+" ");
		sbt.append((char)b);
		//log();
		n++;
		t = t1;
		//logger.trace("Buffer offering ("+buffer.size()+"+1) " + Message.b2h(b));
		buffer.offer(b);
	}
	
	public void log() {
		log("");
	}
	
	public void log(String prefix) {
		t = System.currentTimeMillis();
		logger.trace(prefix + " " + transport.getInfo()+" "+ n +": "+sb.toString()+" '"+sbt.toString()+"'");
		sb.setLength(0);
		sbt.setLength(0);
		n = 0;
	}

	@Override
	public boolean sendMessage(Message m) {
		if (BentelKyoMessage.class.isInstance(m)) {
			return sendMessage((BentelKyoMessage)m);
		} else {
			logger.error("Cannot send message of this class: " + m.getClass().getName());
			return false;
		}
	}
	
	/**
	 * Send message and wait for echo and response
	 * @param m
	 * @return true if received a correct response
	 */
	public boolean sendMessage(BentelKyoMessage m) {
		message = m;
		synchronized (message) {
			// aspetta di ricevere il feedback
			tries = 0;
			long timeout;
			Vector<Integer> temp = new Vector<Integer>();
			while (tries < maxTries ) {
				if (buffer.size() > 0) {
					logger.warn("Clearing buffer: " + buffer.size() + " bytes");
					buffer.clear();
				}
				logger.trace("Waiting guard time before to send to "+transport);
				try {
					Thread.sleep(guardTime);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				logger.trace("Sending to:"+transport+" message:"+message);
				transport.write(message.getBytesMessage());
				tries++;
				timeout = System.currentTimeMillis() + retryTimeout;
				temp.clear();
				while ((temp.size() < message.getRequestSize()) && (timeout > System.currentTimeMillis())) {
			    	try {
			    		Integer b = buffer.poll(retryTimeout, TimeUnit.MILLISECONDS);
			    		if (b != null) {
			    			temp.add(b);
			    			//logger.trace("Buffer poll ("+buff.size()+") " + Message.b2h(b));
			    		}
			    	} catch (InterruptedException e) {
						logger.trace("sendMessage interrupted");
			    	}	
				}
				if (temp.size() == message.getRequestSize()) {
					if (message.checkEcho(temp)) {
						logger.trace("Message echo received");
						message.setSent();
					} else {
						logger.error("Echo incorrect.");
						continue;
					}
				} else {
					logger.error(" No echo received.");
					continue;
				}
				// legge la risposta
				temp.clear();
				timeout = System.currentTimeMillis() + message.getResponseTimeout();
				while ((temp.size() < message.getResponseSize()) && (timeout > System.currentTimeMillis())) {
			    	try {
			    		Integer b = buffer.poll(message.getResponseTimeout(), TimeUnit.MILLISECONDS);
			    		if (b != null) {
			    			temp.add(b);
			    			//logger.trace("Buffer poll ("+buff.size()+") " + Message.b2h(b));
			    		}
			    	} catch (InterruptedException e) {
						logger.trace("sendMessage interrupted");
			    	}	
				}
				if (temp.size() == message.getResponseSize()) { 
					try {
						message.loadResponse(temp);
					} catch (AISException e) {
						logger.error("loadResponse: ",e);
						continue;
					}
				} else {
					logger.error("Incorrect response: "+temp);
					continue;
				}
				// successful end of send cycle
				break;
			}
		}
		if (!message.isSent()) {
			logger.error("Cannot send message");
			return false;
		}
		if (!message.isResponded()) {
			logger.error("Cannot send message");
			return false;
		}
		logger.debug(message);
		return true;
	}

	/**
	 * Discover which panel in connected
	 */
	public void discoverPanel() {
		ReadTypeVersionMessage mr = new ReadTypeVersionMessage(); 
 		if (sendMessage(mr)) {
 			String type = mr.getType();
 			String version = mr.getVersion();
 			logger.info("Control panel type: "+type+" Firmware version: "+version);
 			
 			panel = BentelKyoUnitPanelFactory.getPanel(type, version);
 			for (int i = 1; i <= panel.maxZones(); i++) {
 				addDevice(new ZoneDevice("Zone"+i));
 			}
 			
 			for (int i = 1; i <= panel.maxPartitions(); i++) {
 				addDevice(new PartitionDevice("Partition"+i));
 			}
 			
			addDevice(new OutputsDevice("Outputs",panel.maxOutputs()));

 			/*
 			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			*/
 			//sendMessage(new ReadRealtimeStatusMessage());
 			/*
 			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			*/
 		}			
	}

	/**
	 * Perform an update of panel's real time status
	 */
	protected boolean updateRealTime() {
		return panel.updateRealTime(this);
	}

	/**
	 * Perform an update of panel's status
	 */
	protected boolean updateStatus() {
		return panel.updateStatus(this);
	}

	/**
	 * Perform an update of panel's zone descriptions 
	 */
	public boolean updateZonesDescriptions() {
		return panel.updateZonesDescriptions(this);
	}

	/**
	 * Perform an update of panel's partitions descriptions 
	 */
	public boolean updatePartitionsDescriptions() {
		return panel.updatePartitionsDescriptions(this);
	}

}
