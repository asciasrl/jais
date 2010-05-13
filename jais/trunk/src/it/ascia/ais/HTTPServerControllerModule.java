package it.ascia.ais;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
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
	
	private ContextHandlerCollection contexts = new ContextHandlerCollection();
	
	public void start() {
		HierarchicalConfiguration config = getConfiguration();
		int port = config.getInt("port",80);
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
		
		try {
			Enumeration<java.net.NetworkInterface> ifaces = java.net.NetworkInterface.getNetworkInterfaces();
			String portSpec = port == 80 ? "" : ":"+port;
			while (ifaces.hasMoreElements()) {
				NetworkInterface iface = ifaces.nextElement();
				logger.debug(iface.getName() + " " + iface.getDisplayName()+ " " + iface.getInterfaceAddresses());
				Enumeration<InetAddress> addresses = iface.getInetAddresses();
				while (addresses.hasMoreElements()) {
					InetAddress address = addresses.nextElement();
					logger.info("Server address http://"+address.getHostAddress()+portSpec+"/");
				}
			}
		} catch (SocketException e1) {
			logger.error(e1);
		} 
		
		server.setSessionIdManager(new HashSessionIdManager(new Random()));
		server.setHandler(contexts);
		
		try {
			logger.debug("Starting HTTP server: Port="+port);
			server.start();
			logger.info("Avviato server HTTP");
		} catch (Exception e) {
			logger.fatal("Errore avvio server HTTP: ",e);
			return;
		}		
		super.start();
	}

	/**
	 * Add a context with a new session handler
	 * Context can be added only before starting jetty
	 * @param contextPath
	 * @return the new context
	 */
	public Context addContext(String contextPath) {
		if (server != null && !server.isStopped()) {
			logger.fatal("HTTP server not stopped: cannot add new context!");
			return null;
		}
		Context context = new Context(contexts, contextPath, Context.SESSIONS);		
		logger.trace("Added context '"+context.getContextPath()+"'");
		return context;
	}

	/**
	 * Add a context with default & jsp servlet
	 * Servlet should be added before starting jetty
	 * @param contextPath
	 * @param resourceBase
	 * @return
	 */
	public Context addContext(String contextPath, String resourceBase) {
		Context context = addContext(contextPath);
		if (context != null) {
			context.setResourceBase(resourceBase);
			addServlet(new DefaultServlet(), context, "/");
			addServlet(new JspServlet(), context, "*.jsp");
		}
		return context;
	}	
	
	/**
	 * Add a servlet to a context
	 * @param servlet
	 * @param context
	 * @param pathSpec
	 */
	public void addServlet(HttpServlet servlet, Context context, String pathSpec) {
		if (server != null && !server.isStopped()) {
			logger.warn("HTTP server not stopped: add servlet before starting server!");
		}
		context.addServlet(new ServletHolder(servlet), pathSpec);
		logger.info("Added servlet "+servlet.getClass().getCanonicalName()+", context '"+context.getContextPath()+"', path '"+pathSpec+"'");		
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
