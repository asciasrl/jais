package it.ascia.ais;

import it.ascia.aui.AUIControllerModule;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.HashMap;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.jasper.servlet.JspServlet;
import org.apache.log4j.Logger;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.ContextHandlerCollection;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.DefaultServlet;
import org.mortbay.jetty.servlet.ServletHolder;

public class HTTPServerControllerModule extends ControllerModule {

	/**
	 * Il nostro server HTTP Jetty.
	 */
	private Server server;
	
	public void onDeviceEvent(DeviceEvent event) {
		// TODO Auto-generated method stub
	}

	public void start() {
		//logger = Logger.getLogger(getClass());
		logger.info("Avvio server HTTP...");
		int port = config.getInt("HTTPServer.port",80);
		String root = config.getString("HTTPServer.root","../aui");
		logger.info("Porta="+port+" Root="+root);
		// configurazione livelli di log di Jetty e Jasper
		if (config.getBoolean("HTTPServer.debug", false)) {
			logger.info("Jetty Debug");
			System.setProperty("DEBUG","true");			
		}
		if (config.getBoolean("HTTPServer.verbose", false)) {
			logger.info("Jetty Verbose");
			System.setProperty("VERBOSE","true");			
		}
		server = new Server(port);
		ContextHandlerCollection contexts = new ContextHandlerCollection();
		server.setHandler(contexts);
		// contesto servito da jspservlet
		Context rootContext = new Context(contexts, "/", Context.SESSIONS);
		rootContext.setResourceBase(root);
		JspServlet jspServlet = new JspServlet();
		ServletHolder jspHolder = new ServletHolder(jspServlet);
		rootContext.addServlet(jspHolder, "*.jsp");
		// La nostra AUIRequestServlet e il suo contesto.
		HttpServlet jaisServlet = new HTTPServerControllerModule.JaisServlet();
		Context jaisContext = new Context(contexts, "/jais", Context.SESSIONS);
		ServletHolder jaisHolder = new ServletHolder(jaisServlet);
		jaisContext.addServlet(jaisHolder, "/*");
		// La fileServlet e il suo contesto
		HttpServlet defaultServlet = new DefaultServlet();
		ServletHolder defaultHolder = new ServletHolder(defaultServlet);
		rootContext.addServlet(defaultHolder, "/");
		try {
			server.start();
		} catch (Exception e) {
			logger.fatal("Errore avvio server HTTP: "+e.getMessage());
		}		
		logger.info("Avviato server HTTP");
	}

	public void stop() {
		logger.info("Arresto server HTTP ...");
		try {
			server.stop();
		} catch (Exception e) {
			logger.error("Errore durante l'arresto del server: " +
					e.getMessage());
		}		
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
		return ((AUIControllerModule)controller.getModule("AUI")).doCommand(command, params);
	}
	
	private class JaisServlet extends HttpServlet {
		
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
					logger.trace(res);
				} catch (AISException e) {
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					logger.error(e);
					res = e.getMessage();
				}
				out.println(res);
			} catch (IOException e) {
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				logger.error(e);
			}
		}
	}


}
