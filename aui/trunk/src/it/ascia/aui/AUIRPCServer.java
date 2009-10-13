/**
 * (C) 2009 Ascia S.r.l.
 */
package it.ascia.aui;

import it.ascia.ais.AISException;
import it.ascia.ais.Controller;
import it.ascia.ais.Device;
import it.ascia.ais.DevicePort;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.tree.xpath.XPathExpressionEngine;
import org.apache.log4j.Logger;
import org.jabsorb.JSONRPCBridge;

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
	
	public AUIRPCServer(AUIControllerModule controllerModule ) {
		logger = Logger.getLogger(getClass());		
		this.aui = controllerModule;
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

	private SubnodeConfiguration getConfiguration(HttpSession session) {
		SubnodeConfiguration auiConfig = (SubnodeConfiguration) session.getAttribute("AUI.config");
		if (auiConfig == null) {
			auiConfig = aui.getConfiguration();
			auiConfig.setExpressionEngine(new XPathExpressionEngine());
			session.setAttribute("AUI.config",auiConfig.clone());
		}
		return auiConfig = (SubnodeConfiguration) session.getAttribute("AUI.config");
	}

	public void save(HttpSession session) throws ConfigurationException {
		SubnodeConfiguration auiConfig = (SubnodeConfiguration) session.getAttribute("AUI.config");
		if (auiConfig == null) {
			logger.debug("No configuration to save");
			return;
		}
		XMLConfiguration xmlConf = new XMLConfiguration(auiConfig);
		String fileName = "test.txt";
		xmlConf.save(fileName);
		logger.info("Saved config to: "+fileName);
	}
	
	public Vector getPages(HttpSession session) {
		SubnodeConfiguration auiConfig = getConfiguration(session);
		logger.trace(auiConfig.getExpressionEngine());
		List pages = auiConfig.configurationsAt("pages/page");
		Vector v = new Vector();
		for (Iterator iPages = pages.iterator(); iPages.hasNext(); ) {
			HierarchicalConfiguration pageConfig = (HierarchicalConfiguration) iPages.next();
			String pageId = pageConfig.getString("[@id]","");
			logger.trace("Pagina:"+pageId);
			HashMap p = new HashMap();
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
		SubnodeConfiguration auiConfig = getConfiguration(session);
		if (auiConfig.containsKey("pages/page[@id='"+id+"']/title")) {
			throw(new AISException("Duplicated page id="+id));
		}
		auiConfig.addProperty("pages page@id", id);
		auiConfig.addProperty("pages/page[@id='"+id+"'] title", title);
		auiConfig.addProperty("pages/page[@id='"+id+"'] src", src);
		logger.info("Aggiunta pagina ["+id+"] "+title);
	}
	
	public void setPageSrc(HttpSession session, String pageId,String src) {
		SubnodeConfiguration auiConfig = getConfiguration(session);
		try {
			SubnodeConfiguration pageConfig = auiConfig.configurationAt("pages/page[@id='"+pageId+"']", true);
			pageConfig.setProperty("src", src);			
		} catch (IllegalArgumentException e) {
			throw(new AISException("Page not found="+pageId));
		}
		logger.info("Modificato src pagina ["+pageId+"]: "+src);		
	}
	
	public void deletePage(HttpSession session, String id) {
		SubnodeConfiguration auiConfig = getConfiguration(session);		
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

	public Vector getPageAreas(HttpSession session, String pageId) {
		SubnodeConfiguration auiConfig = getConfiguration(session);
		auiConfig.setExpressionEngine(new XPathExpressionEngine());
		HierarchicalConfiguration pageConfig = auiConfig.configurationAt("//pages/page[@id='"+pageId+"']");
		List areas = pageConfig.configurationsAt("area");
		Vector pageAreas = new Vector();
		for (Iterator ic = areas.iterator(); ic.hasNext(); ) {
			HierarchicalConfiguration areaConfig = (HierarchicalConfiguration) ic.next();
			HashMap areaMap = new HashMap();
			for (Iterator ip = areaConfig.getKeys(); ip.hasNext(); ) {
				String k = (String) ip.next();
				areaMap.put(k, areaConfig.getString(k));
			}
			pageAreas.add(areaMap);
		}
		return pageAreas;
	}

	public Map getPageControls(HttpSession session, String pageId) {
		SubnodeConfiguration auiConfig = getConfiguration(session);
		auiConfig.setExpressionEngine(new XPathExpressionEngine());
		HierarchicalConfiguration pageConfig = auiConfig.configurationAt("//pages/page[@id='"+pageId+"']");
		List controls = pageConfig.configurationsAt("control");
		Map pageControls = new HashMap();
		for (Iterator ic = controls.iterator(); ic.hasNext(); ) {
			HierarchicalConfiguration controlConfig = (HierarchicalConfiguration) ic.next();
			String type = controlConfig.getString("type");
			String id = controlConfig.getString("[@id]");
			String controlId = "control-" + pageId + "-" + id;
			SubnodeConfiguration typeConfig = auiConfig.configurationAt("controls/"+type);
			HashMap controlMap = new HashMap();
			for (Iterator ip = typeConfig.getKeys(); ip.hasNext(); ) {
				String k = (String) ip.next();
				controlMap.put(k, typeConfig.getProperty(k));
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
		SubnodeConfiguration auiConfig = getConfiguration(session);
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
				controlConfig.setProperty(key, value);
				logger.debug(fullControlId + ", "+key+": "+oldValue+" -> "+value);
			} else {
				controlConfig.addProperty(key, value);
				logger.debug(fullControlId + ", "+key+": "+value);
			}		
		}
	}
	
	public Vector getPorts() {
		return getPorts("*");
	}
	
	public Vector getPorts(String search) {		
		Device devices[] = Controller.getController().findDevices(search);
		Vector ports = new Vector();
		for (int i = 0; i < devices.length; i++) {
			Device d = devices[i];
			DevicePort[] devicePorts = d.getPorts();
			for (int j = 0; j < devicePorts.length; j++) {
				DevicePort devicePort = devicePorts[j];
				HashMap p = new HashMap();
				p.put("Address",devicePort.getFullAddress());
				p.put("Class",devicePort.getClass().getSimpleName());
				p.put("Name",devicePort.getName());						
				ports.add(p);
			}			
		}	
		return ports;
	}

	
}
