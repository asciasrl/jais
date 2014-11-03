package it.ascia.aui;

import it.ascia.ais.Address;
import it.ascia.ais.Controller;
import it.ascia.ais.Device;
import it.ascia.ais.DevicePort;
import it.ascia.ais.DevicePortChangeEvent;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.json.simple.JSONObject;

@WebSocket
public class AUIStreamingWebSocket {

	private static final String AUIModuleName = "AUI";

	private Session session;
	
	private Logger logger = Logger.getLogger(getClass());

	private Vector<DevicePort> streamingPorts = new Vector<DevicePort>();

	private PortEventQueue portEventQueue = new PortEventQueue(); 

	private PortEventWebSocketSender portEventSender = new PortEventWebSocketSender();

	@OnWebSocketConnect
    public void onConnect(Session session) {
        logger.info("Got connect: " + session);
        this.session = session;
        portEventSender.start();
	}
     
	private void streamAll() {
		logger.info("Start sreaming of all ports");
        Vector<Device> devices = Controller.getController().getDevices(new Address());
        for (Device device : devices) {
			Collection<DevicePort> ports = device.getPorts();
			streamPorts(ports);
		}
	}
	
	private void streamPorts(Collection<DevicePort> ports) {
		try {
			for (DevicePort devicePort : ports) {
				if (streamingPorts.contains(devicePort)) {
					continue;
				} else {
					streamingPorts.add(devicePort);
				}
				devicePort.addPropertyChangeListener(portEventQueue);
				Object cachedValue = devicePort.getCachedValue();  
				if (cachedValue == null) {
					logger.trace("Non invio valore null");
				} else {
					JSONObject obj=new JSONObject();
					obj.put("V",cachedValue.toString());
					obj.put("A",devicePort.getAddress().toString());
					obj.put("S",devicePort.getStringValue());
			        session.getRemote().sendString(obj.toJSONString());
				}				
			}		
	    } catch (Throwable t) {
	    	logger.error("Error while sending:", t);
	    }
	}
	
	private void streamNone() {
		synchronized (streamingPorts) {
			for (DevicePort devicePort : streamingPorts) {
				devicePort.removePropertyChangeListener(portEventQueue);			
			}
			streamingPorts.clear();			
		}
	}
	
	private void streamPage(String page) {
		List<DevicePort> ports = ((AUIControllerModule) Controller.getController().getModule(AUIModuleName)).getPagePorts(page);
		streamPorts(ports);
	}
	
	@OnWebSocketMessage
	public void onMessage(Session session, String msg) {
		logger.info("Got msg: " + msg);
		if (msg.equals("all")) {
			streamAll();
		} else if (msg.equals("none")) {
			streamNone();
		} else if (msg.startsWith("page-")) {
			String page = msg.substring(5);
			streamPage(page);
		}
	}
		
	@OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        logger.info("Connection closed: " + statusCode + " - " + reason);
		portEventSender.interrupt();
        this.session = null;
        streamNone();
    	try {
    		portEventSender.join();
		} catch (InterruptedException e) {
			logger.error("Interrupted:",e);
		}

    }

	/**
	 * Receive and queue events
	 * @author sergio
	 *
	 */
	@SuppressWarnings("serial")
	private class PortEventQueue extends LinkedBlockingQueue<DevicePortChangeEvent> implements PropertyChangeListener {

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if (DevicePortChangeEvent.class.isInstance(evt)) {
				this.offer((DevicePortChangeEvent) evt);
			}
		}
		
	}
	
	/**
	 * Send queued events to remote
	 * @author sergio
	 *
	 */
	private class PortEventWebSocketSender extends Thread {
		
		public void run() {
			this.setName(getClass().getSimpleName()+session.getRemoteAddress());
			DevicePortChangeEvent evt = null;
			while (session != null && session.isOpen()) {
				try {
					evt = portEventQueue.poll(10,TimeUnit.SECONDS);
				} catch (InterruptedException e) {
					logger.debug("Interrotto.");
				}
				if (session != null && session.isOpen()) {
					JSONObject obj=new JSONObject();
					if (evt != null) {
						if (evt.getNewValue() != null) {
							obj.put("V",evt.getNewValue().toString());
							obj.put("A",((DevicePortChangeEvent) evt).getFullAddress());
							obj.put("T",((DevicePortChangeEvent) evt).getTimeStamp());
							obj.put("S",((DevicePort)evt.getSource()).getStringValue());
						}
					} else {
						obj.put("T",System.currentTimeMillis());
					}
					try {
						logger.trace("Streaming: " + obj.toJSONString());
						session.getRemote().sendString(obj.toJSONString());
			        } catch (Throwable t) {
			        	logger.error("Error while streaming:", t);
					}
				}
			}			
		}
	}
	
}
