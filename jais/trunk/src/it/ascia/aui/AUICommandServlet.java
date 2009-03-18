package it.ascia.aui;

import it.ascia.ais.AISException;
import it.ascia.ais.Controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.HashMap;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

public class AUICommandServlet extends HttpServlet {

	private Logger logger;

	public AUICommandServlet()
	{
		logger = Logger.getLogger(getClass());		
	}
	
	/**
	 * Ruota un comando ad un modulo
	 * TODO gestire il routing dinamico, non solo ad AUI
	 * @param command
	 * @param params
	 * @return
	 * @throws AISException 
	 */
	public String doCommand(String command, HashMap params) throws AISException {		
		return ((AUIControllerModule)Controller.getController().getModule("AUI")).doCommand(command, params);
	}
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) {
		PrintWriter out;
		try {
			out = response.getWriter();
			// Ritorniamo sempre & solo testo
			response.setContentType("text/plain");
			// Va sempre tutto bene (gli errori li scriviamo dentro)
			Enumeration parameterNames = request.getParameterNames();
			HashMap params = new HashMap();
			while (parameterNames.hasMoreElements()) {
				String parameterName = (String) parameterNames.nextElement();
				params.put(parameterName, request.getParameter(parameterName));
			}
			String uri = request.getRequestURI();
			int index = uri.lastIndexOf("/");
			String command = "";
			if (index > 0) {
				command = uri.substring(index + 1);
			} else {
				command = uri;
			}
			logger.info("Comando '"+command+"':"+params.toString());
			String res;
			try {
				res = doCommand(command, params);
				response.setStatus(HttpServletResponse.SC_OK);					
				//logger.trace(res);
			} catch (AISException e) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				logger.error("Errore esecuzione comando "+command,e);
				res = "ERROR: "+e.getMessage();
			}
			out.println(res);
		} catch (IOException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			logger.fatal("Errore gestione richiesta:",e);
		}
	}

}
