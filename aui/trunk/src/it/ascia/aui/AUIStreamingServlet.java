/**
 * 
 */
package it.ascia.aui;

import it.ascia.ais.AISException;
import it.ascia.ais.Controller;
import it.ascia.ais.Device;
import it.ascia.ais.DevicePort;
import it.ascia.ais.DevicePortChangeEvent;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;

/**
 * @author Sergio
 *
 */
public class AUIStreamingServlet extends HttpServlet implements PropertyChangeListener {
	
	private Logger logger;
	
	private LinkedBlockingQueue eventQueue;

	public AUIStreamingServlet()
	{
		logger = Logger.getLogger(getClass());
		eventQueue = new LinkedBlockingQueue();
	}
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) {
		String remote = request.getRemoteAddr()+":"+request.getRemotePort();
		String uri = request.getRequestURI();
		int index = uri.lastIndexOf("page-");
		String page = "";
		if (index > 0) {
			page = uri.substring(index + 5);
		}		
		int maxEvents = ((AUIControllerModule)Controller.getController().getModule("AUI")).getMaxEventsPerRequest();
		int counter = 0;
		try {
			PrintWriter out = response.getWriter();
			response.setContentType("text/html");
			response.setStatus(HttpServletResponse.SC_OK);					
			logger.info("Inizio streaming verso "+remote+" page="+page);
			
			// per prima cosa aggiorna lo stato di tutte le porte
			// TODO aggiornare solo lo stato delle porte richieste
			DevicePort[] ports = getPagePorts(page);
			if (ports.length < 1) {
				logger.warn("No available DevicePorts on page "+page);
			}
			try {
				for (int i = 0; i < ports.length; i++) {
					DevicePort p = ports[i];
					p.addPropertyChangeListener(this);
					JSONObject obj=new JSONObject();
					Object value = p.getCachedValue();  
					if (value == null) {
						logger.trace("Non invio valore null");
					} else {
						obj.put("A",p.getFullAddress());
						obj.put("V",value.toString());
					}
					out.println(obj.toJSONString());
				}				
			} catch (AISException e) {
				logger.warn("Errore durante streaming con "+remote+":",e);
			}
			
			while (! out.checkError() && counter < maxEvents) {
				logger.trace("Streaming "+remote);
				DevicePortChangeEvent evt = null;
				try {
					// uso poll in modo da inviare sempre qualcosa al client
					evt = (DevicePortChangeEvent) eventQueue.poll(10,TimeUnit.SECONDS);
				} catch (InterruptedException e) {
					logger.trace("Interrupted:",e);
				}
				JSONObject obj=new JSONObject();
				if (evt != null) {
					logger.trace("Streaming "+evt.toString());
					if (evt.getNewValue() == null) {
						logger.trace("Non invio evento con valore null");
					} else {
						obj.put("A",evt.getFullAddress());
						obj.put("V",evt.getNewValue().toString());
					}
				}
				counter++;
				out.println(obj.toJSONString());
			}
			for (int i = 0; i < ports.length; i++) {
				DevicePort p = ports[i];
				p.removePropertyChangeListener(this);
			}
			logger.debug("Fine streaming verso "+remote+" eventi="+counter);
		} catch (IOException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			logger.error("Errore interno durante streaming verso "+remote+" :",e);
		}
	}
	
	/**
	 * Fornisce tutte le porte presenti su una Page
	 * @param page
	 * @return
	 */
	private DevicePort[] getPagePorts(String page) {
		List ports = new ArrayList();
		try {
			Device[] devices = Controller.getController().findDevices("*");
			for (int i = 0; i < devices.length; i++) {
				DevicePort[] devicePorts = devices[i].getPorts();
				for (int j = 0; i < devicePorts.length; j++) {			
					ports.add(devicePorts[j]);
				}
			}
		} catch (AISException e) {
			logger.error("Error getting page ports: " + e);
		}
		if (ports.size() > 0) {
			return (DevicePort[]) ports.toArray();
		} else {
			return new DevicePort[0];			
		}
	}

	public void propertyChange(PropertyChangeEvent evt) {
		eventQueue.offer(evt);
	}
	
}
