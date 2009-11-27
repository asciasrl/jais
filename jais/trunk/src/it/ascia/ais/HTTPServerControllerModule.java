package it.ascia.ais;

import java.util.Random;

import javax.servlet.http.HttpServlet;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.jasper.servlet.JspServlet;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.ContextHandlerCollection;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.DefaultServlet;
import org.mortbay.jetty.servlet.HashSessionIdManager;
import org.mortbay.jetty.servlet.ServletHolder;

public class HTTPServerControllerModule extends ControllerModule {

	/**
	 * Il nostro server HTTP Jetty.
	 */
	private Server server;
	
	private Context rootContext;
	
	public void start() {
		HierarchicalConfiguration config = getConfiguration();
		int port = config.getInt("port",80);
		String root = config.getString("root","web");
		// configurazione livelli di log di Jetty e Jasper
		if (config.getBoolean("debug", false)) {
			logger.info("Jetty Debug");
			System.setProperty("DEBUG","true");			
		}
		if (config.getBoolean("verbose", false)) {
			logger.info("Jetty Verbose");
			System.setProperty("VERBOSE","true");			
		}
		server = new Server(port);
		server.setSessionIdManager(new HashSessionIdManager(new Random()));
		ContextHandlerCollection contexts = new ContextHandlerCollection();
		server.setHandler(contexts);
		rootContext = new Context(contexts, "/", Context.SESSIONS);
		rootContext.setResourceBase(root);
		
		addServlet(new DefaultServlet(), "/");
		addServlet(new JspServlet(), "*.jsp");

		try {
			logger.info("Starting HTTP server: Port="+port+" Root="+root);
			server.start();
			logger.info("Avviato server HTTP");
		} catch (Exception e) {
			logger.fatal("Errore avvio server HTTP: ",e);
			return;
		}		
		super.start();
	}

	public void addServlet(HttpServlet servlet, String path) {
		rootContext.addServlet(new ServletHolder(servlet), path);
		logger.info("Added servlet "+servlet.getClass().getCanonicalName()+", path '"+path+"'");		
	}

	public void stop() {
		super.stop();
		logger.debug("Arresto server HTTP ...");
		try {
			server.stop();
			logger.debug("Arrestato server HTTP.");
		} catch (Exception e) {
			logger.error("Errore durante l'arresto del server: ", e);
		}	
	}
	
}
