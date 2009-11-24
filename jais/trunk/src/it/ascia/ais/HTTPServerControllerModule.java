package it.ascia.ais;

import java.util.Iterator;
import java.util.List;
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
	
	public void start() throws Exception {
		//logger = Logger.getLogger(getClass());
		logger.info("Avvio server HTTP...");
		HierarchicalConfiguration config = getConfiguration();
		int port = config.getInt("port",80);
		String root = config.getString("root","../aui");
		logger.info("Porta="+port+" Root="+root);
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
		Context rootContext = new Context(contexts, "/", Context.SESSIONS);
		rootContext.setResourceBase(root);
		// contesto servito da jspservlet
		JspServlet jspServlet = new JspServlet();
		ServletHolder jspHolder = new ServletHolder(jspServlet);
		rootContext.addServlet(jspHolder, "*.jsp");
		
		// configurazione delle servlet 
		List servlets = config.configurationsAt("servlets.servlet");
		for (Iterator si = servlets.iterator(); si.hasNext();)
		{
			try {
			    HierarchicalConfiguration sub = (HierarchicalConfiguration) si.next();
			    String className = sub.getString("class");
			    String context = sub.getString("context");
				logger.debug("Caricamento servlet '"+context+"' da '"+className+"'");
				ClassLoader moduleLoader = ControllerModule.class.getClassLoader();
			    Class servletClass = moduleLoader.loadClass(className);
			    HttpServlet servlet = (HttpServlet) servletClass.newInstance();
				Context servletContext = new Context(contexts, context, Context.SESSIONS);
				servletContext.setResourceBase(root);
				ServletHolder servletHolder = new ServletHolder(servlet);
				servletContext.addServlet(servletHolder, "/*");
				logger.info("Caricata servlet '"+context+"'");
			} catch (ClassNotFoundException e) {
				logger.fatal("Classe non trovata:",e);
			} catch (InstantiationException e) {
				logger.fatal("Classe non instanziata:",e);
			} catch (IllegalAccessException e) {
				logger.fatal("Classe non accessibile:",e);
			}
		}		
		
		// La fileServlet e il suo contesto
		HttpServlet defaultServlet = new DefaultServlet();
		ServletHolder defaultHolder = new ServletHolder(defaultServlet);
		rootContext.addServlet(defaultHolder, "/");
		try {
			server.start();
			logger.info("Avviato server HTTP");
		} catch (Exception e) {
			logger.fatal("Errore avvio server HTTP: ",e);
			throw(e);
		}		
		super.start();
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
