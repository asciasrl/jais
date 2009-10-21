/**
 * 
 */
package it.ascia.aui;

import it.ascia.ais.AISException;
import it.ascia.ais.Controller;
import it.ascia.ais.DevicePort;
import it.ascia.ais.DevicePortChangeEvent;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;

/**
 * TODO Incorporare in AUIServlet
 * @author Sergio
 *
 */
public class AUIStreamingServlet extends HttpServlet {
	
	private static final String AUIModuleName = "AUI";
	
	private Logger logger;
	
	public AUIStreamingServlet()
	{
		logger = Logger.getLogger(getClass());
	}
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) {
		String remote = request.getRemoteAddr()+":"+request.getRemotePort();
		String uri = request.getRequestURI();
		portEventQueue eventQueue = new portEventQueue();
		int index = uri.lastIndexOf("page-");
		String page = "";
		if (index > 0) {
			page = uri.substring(index + 5);
		}		
		int maxEvents = ((AUIControllerModule)Controller.getController().getModule(AUIModuleName)).getMaxEventsPerRequest();
		int counter = 0;
		try {
			PrintWriter out = response.getWriter();
			response.setContentType("text/html");
			response.setStatus(HttpServletResponse.SC_OK);					
			logger.info("Inizio streaming verso "+remote+" page="+page);
			logger.trace("Local address "+request.getLocalAddr()+":"+request.getLocalPort()+" Query:"+request.getQueryString());
			// per prima cosa aggiorna lo stato di tutte le porte
			List ports = getPagePorts(page);
			if (ports.size() < 1) {
				logger.warn("No available DevicePorts on page "+page);
				JSONObject obj=new JSONObject();
				obj.put("ERROR","No available DevicePorts on page "+page);
				out.println(obj.toJSONString());
				maxEvents = 1; // termina quasi subito il ciclo sottostante
			}
			try {
				for (Iterator iterator = ports.iterator(); iterator.hasNext();) {
					DevicePort p = (DevicePort) iterator.next();
					p.addPropertyChangeListener(eventQueue);
					JSONObject obj=new JSONObject();
					Object value = p.getCachedValue();  
					if (value == null) {
						logger.trace("Non invio valore null");
					} else {
						obj.put("A",p.getFullAddress());
						obj.put("V",value.toString());
					}
					out.println(obj.toJSONString());
					logger.trace("Streaming "+remote+" : "+obj.toJSONString());
				}				
			} catch (AISException e) {
				logger.warn("Errore durante streaming con "+remote+":",e);
			}
			
			while (! out.checkError() && counter < maxEvents) {
				DevicePortChangeEvent evt = null;
				// uso poll in modo da inviare sempre qualcosa al client
				evt = (DevicePortChangeEvent) eventQueue.poll(10,TimeUnit.SECONDS);
				JSONObject obj=new JSONObject();
				if (evt != null) {
					if (evt.getNewValue() == null) {
						logger.trace("Non invio evento con valore null: "+evt.toString());
					} else {
						obj.put("A",evt.getFullAddress());
						obj.put("V",evt.getNewValue().toString());
					}
				}
				counter++;
				out.println(obj.toJSONString());
				logger.trace("Streaming "+remote+" ("+counter+"): "+obj.toJSONString());
			}
			for (Iterator iterator = ports.iterator(); iterator.hasNext();) {
				DevicePort p = (DevicePort) iterator.next();
				p.removePropertyChangeListener(eventQueue);
			}
			logger.debug("Fine streaming verso "+remote+" eventi="+counter);
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			logger.debug("Interrotto.");
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
	private List getPagePorts(String page) {
		return ((AUIControllerModule) Controller.getController().getModule(AUIModuleName)).getPagePorts(page);
	}

	private class portEventQueue extends LinkedBlockingQueue implements PropertyChangeListener {

		public void propertyChange(PropertyChangeEvent evt) {
			this.offer(evt);
		}
	}
	
}
