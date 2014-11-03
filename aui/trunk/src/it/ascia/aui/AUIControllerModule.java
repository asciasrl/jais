package it.ascia.aui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Map.Entry;

import javax.servlet.http.HttpSession;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.tree.xpath.XPathExpressionEngine;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.jabsorb.JSONRPCBridge;
import org.jabsorb.JSONRPCServlet;
// TODO Eliminare riferimenti a jsonsimple (usare org.json di jabsorb)
import org.json.simple.JSONObject;

import it.ascia.ais.AISException;
import it.ascia.ais.Address;
import it.ascia.ais.CommandInterface;
import it.ascia.ais.Controller;
import it.ascia.ais.ControllerModule;
import it.ascia.ais.DevicePort;
import it.ascia.ais.HTTPServerControllerModule;

public class AUIControllerModule extends ControllerModule {
	
	public static ResourceBundle messages = ResourceBundle.getBundle("it.ascia.aui.messages");

	private static final String SESSION_ATTRIBUTE_USERNAME = "AUI.username";
	
	private static final String SESSION_ATTRIBUTE_ISLOGGED = "AUI.isLogged";
	
    private HierarchicalConfiguration skinConfiguration;

	public AUIControllerModule() {
		super();
	}

	@Override
	public void setConfiguration(HierarchicalConfiguration config) {
		String skin = config.getString("AUI.skin");
		if (skin == null) {
			throw(new AISException("Missing <skin> tag in configuration!"));
		}
		if (!skin.endsWith("/")) {
			skin += "/";
		}
		if (!skin.startsWith("/")) {
			String webroot = config.getString("AUI.webroot");
			if (webroot == null) {
				throw(new AISException("Missing <webroot> tag in configuration!"));
			}
			if (!webroot.endsWith("/")) {
				webroot += "/";
			}
			skin = webroot + skin;
		}
		skin += "skin.xml";		
		try {
			skinConfiguration = new XMLConfiguration(skin);
		} catch (ConfigurationException e) {
			//logger.fatal("Error loading skin configuration from "+skin,e);
			throw(new AISException("Error loading skin configuration from: "+skin,e));
		}
		//skinConfiguration.setExpressionEngine(new XPathExpressionEngine());
		logger.info("Loaded skin configuration from: "+skin);
		super.setConfiguration(config);
	}
	
	public HierarchicalConfiguration getSkinConfiguration() {
		return skinConfiguration.configurationAt(getName());
	}
	
	public void start() {
		super.start();
		HTTPServerControllerModule h = (HTTPServerControllerModule) Controller.getController().getModule("HTTPServer");

		HierarchicalConfiguration config = getConfiguration();
		String webroot = config.getString("webroot","web");
		logger.info("AUI web root is: "+webroot);
		
		ServletContextHandler root = h.addContext("/", webroot, ServletContextHandler.SESSIONS, true);

		root.addServlet(new ServletHolder("RPC",new JSONRPCServlet()), "/aui/rpc");

		root.addServlet(new ServletHolder("UPLOAD",new UploadServlet()), "/aui/upload");

		root.addServlet(new ServletHolder("STREAMING",new AUIStreamingServlet()), "/stream/*");
		
		root.addServlet(new ServletHolder("WS",new AUIWebSocketServlet()), "/ws/*");
		
		JSONRPCBridge.getGlobalBridge().registerObject("AUI", new AUIRPCServer(this));
	}
	
	public void stop() {
		super.stop();
		if (JSONRPCBridge.getGlobalBridge().lookupClass("AUI") != null) {
			JSONRPCBridge.getGlobalBridge().unregisterObject("AUI");
		}
	}
	
	/**
	 * Lo stream viene chiuso dopo che sono stati trasmessi uno specifico numero di eventi.
	 * Non vengono conteggiati gli eventi trasmessi all'avvio dello streaming.
	 * usare maxEventsPerRequest come elemento nella configurazione del modulo AUI
	 * Default: 100
	 */
	public int getMaxEventsPerRequest() {
		return getConfiguration().getInt("maxRequestPerStream",100);
	}
	
	/**
	 * 
	 * @param mapId
	 * @return
	 */
	public String getMapControls(String mapId) {
		JSONObject j =new JSONObject();
		HierarchicalConfiguration auiConfig = getConfiguration();
		HierarchicalConfiguration skinConfig = getSkinConfiguration();
		List maps = auiConfig.configurationsAt("map");
		for (Iterator im = maps.iterator(); im.hasNext(); ) {
			HierarchicalConfiguration mapConfig = (HierarchicalConfiguration) im.next();
			if (mapConfig.getString("id").equals(mapId)) {
				List controls = mapConfig.configurationsAt("control");
				logger.debug("controlli della mappa '"+mapId+"' :"+controls.size());
				for (Iterator ic = controls.iterator(); ic.hasNext(); ) {
					HierarchicalConfiguration controlConfig = (HierarchicalConfiguration) ic.next();
					String id = mapId + "-" + controlConfig.getString("id");
					String type = controlConfig.getString("type");
					SubnodeConfiguration typeConfig = skinConfig.configurationAt("controls."+type);
					HashMap controlMap = new HashMap();
					for (Iterator ip = typeConfig.getKeys(); ip.hasNext(); ) {
						String k = (String) ip.next();
						controlMap.put(k, typeConfig.getString(k));
					}
					for (Iterator ip = controlConfig.getKeys(); ip.hasNext(); ) {
						String k = (String) ip.next();
						controlMap.put(k, controlConfig.getString(k));
					}
					j.put(id, controlMap);
				}
			}
		}
		return j.toJSONString();
	}

	/**
	 * @deprecated
	 */
	public LinkedHashMap getPages() {
		LinkedHashMap h = new LinkedHashMap();
		HierarchicalConfiguration auiConfig = getConfiguration();
		List pages = auiConfig.configurationsAt("pages.page");
		for (Iterator iPages = pages.iterator(); iPages.hasNext(); ) {
			HierarchicalConfiguration pageConfig = (HierarchicalConfiguration) iPages.next();
			String pageId = pageConfig.getString("[@id]");
			logger.trace("Pagina:"+pageId);
			HashMap p = new HashMap();
			p.put("id", pageId);
			p.put("src", pageConfig.getString("src"));
			p.put("title", pageConfig.getString("title"));
			h.put(pageId, p);
		}
		return h;
	}
	
	public String set(HashMap params) {
		// Comando "set"
		if (params.size() == 0) {
			throw(new AISException("mancano parametri"));
		}
		Iterator iterator = params.entrySet().iterator();
		Entry entry = (Entry) iterator.next();
		Address address = new Address((String) entry.getKey());
		if (! address.isFullyQualified()) {
			throw(new AISException("Parametro 'address' richiesto"));
		}
		String value = (String) entry.getValue();				
		if (value == null) {
			throw(new AISException("Parametro 'value' richiesto"));
		}
		DevicePort p = controller.getDevicePort(address);
		if (p == null) {
			throw(new AISException("Port not found"));
		}			
		if (p.writeValue(value)) {
			return "OK";
		} else {
			return "ERROR";
		}		
	}
	
	class setCommand implements CommandInterface {
		public String execute(HashMap params) {
			return set(params);
		}
	}

	/**
	 * 
	 * @param page id della pagina
	 * @return Elenco delle porte presenti nella pagina
	 */
	public List<DevicePort> getPagePorts(String page) {
		List<DevicePort> ports = new ArrayList<DevicePort>();
		Controller controller = Controller.getController();
		HierarchicalConfiguration auiConfig = getConfiguration();
		auiConfig.setExpressionEngine(new XPathExpressionEngine());
		HierarchicalConfiguration pageConfig = auiConfig.configurationAt("pages/page[@id='"+page+"']");
		List<HierarchicalConfiguration> controls = pageConfig.configurationsAt("control");
		for (Iterator<HierarchicalConfiguration> ic = controls.iterator(); ic.hasNext(); ) {
			HierarchicalConfiguration controlConfig = ic.next();
			List<HierarchicalConfiguration> addresses = controlConfig.configurationsAt("address");
			for (int i = 0; i < addresses.size(); i++) {
				Address address = new Address((String) ((SubnodeConfiguration) addresses.get(i)).getRoot().getValue());
				if (address.isFullyQualified()) {
					DevicePort p = controller.getDevicePort(address);
					if (p != null) {
						ports.add(p);
					}
				}
			}
		}
		return ports;
	}

	/**
	 * Get the path where images resides
	 * @return path relative to web root
	 */
	public String getImagesPath() {
		return getConfiguration().getString("images","/images/");
	}

	/**
	 * Check user credential
	 * @param username
	 * @param password
	 * @return True if credentials match
	 */
	public boolean login(HttpSession session, String username, String password) {		
		HierarchicalConfiguration auiConfig = getConfiguration();
		auiConfig.setExpressionEngine(new XPathExpressionEngine());
		if (password.equals(auiConfig.getString("//users/user[username='"+username+"']/password"))) {
			session.setAttribute(SESSION_ATTRIBUTE_USERNAME,username);
			session.setAttribute(SESSION_ATTRIBUTE_ISLOGGED,new Boolean(true));
			logger.info("Autenticated, username '"+username+"'");			
			return true;
		}
		return false;
	}
	
	/**
	 * Check if user is logged (use session)
	 * @param session
	 * @return
	 */
	public boolean isLogged(HttpSession session) {
		if (session == null) {
			logger.warn("Session not available");
			return false;
		}
		String username = (String) session.getAttribute(SESSION_ATTRIBUTE_USERNAME);
		Boolean logged = (Boolean) session.getAttribute(SESSION_ATTRIBUTE_ISLOGGED);
		if (username != null && logged != null && logged.booleanValue()) {
			logger.debug("Session "+session.getId()+" Authenticated user '"+username+"'");
			return true;
		}
		logger.debug("Session "+session.getId()+" Not authenticated"); 
		return false;
	}

}
