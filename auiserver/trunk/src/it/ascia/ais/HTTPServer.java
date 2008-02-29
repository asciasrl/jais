/**
 * Copyright (C) 2008 ASCIA S.R.L.
 */
package it.ascia.ais;

import it.ascia.ais.AISException;

import org.apache.log4j.Logger;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.ContextHandlerCollection;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.DefaultServlet;
import org.mortbay.jetty.servlet.ServletHolder;

/**
 *
 * Server HTTP che comunica con AUI.
 * 
 * <p>I suoi compiti sono:</p>
 * <ul>
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
	private DefaultServlet fileServlet;
	/**
	 * L'handler che serve le richieste di AUI.
	 */
	private AUIRequestServlet auiRequestServlet;
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
		Context filesContext, requestsContext;
		ServletHolder holder;
		logger = Logger.getLogger(getClass());
		server = new Server(port);
		// La nostra AUIRequestServlet e il suo contesto.
		auiRequestServlet = new AUIRequestServlet(controller);
		requestsContext = new Context(contexts, "/jeds", Context.SESSIONS);
		holder = new ServletHolder(auiRequestServlet);
		requestsContext.addServlet(holder, "/*");
		// La fileServlet e il suo contesto
		filesContext = new Context(contexts, "/aui", Context.SESSIONS);
		fileServlet = new DefaultServlet();
		holder = new ServletHolder(fileServlet);
		holder.setInitParameter("resourceBase", auiDirectory);
		filesContext.addServlet(holder, "/*");
		server.setHandler(contexts);
		try {
			server.start();
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
