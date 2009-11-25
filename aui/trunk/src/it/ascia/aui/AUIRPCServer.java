/**
 * (C) 2009 Ascia S.r.l.
 */
package it.ascia.aui;

import it.ascia.ais.AISException;
import it.ascia.ais.Address;
import it.ascia.ais.Controller;
import it.ascia.ais.Device;
import it.ascia.ais.DevicePort;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.servlet.http.HttpSession;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.tree.ConfigurationNode;
import org.apache.commons.configuration.tree.xpath.XPathExpressionEngine;
import org.apache.log4j.Logger;

/**
 * Server RPC, espone i metodi usati dal client RPC
 * @author Sergio
 *
 */
public class AUIRPCServer implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8944342209414623443L;

	protected AUIControllerModule aui;
	
    protected Logger logger;
    
    private Controller controller; 
	
	public AUIRPCServer(AUIControllerModule controllerModule ) {
		logger = Logger.getLogger(getClass());		
		this.aui = controllerModule;
		controller = Controller.getController();
	}
	
	public boolean isLogged(HttpSession session) {
		String username = (String) session.getAttribute("AUI.username");
		Boolean logged = (Boolean) session.getAttribute("AUI.logged");
		if (username != null && logged != null && logged.booleanValue()) {
			logger.debug("Session "+session.getId()+" Authenticated user '"+username+"'");
			return true;
		}
		logger.debug("Session "+session.getId()+" Not authenticated"); 
		return false;
	}
	
	public boolean login(HttpSession session, String username, String password) {
		// TODO Spostare autenticazione a livello di AUIControllerModule, in modo da usarlo anche per gli altri servlet
		HierarchicalConfiguration auiConfig = aui.getConfiguration();
		auiConfig.setExpressionEngine(new XPathExpressionEngine());
		if (password.equals(auiConfig.getString("//users/user[username='"+username+"']/password"))) {
			session.setAttribute("AUI.username",username);
			session.setAttribute("AUI.logged",new Boolean(true));
			logger.info("Autenticated, username '"+username+"'");
			return true;
		} else {
			logger.error("Authentication failed, username '"+username+"' password '"+password+"'");
			try {
				Thread.sleep(200+Math.round(2000*Math.random()));
			} catch (InterruptedException e) {
				// don't care
			}
			return false;
		}
	}
		
	public boolean logout(HttpSession session) {
		logger.debug("Invalidate session "+session.getId());
		session.invalidate();
		return true;
	}
	
	public Object getPortValue(HttpSession session,String fullAddress) {
		DevicePort p = controller.getDevicePort(new Address(fullAddress));
		if (p == null) {
			throw(new AISException("Porta non trovata: "+fullAddress));
		}
		if (p.isExpired() || p.isDirty()) {
			return null;
		}
		return p.getCachedValue();
	}

	public boolean writePortValue(HttpSession session,String fullAddress, Object newValue) {
		DevicePort p = controller.getDevicePort(new Address(fullAddress));
		if (p == null) {
			throw(new AISException("Porta non trovata: "+fullAddress));
		}
		return p.writeValue(newValue);
	}

	private HierarchicalConfiguration getConfiguration(HttpSession session) {
		HierarchicalConfiguration auiConfig = (SubnodeConfiguration) session.getAttribute("AUI.config");
		if (auiConfig == null) {
			auiConfig = aui.getConfiguration();
			auiConfig.setExpressionEngine(new XPathExpressionEngine());
			session.setAttribute("AUI.config",auiConfig.clone());
		}
		return auiConfig = (HierarchicalConfiguration) session.getAttribute("AUI.config");
	}

	public void save(HttpSession session) throws ConfigurationException, FileNotFoundException {
		SubnodeConfiguration auiConfig = (SubnodeConfiguration) session.getAttribute("AUI.config");
		if (auiConfig == null) {
			logger.debug("No configuration to save");
			return;
		}
		AUIControllerModule aui = (AUIControllerModule) controller.getModule("AUI");
		aui.setConfiguration(auiConfig);
		aui.saveConfiguration();
		logger.info("Saved configuration to: "+aui.getConfigurationFilename());
	}

	public void saveAs(HttpSession session, String filename, boolean overwrite) throws ConfigurationException, FileNotFoundException {
		SubnodeConfiguration auiConfig = (SubnodeConfiguration) session.getAttribute("AUI.config");
		if (auiConfig == null) {
			logger.debug("No configuration to save");
			return;
		}
		AUIControllerModule aui = (AUIControllerModule) controller.getModule("AUI");
		aui.saveConfigurationAs(auiConfig,filename, overwrite);	
		logger.info("Saved config to: "+filename);
	}
	
	public Vector<HashMap<String, String>> getPages(HttpSession session) {
		HierarchicalConfiguration auiConfig = getConfiguration(session);
		logger.trace(auiConfig.getExpressionEngine());
		List pages = auiConfig.configurationsAt("pages/page");
		Vector<HashMap<String, String>> v = new Vector<HashMap<String, String>>();
		for (Iterator iPages = pages.iterator(); iPages.hasNext(); ) {
			HierarchicalConfiguration pageConfig = (HierarchicalConfiguration) iPages.next();
			String pageId = pageConfig.getString("[@id]","");
			logger.trace("Pagina:"+pageId);
			HashMap<String, String> p = new HashMap<String, String>();
			p.put("id", pageId);
			p.put("src", pageConfig.getString("src",""));
			p.put("title", pageConfig.getString("title",""));
			v.add(p);
		}
		return v;
	}
	
	/**
	 * @param id
	 * @param title
	 * @param src
	 */
	public void newPage(HttpSession session, String id, String title, String src) {
		HierarchicalConfiguration auiConfig = getConfiguration(session);
		if (auiConfig.containsKey("pages/page[@id='"+id+"']/title")) {
			throw(new AISException("Duplicated page id="+id));
		}
		auiConfig.addProperty("pages page@id", id);
		auiConfig.addProperty("pages/page[@id='"+id+"'] title", title);
		auiConfig.addProperty("pages/page[@id='"+id+"'] src", src);
		logger.info("Aggiunta pagina ["+id+"] "+title);
	}
	
	public void setPageTitle(HttpSession session, String pageId,String title) {
		HierarchicalConfiguration auiConfig = getConfiguration(session);
		try {
			SubnodeConfiguration pageConfig = auiConfig.configurationAt("pages/page[@id='"+pageId+"']", true);
			pageConfig.setProperty("title", title);			
		} catch (IllegalArgumentException e) {
			throw(new AISException("Page not found="+pageId));
		}
		logger.info("Modificato title pagina ["+pageId+"]: "+title);		
	}

	public void setPageSrc(HttpSession session, String pageId,String src) {
		HierarchicalConfiguration auiConfig = getConfiguration(session);
		try {
			SubnodeConfiguration pageConfig = auiConfig.configurationAt("pages/page[@id='"+pageId+"']", true);
			pageConfig.setProperty("src", src);			
		} catch (IllegalArgumentException e) {
			throw(new AISException("Page not found="+pageId));
		}
		logger.info("Modificato src pagina ["+pageId+"]: "+src);		
	}
	
	public void deletePage(HttpSession session, String id) {
		HierarchicalConfiguration auiConfig = getConfiguration(session);		
		auiConfig.clearTree("pages/page[@id='"+id+"']");
		logger.info("Removed page "+id);
			//throw(new AISException("Page not found id="+id));
	}

	public String getSkin() {
		HierarchicalConfiguration auiConfig = aui.getConfiguration();
		return auiConfig.getString("skin");		
	}
	
	public List<String> getImagesList(HttpSession session) {
		List<String> images = new Vector<String>();
		String imagesPath = aui.getImagesPath();
		String dirname = session.getServletContext().getRealPath(imagesPath);
		logger.info("Looking for files in '"+imagesPath+"' ("+dirname+")");
		File dir = new File(dirname);	    
	    File[] children = dir.listFiles();
	    if (children == null) {
	        // Either dir does not exist or is not a directory
	    } else {
	        for (int i=0; i<children.length; i++) {
	            // Get filename of file or directory
	        	File item = children[i];
	        	if (item.isFile()) {
	        		images.add(imagesPath + item.getName());
	        	}
	        }
	    }
	    return images;
	}

	public Vector<HashMap<String, String>> getPageAreas(HttpSession session, String pageId) {
		HierarchicalConfiguration auiConfig = getConfiguration(session);
		auiConfig.setExpressionEngine(new XPathExpressionEngine());
		HierarchicalConfiguration pageConfig = auiConfig.configurationAt("//pages/page[@id='"+pageId+"']");
		List areas = pageConfig.configurationsAt("area");
		Vector<HashMap<String, String>> pageAreas = new Vector<HashMap<String, String>>();
		for (Iterator ic = areas.iterator(); ic.hasNext(); ) {
			HierarchicalConfiguration areaConfig = (HierarchicalConfiguration) ic.next();
			HashMap<String, String> areaMap = new HashMap<String, String>();
			for (Iterator ip = areaConfig.getKeys(); ip.hasNext(); ) {
				String k = (String) ip.next();
				areaMap.put(k, areaConfig.getString(k));
			}
			pageAreas.add(areaMap);
		}
		return pageAreas;
	}

	public Map<String, HashMap<String, Object>> getPageControls(HttpSession session, String pageId) {
		HierarchicalConfiguration auiConfig = getConfiguration(session);
		auiConfig.setExpressionEngine(new XPathExpressionEngine());
		HierarchicalConfiguration pageConfig = auiConfig.configurationAt("//pages/page[@id='"+pageId+"']");
		List controls = pageConfig.configurationsAt("control");
		Map<String, HashMap<String, Object>> pageControls = new HashMap<String, HashMap<String, Object>>();
		for (Iterator ic = controls.iterator(); ic.hasNext(); ) {
			HierarchicalConfiguration controlConfig = (HierarchicalConfiguration) ic.next();
			String type = controlConfig.getString("type");
			String id = controlConfig.getString("[@id]");
			String controlId = "control-" + pageId + "-" + id;
			HashMap<String, Object> controlMap = new HashMap<String, Object>();
			if (auiConfig.containsKey("controls/"+type+"/default")) {
				SubnodeConfiguration typeConfig = auiConfig.configurationAt("controls/"+type);
				for (Iterator ip = typeConfig.getKeys(); ip.hasNext(); ) {
					String k = (String) ip.next();
					controlMap.put(k, typeConfig.getString(k));
				}
			}
			for (Iterator ip = controlConfig.getKeys(); ip.hasNext(); ) {
				String k = (String) ip.next();
				controlMap.put(k, controlConfig.getProperty(k));
			}
			pageControls.put(controlId, controlMap);
		}
		return pageControls;
	}
	
	public void setPageControl(HttpSession session, String fullControlId, Map parameters) {
		HierarchicalConfiguration auiConfig = getConfiguration(session);
		auiConfig.setExpressionEngine(new XPathExpressionEngine());
		String[] parts = fullControlId.split("-",3);
		logger.debug("fullControlId="+fullControlId);
		if (!parts[0].equals("control")) {
			throw( new AISException("fullControlId must start with 'control'"));
		}
		if (parts.length != 3) {
			throw( new AISException("fullControlId must have 3 parts"));
		}
		String pageId = parts[1];
		String controlId = parts[2];
		HierarchicalConfiguration controlConfig = auiConfig.configurationAt("//pages/page[@id='"+pageId+"']/control[@id='"+controlId+"']", true);
		for (Iterator keys = parameters.keySet().iterator(); keys.hasNext();) {
			String key = (String) keys.next();
			Object value = parameters.get(key);
			if (controlConfig.containsKey(key)) {
				Object oldValue = controlConfig.getProperty(key);				
				controlConfig.setProperty(key, (Object) value);
				logger.debug(fullControlId + ", "+key+" ("+Object.class.getSimpleName()+") : "+oldValue+" -> "+value);
			} else {
				controlConfig.addProperty(key, value);
				logger.debug(fullControlId + ", "+key+": "+value);
			}		
		}
	}
	
	public Vector<HashMap<String, String>> getPorts() {
		return getPorts("*");
	}
	
	public Vector<HashMap<String, String>> getPorts(String search) {
		Address address = new Address(search);
		Collection<Device> devices = Controller.getController().getDevices(address);
		Vector<HashMap<String, String>> ports = new Vector<HashMap<String, String>>();
		for (Device d : devices) {
			DevicePort[] devicePorts = d.getPorts();
			for (int j = 0; j < devicePorts.length; j++) {
				DevicePort devicePort = devicePorts[j];
				HashMap<String, String> p = new HashMap<String, String>();
				p.put("Address",devicePort.getFullAddress());
				p.put("Class",devicePort.getClass().getSimpleName());
				p.put("Description",devicePort.getDescription());						
				ports.add(p);
			}			
		}	
		return ports;
	}

	
}
