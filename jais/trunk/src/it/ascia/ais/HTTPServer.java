/**
 * Copyright (C) 2008 ASCIA S.R.L.
 */
package it.ascia.ais;

import javax.servlet.http.HttpServlet;

import it.ascia.ais.AISException;

import org.apache.log4j.Logger;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.ContextHandlerCollection;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.DefaultServlet;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.log.Log;
import org.apache.jasper.servlet.JspServlet;

/**
 *
 * Server HTTP che comunica con AUI.
 * 
 * <p>I suoi compiti sono:</p>
 * <ul>
 *   <li>servire AUI (JSP);</li>
 *   <li>servire i file richiesti da AUI;</li>
 *   <li>rispondere alle richieste di AUI.</li>
 * </ul>
 *   
 * @author arrigo
 */
public class HTTPServer {
	/**
	 * L'handler che serve file per AUI.
	 */
	//private DefaultServlet fileServlet;
	/**
	 * L'handler che serve le richieste di AUI.
	 */
	//private AUIRequestServlet auiRequestServlet;
	/**
	 * Il nostro server.
	 */
	private Server server;
	/**
	 * Il nostro logger.
	 */
	private Logger logger;
	
	/**
	 * Costruttore.
	 * 
	 * @param port porta TCP su cui ascoltare.
	 * @param auiDirectory directory contenente i file richiesti da AUI
	 * 
	 * @throws AISException se qualcosa va storto.
	 */
	public HTTPServer(int port, Controller controller, String auiDirectory) 
		throws AISException {
		// Scegliamo il nostro logger anziche' quello predefinito di Jetty
		System.setProperty("org.mortbay.log.class", "it.ascia.ais.JettyLogger");
		ContextHandlerCollection contexts = new ContextHandlerCollection();
		logger = Logger.getLogger(getClass());
		server = new Server(port);
		server.setHandler(contexts);
		// TODO capire come servire dalla stessa directory sia jsp che files statici (adesso mostra il sorgente!!!)
		// contesto servito da jspservlet
		Context rootContext = new Context(contexts, "/", Context.SESSIONS);
		rootContext.setResourceBase(auiDirectory);
		JspServlet jspServlet = new JspServlet();
		ServletHolder jspHolder = new ServletHolder(jspServlet);
		rootContext.addServlet(jspHolder, "*.jsp");
		// La nostra AUIRequestServlet e il suo contesto.
		HttpServlet auiRequestServlet = new AUIRequestServlet(controller);
		Context jaisContext = new Context(contexts, "/jais", Context.SESSIONS);
		ServletHolder jaisHolder = new ServletHolder(auiRequestServlet);
		jaisContext.addServlet(jaisHolder, "/*");
		// La fileServlet e il suo contesto
		HttpServlet defaultServlet = new DefaultServlet();
		ServletHolder defaultHolder = new ServletHolder(defaultServlet);
		defaultHolder.setInitParameter("resourceBase", auiDirectory);		 
		defaultHolder.setInitParameter("dirAllowed", "false");
		defaultHolder.setInitParameter("redirectWelcome", "false");		 
		rootContext.addServlet(defaultHolder, "/");
		try {
			logger.info("Avvio server HTTP...");
			server.start();
			logger.info("Avviato server HTTP");
		} catch (Exception e) {
			throw new AISException(e.getMessage());
		}
	}

	/**
	 * Ferma il server.
	 */
	public void close() {
		logger.info("Arresto server...");
		try {
			server.stop();
		} catch (Exception e) {
			logger.error("Errore durante l'arresto del server: " +
					e.getMessage());
		}
	}
}
