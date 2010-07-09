package it.ascia.webconsole;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.jabsorb.JSONRPCBridge;
import org.jabsorb.JSONRPCServlet;
import org.mortbay.jetty.servlet.Context;

import it.ascia.ais.Controller;
import it.ascia.ais.ControllerModule;
import it.ascia.ais.HTTPServerControllerModule;

public class WebconsoleControllerModule extends ControllerModule {

	public void start() {
		super.start();
		HTTPServerControllerModule h = (HTTPServerControllerModule) Controller.getController().getModule("HTTPServer");

		HierarchicalConfiguration config = getConfiguration();
		String path = config.getString("contextBase[@path]","/webconsole");
		String base = config.getString("contextBase","/webconsole");
		logger.info("Webconsole contextPath="+path+" resourceBase="+base);
		
		Context root = h.addContext(path, base);

		h.addServlet(new JSONRPCServlet(),root,"/rpc");

		JSONRPCBridge.getGlobalBridge().registerObject("Webconsole", new WebconsoleRPCServer(this));
	}

}
