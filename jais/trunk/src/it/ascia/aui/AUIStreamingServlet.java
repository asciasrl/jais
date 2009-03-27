/**
 * 
 */
package it.ascia.aui;

import it.ascia.ais.AISException;
import it.ascia.ais.Controller;
import it.ascia.ais.Device;
import it.ascia.ais.DevicePort;
import it.ascia.ais.DevicePortChangeEvent;
import java.io.IOException;
import java.io.PrintWriter;
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
public class AUIStreamingServlet extends HttpServlet {
	
	private Logger logger;

	public AUIStreamingServlet()
	{
		logger = Logger.getLogger(getClass());
	}
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) {
		String remote = request.getRemoteAddr()+":"+request.getRemotePort();
		int counter = ((AUIControllerModule)Controller.getController().getModule("AUI")).getMaxEventsPerRequest();
		try {
			PrintWriter out = response.getWriter();
			response.setContentType("text/html");
			response.setStatus(HttpServletResponse.SC_OK);					
			LinkedBlockingQueue q = new LinkedBlockingQueue();
			AUIControllerModule auiControllerModule = (AUIControllerModule) Controller.getController().getModule("AUI");
			auiControllerModule.addStreamQueue(q);
			logger.info("Inizio streaming verso "+remote);
			
			// per prima cosa aggiorna lo stato di tutte le porte
			// TODO aggiornare solo lo stato delle porte richieste
			Device[] devices;
			try {
				devices = Controller.getController().findDevices("*");
				for (int i = 0; i < devices.length; i++) {
					DevicePort[] devicePorts = devices[i].getPorts();
					for (int j = 0; j < devicePorts.length; j++) {
						DevicePort devicePort = devicePorts[j];
						JSONObject obj=new JSONObject();
						obj.put("fullAddress",devicePort.getFullAddress());
						obj.put("newValue",devicePort.getValue());
						obj.put("timeStamp",new Long(devicePort.getTimeStamp()));
						out.println("fireDevicePortChangeEvent("+obj.toJSONString()+");");
					}
				}
			} catch (AISException e) {
				logger.warn("Errore durante streaming con "+remote+":",e);
			}
			
			while (! out.checkError() && counter > 0) {
				logger.trace("Streaming "+remote);
				DevicePortChangeEvent evt = null;
				try {
					// uso poll in modo da inviare sempre qualcosa al client
					evt = (DevicePortChangeEvent) q.poll(10,TimeUnit.SECONDS);
				} catch (InterruptedException e) {
					logger.trace("Interrupted:",e);
				}
				if (evt != null) {
					logger.trace("Streaming "+evt.toString());
					if (evt.getNewValue() == null) {
						logger.trace("Non invio evento con valore null");
					} else {
						JSONObject obj=new JSONObject();
						obj.put("fullAddress",evt.getFullAddress());
						obj.put("newValue",evt.getNewValue());
						obj.put("timeStamp",new Long(evt.getTimeStamp()));
						String s = "fireDevicePortChangeEvent("+obj.toJSONString()+");";
						out.println(s);
						logger.debug(remote +" "+s);
						counter--;
					}
				} else {
					out.println("void(0);");
				}
			}
			auiControllerModule.removeStreamQueue(q);
			logger.debug("Fine streaming verso "+remote+" eventi="+counter);
		} catch (IOException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			logger.error("Errore interno durante streaming verso "+remote+" :",e);
		}
	}
	
}
