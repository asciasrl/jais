/**
 * Copyright (C) 2008 ASCIA S.R.L.
 */
package it.ascia.eds;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.ContextHandlerCollection;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.DefaultServlet;
import org.mortbay.jetty.servlet.ServletHolder;

/**
 * @author arrigo
 *
 * Server HTTP che comunica con AUI.
 * 
 * I suoi compiti sono:
 * 
 *   * servire i file richiesti da AUI;
 *   * rispondere alle richieste di AUI.
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
	 * Costruttore.
	 * 
	 * @param port porta TCP su cui ascoltare.
	 * @param auiDirectory directory contenente i file richiesti da AUI
	 * 
	 * @throws un'EDSException se qualcosa va storto.
	 */
	public HTTPServer(int port, BusController controller, String auiDirectory) 
		throws EDSException {
		ContextHandlerCollection contexts = new ContextHandlerCollection();
		Context filesContext, requestsContext;
		ServletHolder holder;
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
			throw new EDSException(e.getMessage());
		}
	}

	/**
	 * Ferma il server.
	 */
	public void close() {
		try {
			server.stop();
		} catch (Exception e) {
			System.err.println("Errore durante l'arresto del server: " +
					e.getMessage());
		}
	}
}
