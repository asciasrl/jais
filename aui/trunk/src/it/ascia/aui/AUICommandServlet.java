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

/**
 * TODO Incorporare in AUIServlet 
 * @author Sergio
 *
 */

public class AUICommandServlet extends HttpServlet {

	private Logger logger;
	private Controller controller;

	public AUICommandServlet()
	{
		logger = Logger.getLogger(getClass());
		controller = Controller.getController();
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
			long start = System.nanoTime();
			String res;
			try {
				res = controller.doCommand(command, params);
				response.setStatus(HttpServletResponse.SC_OK);					
				//logger.trace(res);
			} catch (AISException e) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				logger.error("Errore esecuzione comando "+command,e);
				res = "ERROR: "+e.getMessage();
			}
			long t = (System.nanoTime() - start) / 1000000L; 
			out.println(res);			
			if (res.startsWith("ERROR")) {
				logger.error("Eseguito comando '"+command+"' in "+t+"mS :"+res);
			} else {
				logger.debug("Eseguito comando '"+command+"' in "+t+"mS");
			}
		} catch (IOException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			logger.fatal("Errore gestione richiesta:",e);
		}
	}

}
