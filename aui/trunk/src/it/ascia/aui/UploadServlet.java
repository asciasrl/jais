/**
 * (C) 2010 Ascia S.r.l.
 */
package it.ascia.aui;

import it.ascia.ais.AISException;
import it.ascia.ais.Controller;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Servlet che gestisce le richieste per il modulo AUI
 * 
 * @author Sergio
 * 
 */
public class UploadServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6492640894329160081L;

	private static final String AUIModuleName = "AUI";

    protected Logger logger;

	protected AUIControllerModule aui;
	
	public UploadServlet() {
		logger = Logger.getLogger(getClass());
		aui = (AUIControllerModule) Controller.getController().getModule(AUIModuleName);
	}
	
	public void service(HttpServletRequest request,HttpServletResponse response) throws IOException {
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
	
	/**
	 * 
	 * @param session
	 * @return
	 * 
	 * FIXME Metodo duplicato in AUIRPCServer
	 */
	private boolean isLogged(HttpSession session) {
		return ((AUIControllerModule) Controller.getController().getModule(AUIModuleName)).isLogged(session);
	}

}
