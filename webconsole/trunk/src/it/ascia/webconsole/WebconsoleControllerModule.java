package it.ascia.webconsole;

import it.ascia.ais.Controller;
import it.ascia.ais.ControllerModule;
import it.ascia.ais.HTTPServerControllerModule;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.jabsorb.JSONRPCBridge;
import org.jabsorb.JSONRPCServlet;

public class WebconsoleControllerModule extends ControllerModule {

	public void start() {
		super.start();
		HTTPServerControllerModule h = (HTTPServerControllerModule) Controller.getController().getModule("HTTPServer");

		HierarchicalConfiguration config = getConfiguration();
		String path = config.getString("contextBase[@path]","/webconsole");
		String base = config.getString("contextBase","/webconsole");
		logger.info("Webconsole contextPath="+path+" resourceBase="+base);
		
		ServletContextHandler context = h.addContext(path, base,ServletContextHandler.SESSIONS, false);

		context.addServlet(new ServletHolder("JSONRPC",new JSONRPCServlet()),"/rpc");				

		JSONRPCBridge.getGlobalBridge().registerObject("Webconsole", new WebconsoleRPCServer(this));
	}

}
