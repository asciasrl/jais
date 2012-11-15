package it.ascia.ais;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Random;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.jasper.servlet.JspServlet;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.session.HashSessionIdManager;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

public class HTTPServerControllerModule extends ControllerModule {

	/**
	 * Il nostro server HTTP Jetty.
	 */
	private Server server;
	
	/**
	 * uses the longest prefix of the request URI (the contextPath) to select a specific ContextHandler to handle the request.
	 */
	private ContextHandlerCollection handlers = new ContextHandlerCollection();
	
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
		server.setHandler(handlers);
		
		try {
			logger.debug("Starting HTTP server: Port="+port);
			server.start();
			server.join();
			logger.info("Avviato server HTTP");
		} catch (Exception e) {
			stop();
			throw(new AISException("Errore avvio server HTTP: ",e));
		}
		super.start();
	}

	/**
	 * Add the handler to the server
	 * Handlers can be added only before starting jetty
	 * @param ContextHandler nuovo handler
	 * @throws IllegalStateException
	 */
	public void addHandler(Handler handler) {
		if (!isStopped()) {
			throw(new IllegalStateException("HTTP server not stopped: cannot add new handler!"));
		}
		handlers.addHandler(handler);
	}

	/**
	 * Add a context to the server with resource base and default servlet
	 * @param contextPath
	 * @param resourceBase
	 * @param options SESSIONS || security
	 * @param withJsp The context must support JSP
	 * @return
	 */
	public ServletContextHandler addContext(String contextPath, String resourceBase, int options, boolean withJsp) {				
        ServletContextHandler context = new ServletContextHandler(options);
        context.setContextPath(contextPath);
        context.setResourceBase(resourceBase);
        
        DefaultServlet defa = new DefaultServlet();
        context.addServlet(new ServletHolder("DEFAULT",defa), "/");
        JspServlet jsp = new JspServlet();
                
        if (withJsp) {
            context.setClassLoader(this.getClass().getClassLoader());
        	context.addServlet(new ServletHolder("JSP",jsp), "*.jsp");        
        }

        addHandler(context);
		return context;
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
	
	public boolean isStopped() {
		return server == null || server.isStopped();
	}
	
}
