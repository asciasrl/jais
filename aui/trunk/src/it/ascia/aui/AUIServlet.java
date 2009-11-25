/**
 * (C) 2009 Ascia S.r.l.
 */
package it.ascia.aui;

import it.ascia.ais.AISException;
import it.ascia.ais.Controller;
import it.ascia.ais.DevicePort;
import it.ascia.ais.DevicePortChangeEvent;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.apache.log4j.Logger;
import org.jabsorb.JSONRPCServlet;
import org.jabsorb.callback.InvocationCallback;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Servlet che gestisce le richieste per il modulo AUI
 * 
 * @author Sergio
 * 
 */
public class AUIServlet extends JSONRPCServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6492640894329160081L;

	private static final String AUIModuleName = "AUI";

    protected Logger logger;

	protected AUIControllerModule aui;
	
    private static InvocationCallback cb = new AUIInvocationCallback();
	
	public AUIServlet() {
		logger = Logger.getLogger(getClass());
		aui = (AUIControllerModule) Controller.getController().getModule(AUIModuleName);
	}
	
	public void service(HttpServletRequest request,HttpServletResponse response) throws IOException {
		HttpSession session = request.getSession();
	
		String uri = request.getRequestURI();
		int index = uri.lastIndexOf("/");
		String command = "";
		if (index > 0) {
			command = uri.substring(index + 1);
		} else {
			command = uri;
		}
		logger.info("Comando '"+command+"'");
		
		if (command.equals("rpc")) {
			/*
			JSONRPCBridge bridge = (JSONRPCBridge) session.getAttribute("JSONRPCBridge");
			if (bridge == null)
			{
			  bridge = new JSONRPCBridge();
			  bridge.registerCallback(cb, HttpServletRequest.class);
			  session.setAttribute("JSONRPCBridge", bridge);
			  logger.debug("Instantiated session JSONRPCBridge");
			}
			*/
			super.service(request, response);
		} else if (command.equals("stream")) {
			doStream(request, response);
		} else if (command.equals("upload")) {
			doUpload(request,response);
		}
	}
	
	private void doUpload(HttpServletRequest request,HttpServletResponse response) throws IOException {
		JSONObject res = new JSONObject();
		try {
			// Check that we have a file upload request
			if (!ServletFileUpload.isMultipartContent(request)) {
				logger.error(AUIControllerModule.messages.getString("NotFileUploadRequest"));
				throw(new AISException(AUIControllerModule.messages.getString("NotFileUploadRequest")));
			}
			if (!isLogged(request.getSession(false))) {
				logger.error(AUIControllerModule.messages.getString("UserNotLogged"));
				throw(new AISException(AUIControllerModule.messages.getString("UserNotLogged")));
			}
			
			// Create a new file upload handler
			ServletFileUpload upload = new ServletFileUpload();
	
			// Parse the request
			FileItemIterator iter = upload.getItemIterator(request);
			// Process the uploaded items
			JSONArray ja = new JSONArray();
			res.put("status", "OK");
			while (iter.hasNext()) {
			    FileItemStream item = (FileItemStream) iter.next();
	        	InputStream stream = item.openStream();
	            String name = item.getFieldName();
			    if (item.isFormField()) {
			    	logger.debug("Field: "+name+"="+Streams.asString(stream));
			    } else {
			        String pathname = aui.getImagesPath() + new File(item.getName()).getName(); 
			        /*
			        pathname += randomUUID.getString();
			        if (item.getContentType().equals("image/png")) {
			        	pathname += ".png";
			        } else if (item.getContentType().equals("image/jpeg")) {
			        	pathname += ".jpg";
			        } else if (item.getContentType().equals("image/gif")) {
			        	pathname += ".gif";
			        }
			        */
			        String realpathname = getServletContext().getRealPath(pathname);
			        logger.debug("fieldName=" + name + " fileName=" + item.getName() + " contentType=" + item.getContentType()+ " url="+pathname + " path=" + realpathname);
			        if (new File(realpathname).exists()) {
			        	res.append("errors",String.format(AUIControllerModule.messages.getString("FileExists"), pathname));
			        } else {
				        FileOutputStream uploadedFile = new FileOutputStream(realpathname);
				        ja.put(realpathname);
				        try {
				        	long size = Streams.copy(stream, uploadedFile, true);
				        	logger.debug("Size: "+size+" Bytes");
							res.append("files",pathname);
						} catch (Exception e) {
							logger.error(e);
							res.append("errors",e.getLocalizedMessage());
						}
			        }
			    }
			}
		} catch (Exception e) {
			logger.error("Upload error: ",e);
			try {
				res.append("errors", e.getLocalizedMessage());
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
		}
		if (!res.has("files")) {
			try {
				res.put("status", "ERROR");
				res.append("errors",AUIControllerModule.messages.getString("NoFileUploaded"));
			} catch (JSONException e) {
			}			
		}
		if (res.has("errors")) {
			try {
				res.put("status", "ERROR");
			} catch (JSONException e) {
			}
		}
		response.setContentType("text/plain");
		response.getWriter().println(res);
		logger.debug("End upload:"+res);
	}
	
	private void doStream(HttpServletRequest request, HttpServletResponse response) {
		String remote = request.getRemoteAddr()+":"+request.getRemotePort();
		String uri = request.getRequestURI();
		portEventQueue eventQueue = new portEventQueue();
		int index = uri.lastIndexOf("page-");
		String page = "";
		if (index > 0) {
			page = uri.substring(index + 5);
		}		
		int maxEvents = aui.getMaxEventsPerRequest();
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
				out.println(obj.toString());
				maxEvents = 1; // termina quasi subito il ciclo sottostante
			}
			try {
				for (Iterator iterator = ports.iterator(); iterator.hasNext();) {
					DevicePort p = (DevicePort) iterator.next();
					p.addPropertyChangeListener(eventQueue);
					JSONObject obj=new JSONObject();
					Object value = p.getCachedValue();  
					if (value == null) {
						logger.trace("Non invio valore null di "+p.getFullAddress());
					} else {
						obj.put("A",p.getFullAddress());
						obj.put("V",value.toString());
					}
					out.println(obj.toString());
					logger.trace("Streaming "+remote+" : "+obj.toString());
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
				out.println(obj.toString());
				logger.trace("Streaming "+remote+" ("+counter+"): "+obj.toString());
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
		} catch (JSONException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			logger.error("Errore JSON durante streaming verso "+remote+" :",e);
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
	
	/**
	 * 
	 * @param session
	 * @return
	 * 
	 * FIXME Metodo duplicato in AUIRPCServer
	 */
	private boolean isLogged(HttpSession session) {
		if (session == null) {
			logger.warn("No session");
			return false;
		}
		String username = (String) session.getAttribute("AUI.username");
		Boolean logged = (Boolean) session.getAttribute("AUI.logged");
		if (username != null && logged != null && logged.booleanValue()) {
			logger.debug("Session "+session.getId()+" "+session.getServletContext()+": Authenticated user '"+username+"'");
			return true;
		}
		logger.debug("Session "+session.getId()+": Not authenticated"); 
		return false;
	}



}
