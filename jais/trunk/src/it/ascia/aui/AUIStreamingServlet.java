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
		try {
			PrintWriter out = response.getWriter();
			response.setContentType("text/html");
			response.setStatus(HttpServletResponse.SC_OK);					
			LinkedBlockingQueue q = new LinkedBlockingQueue();
			AUIControllerModule auiControllerModule = (AUIControllerModule) Controller.getController().getModule("AUI");
			auiControllerModule.addStreamQueue(q);
			
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
			} catch (AISException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			
			//out.println("<!-- Streaming jais events -->");
			while (! out.checkError()) {
				logger.trace("Streaming ...");
				DevicePortChangeEvent evt = null;
				try {
					evt = (DevicePortChangeEvent) q.poll(10,TimeUnit.SECONDS);
				} catch (InterruptedException e) {
					logger.trace(e);
				}
				if (evt != null) {
					//logger.debug(evt.toString());
					// TODO Trasformare in forma JSON
					JSONObject obj=new JSONObject();
					obj.put("fullAddress",evt.getFullAddress());
					obj.put("newValue",evt.getNewValue());
					obj.put("timeStamp",new Long(evt.getTimeStamp()));
					//out.println("<script type=\"text/javascript\" language=\"JavaScript\">");
					out.println("fireDevicePortChangeEvent("+obj.toJSONString()+");");
					//out.println("</script>");
					//out.println(obj.toJSONString());
					logger.trace("Streaming "+evt.toString());
				} else {
					//out.println("<!-- no events -->");
				}
			}
			auiControllerModule.removeStreamQueue(q);
			logger.debug("End streaming ...");
		} catch (IOException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			logger.error(e);
		}
	}
		
	
}
