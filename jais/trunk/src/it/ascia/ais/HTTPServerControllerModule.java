package it.ascia.ais;

import java.io.PrintStream;
import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;

import org.apache.commons.configuration.XMLConfiguration;
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
	
	/**
	 * Il nostro PIN.
	 */
	private String pin;
	
	
	public void configure(XMLConfiguration config) {
		super.configure(config);
		pin = config.getString("AUI.pin");
	}

	public void onDeviceEvent(DeviceEvent event) {
		// TODO Auto-generated method stub
	}

	public void start() {
		int port = config.getInt("HTTPServer.port",80);
		String root = config.getString("HTTPServer.root","../aui");
		// Scegliamo il nostro logger anziche' quello predefinito di Jetty
		System.setProperty("org.mortbay.log.class", "it.ascia.ais.JettyLogger");
		ContextHandlerCollection contexts = new ContextHandlerCollection();
		logger = Logger.getLogger(getClass());
		server = new Server(port);
		server.setHandler(contexts);
		// contesto servito da jspservlet
		Context rootContext = new Context(contexts, "/", Context.SESSIONS);
		rootContext.setResourceBase(root);
		JspServlet jspServlet = new JspServlet();
		ServletHolder jspHolder = new ServletHolder(jspServlet);
		rootContext.addServlet(jspHolder, "*.jsp");
		// La nostra AUIRequestServlet e il suo contesto.
		HttpServlet auiRequestServlet = new AUIRequestServlet(this);
		Context jaisContext = new Context(contexts, "/jais", Context.SESSIONS);
		ServletHolder jaisHolder = new ServletHolder(auiRequestServlet);
		jaisContext.addServlet(jaisHolder, "/*");
		// La fileServlet e il suo contesto
		HttpServlet defaultServlet = new DefaultServlet();
		ServletHolder defaultHolder = new ServletHolder(defaultServlet);
		rootContext.addServlet(defaultHolder, "/");
		try {
			logger.info("Avvio server HTTP...");
			server.start();
			logger.info("Avviato server HTTP");
		} catch (Exception e) {
			logger.fatal("Errore avvio server HTTP: "+e.getMessage());
		}		
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
	 * Il cuore del controllore: riceve la richiesta e produce una risposta.
	 */
	public String receiveRequest(String command, String name, String value,
			String pin) {
		if ((this.pin != null) && (!this.pin.equals(pin))) {
			logger.warn("Richiesta con PIN errato:  \"" + command + "\" \"" +
					name + "\" \"" + value + "\"");
			return "ERROR: PIN errato.";
		}
		return receiveAuthenticatedRequest(command, name, value);
	}

	/**
	 * Si auto-invia una richiesta.
	 * 
	 * <p>Questo metodo e' per uso interno: esegue una richiesta senza
	 * controllare il pin.</p>
	 * TODO spostare in HTTPServerControllerModule
	 */
	private String receiveAuthenticatedRequest(String command, String name,
			String value) {
		String retval;
		logger.debug("Comando: \"" + command + "\" \"" + name + "\" \"" +
				value + "\"");
		try {
			if (command.equals("get")) {
				// Comando "get"
					String deviceAddress = controller.getDeviceFromAddress(name);
					String portName = controller.getPortFromAddress(name);
					Device devices[] = controller.findDevices(deviceAddress);
					if (devices.length > 0) {
						retval = "";
						for (int i = 0; i < devices.length; i++) {
							retval += devices[i].getStatus(portName, 0);
						}
					} else {
						retval = "ERROR: address " + name + " not found.";
					}
			} else if (command.equals("getAll")) {
				// Comando "getAll": equivale a "get *:*"
					retval = System.currentTimeMillis() + "\n";
					Device[] devices = controller.findDevices("*");
					long timestamp = 0;
					if (name.equals("timestamp")) {
						try {
							timestamp = Long.parseLong(value);
						} catch (NumberFormatException e) {
							// Manteniamo il valore di default: zero
						}
					}
					for (int i = 0; i < devices.length; i++) {
						retval += devices[i].getStatus("*", timestamp);
					}
			} else if (command.equals("set")) {
				// Comando "set"
					String deviceAddress = controller.getDeviceFromAddress(name);
					String portName = controller.getPortFromAddress(name);
					Device devices[] = controller.findDevices(deviceAddress);
					if (devices.length == 1) {
						devices[0].poke(portName, value);
						retval = "OK";
					} else {
						retval = "ERROR: indirizzo ambiguo";
					}
			} else {
				retval = "ERROR: Unknown command \"" + command + "\".";
			}
		} catch (AISException e) {
			logger.error(e.getMessage());
			retval = "ERROR: " + e.getMessage();
		}
		return retval;
	}
	


}
