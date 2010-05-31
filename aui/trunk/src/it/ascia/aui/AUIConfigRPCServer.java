/**
 * (C) 2009 Ascia S.r.l.
 */
package it.ascia.aui;

import it.ascia.ais.AISException;
import it.ascia.ais.Address;
import it.ascia.ais.Controller;
import it.ascia.ais.Device;
import it.ascia.ais.DevicePort;
import it.ascia.ais.randomUUID;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;
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
 * Server RPC, espone i metodi usati dal client RPC per la configurazione
 * @author Sergio
 *
 */
public class AUIConfigRPCServer implements Serializable {


	/**
	 * 
	 */
	private static final long serialVersionUID = 984689458544879562L;

	protected AUIControllerModule aui;
	
    protected Logger logger;
    
    private Controller controller; 
	
	public AUIConfigRPCServer(AUIControllerModule controllerModule ) {
		logger = Logger.getLogger(getClass());		
		this.aui = controllerModule;
		controller = Controller.getController();
	}

	/**
	 * Get the copy of aui configuration stored in the session
	 * @param session
	 * @return
	 */
	private HierarchicalConfiguration getConfiguration(HttpSession session) {
		HierarchicalConfiguration auiConfig = (SubnodeConfiguration) session.getAttribute("AUI.config");
		if (auiConfig == null) {
			auiConfig = aui.getConfiguration();
			auiConfig.setExpressionEngine(new XPathExpressionEngine());
			session.setAttribute("AUI.config",auiConfig.clone());
		}
		return auiConfig = (HierarchicalConfiguration) session.getAttribute("AUI.config");
	}

	/**
	 * Save AUI configuration to xml configuration file
	 * @param session
	 * @throws ConfigurationException
	 * @throws FileNotFoundException
	 */
	public void save(HttpSession session) throws ConfigurationException, FileNotFoundException {
		saveAs(session,aui.getConfigurationFilename(),true);
	}

	/**
	 * Save AUI configuration to a specific xml configuration file
	 * @param session
	 * @param filename
	 * @param overwrite Overwrite existing file
	 * @throws ConfigurationException
	 * @throws FileNotFoundException If file already exists
	 */
	public void saveAs(HttpSession session, String filename, boolean overwrite) throws ConfigurationException, FileNotFoundException {
		SubnodeConfiguration auiConfig = (SubnodeConfiguration) session.getAttribute("AUI.config");
		if (auiConfig == null) {
			logger.debug("No configuration to save");
			return;
		}
		if (!overwrite) {
			File f = new File(filename);
			if (f.exists()) {
				throw(new FileNotFoundException("File already exists: "+filename));
			}
		}
		XMLConfiguration xmlConfiguration = new XMLConfiguration();
		xmlConfiguration.setExpressionEngine(new XPathExpressionEngine());
		xmlConfiguration.setRootElementName("jais:configuration");
		xmlConfiguration.addProperty("/ @version", Controller.CONFIGURATION_VERSION);
		Collection<ConfigurationNode> c = new Vector<ConfigurationNode>();
		c.add(auiConfig.getRootNode());
		xmlConfiguration.addNodes(null, c);
		xmlConfiguration.setFileName(filename);		
		try {
			logger.info("Saving configuration to '"+filename+"'");
			xmlConfiguration.save();
		} catch (ConfigurationException e) {
			logger.error("Unable to save configuration to '"+filename+"': ",e);
			throw (new AISException("Unable to save configuration to '"+filename+"'"));
		}
		logger.info("Saved config to: "+filename);
	}
	
	/**
	 * Get AUI pages configuration
	 * @param session
	 * @return For each page: id, src, title
	 */
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
	 * Add a page
	 * @param id
	 * @param src
	 * @param title
	 */
	public void newPage(HttpSession session, String id, String src, String title) {
		HierarchicalConfiguration auiConfig = getConfiguration(session);
		if (auiConfig.containsKey("pages/page[@id='"+id+"']/title")) {
			throw(new AISException("Duplicated page id="+id));
		}
		auiConfig.addProperty("pages page@id", id);
		auiConfig.addProperty("pages/page[@id='"+id+"'] src", src);
		auiConfig.addProperty("pages/page[@id='"+id+"'] title", title);
		logger.info("Aggiunta pagina ["+id+"] "+title);
	}
	
	/**
	 * Change page id
	 * Don't update references
	 * @param session
	 * @param pageId
	 * @param newId
	 */
	public void changePageId(HttpSession session, String pageId, String newId) {
		if (pageId.equals(newId)) {
			logger.warn("Ignored identity page id change");
			return;
		}
		HierarchicalConfiguration auiConfig = getConfiguration(session);
		if (auiConfig.containsKey("pages/page[@id='"+newId+"']/title")) {
			throw(new AISException("Duplicated page id="+newId));
		}		
		HierarchicalConfiguration pageConfig = auiConfig.configurationAt("//pages/page[@id='"+pageId+"']", true);
		pageConfig.setProperty("[@id]", newId);
		logger.info("Page "+pageId+", change id: "+newId);
	}
	
	/**
	 * Change the title of an existing page
	 * @param session
	 * @param pageId
	 * @param title
	 */
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

	/**
	 * Change the src of an existing page
	 * @param session
	 * @param pageId
	 * @param src
	 */
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
	
	/**
	 * Delete an existing page
	 * @param session
	 * @param pageId Page to be deleted
	 */
	public void deletePage(HttpSession session, String pageId) {
		HierarchicalConfiguration auiConfig = getConfiguration(session);		
		auiConfig.clearTree("pages/page[@id='"+pageId+"']");
		logger.info("Removed page "+pageId);
			//throw(new AISException("Page not found id="+id));
	}

	/**
	 * Get the skin path
	 * @param session
	 * @return
	 */
	public String getSkin(HttpSession session) {
		HierarchicalConfiguration auiConfig = getConfiguration(session);
		return auiConfig.getString("skin");		
	}
	
	/**
	 * Get list of images in "images" folder
	 * @param session
	 * @return
	 */
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

	/**
	 * Get areas of the page
	 * @param session
	 * @param pageId
	 * @return
	 */
	public Vector<HashMap<String, String>> getPageAreas(HttpSession session, String pageId) {
		HierarchicalConfiguration auiConfig = getConfiguration(session);
		//auiConfig.setExpressionEngine(new XPathExpressionEngine());
		HierarchicalConfiguration pageConfig = auiConfig.configurationAt("//pages/page[@id='"+pageId+"']");
		List<HierarchicalConfiguration> areas = pageConfig.configurationsAt("area");
		Vector<HashMap<String, String>> pageAreas = new Vector<HashMap<String, String>>();
		for (Iterator<HierarchicalConfiguration> ic = areas.iterator(); ic.hasNext(); ) {
			HierarchicalConfiguration areaConfig = (HierarchicalConfiguration) ic.next();
			HashMap<String, String> areaMap = new HashMap<String, String>();
			for (Iterator<String> ip = areaConfig.getKeys(); ip.hasNext(); ) {
				String k = ip.next();
				areaMap.put(k, areaConfig.getString(k));
			}
			pageAreas.add(areaMap);
		}
		return pageAreas;
	}

	/**
	 * Get controls of the page
	 * @param session
	 * @param pageId
	 * @return
	 */
	public Vector<HashMap<String, Object>> getPageControls(HttpSession session, String pageId) {
		HierarchicalConfiguration auiConfig = getConfiguration(session);
		//auiConfig.setExpressionEngine(new XPathExpressionEngine());
		HierarchicalConfiguration pageConfig = auiConfig.configurationAt("//pages/page[@id='"+pageId+"']");
		List controls = pageConfig.configurationsAt("control");
		Vector<HashMap<String, Object>> pageControls = new Vector<HashMap<String, Object>>();
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
			pageControls.add(controlMap);
		}
		return pageControls;
	}

	/**
	 * Add a new control to a page
	 * @param session
	 * @param pageId
	 * @param controlId
	 * @param title
	 */
	public void newPageControl(HttpSession session, String pageId, String controlId, String type, String title) {
		HierarchicalConfiguration auiConfig = getConfiguration(session);
		if (auiConfig.containsKey("pages/page[@id='"+pageId+"']/control[@id='"+controlId+"']/type")) {
			throw(new AISException("Duplicated control id="+controlId+" on page "+pageId));
		}
		if (!auiConfig.containsKey("controls/"+type+"/default")) {
			throw(new AISException("Control type not defined: "+type));
		}		
		auiConfig.addProperty("pages/page[@id='"+pageId+"'] control@id", controlId);
		auiConfig.addProperty("pages/page[@id='"+pageId+"']/control[@id='"+controlId+"'] type",type);
		auiConfig.addProperty("pages/page[@id='"+pageId+"']/control[@id='"+controlId+"'] title",title);
		auiConfig.addProperty("pages/page[@id='"+pageId+"']/control[@id='"+controlId+"'] top",0);
		auiConfig.addProperty("pages/page[@id='"+pageId+"']/control[@id='"+controlId+"'] left",0);
		logger.info("Added control "+controlId+" on page "+pageId+": "+title);
	}

	/**
	 * Delete a page control
	 * @param session
	 * @param fullControlId
	 */
	public void deletePageControl(HttpSession session, String fullControlId) {
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
		deletePageControl(session, pageId, controlId);
	}
	
	/**
	 * Delete a page control
	 * @param session
	 * @param pageId
	 * @param controlId
	 */
	public void deletePageControl(HttpSession session, String pageId, String controlId) {
		HierarchicalConfiguration auiConfig = getConfiguration(session);		
		auiConfig.clearTree("pages/page[@id='"+pageId+"']/control[@id='"+controlId+"']");
		logger.info("Removed control "+pageId+" / "+controlId);		
	}

	/**
	 * Set arbitrary parameters of the control
	 * @param session
	 * @param fullControlId
	 * @param properties
	 */
	public void setPageControlProperties(HttpSession session, String fullControlId, Map properties) {
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
		setPageControlProperties(session, pageId, controlId, properties);
	}
		
	public void setPageControlProperties(HttpSession session, String pageId, String controlId, Map properties) {
		for (Iterator keys = properties.keySet().iterator(); keys.hasNext();) {
			String key = (String) keys.next();
			Object value = properties.get(key);
			setPageControlProperty(session, pageId, controlId, key, value);
		}
	}
	

	public void setPageControlProperty(HttpSession session, String fullControlId, String key, Object value) {
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
		setPageControlProperty(session, pageId, controlId, key, value);
	}

	public void setPageControlProperty(HttpSession session, String pageId, String controlId, String key, Object value) {
		HierarchicalConfiguration auiConfig = getConfiguration(session);
		HierarchicalConfiguration controlConfig = auiConfig.configurationAt("//pages/page[@id='"+pageId+"']/control[@id='"+controlId+"']", true);
		Object oldValue = null;
		if (controlConfig.containsKey(key)) {
			controlConfig.setProperty(key, (Object) value);
		} else {
			controlConfig.addProperty(key, value);
		}		
		logger.debug(pageId + "/" + controlId + ", "+key+" ("+Object.class.getSimpleName()+") : "+oldValue+" -> "+value);
	}
	
	public void changePageControlId(HttpSession session, String fullControlId, String newId) {
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
		changePageControlId(session, pageId, controlId, newId);
	}
	
	public void changePageControlId(HttpSession session, String pageId, String controlId, String newId) {
		if (controlId.equals(newId)) {
			logger.trace("Control id not changed");
			return;
		}
		HierarchicalConfiguration auiConfig = getConfiguration(session);
		if (auiConfig.containsKey("pages/page[@id='"+pageId+"']/control[@id='"+newId+"']/type")) {
			throw(new AISException("Duplicated control id="+newId+" on page "+pageId));
		}		
		HierarchicalConfiguration controlConfig = auiConfig.configurationAt("//pages/page[@id='"+pageId+"']/control[@id='"+controlId+"']", true);
		controlConfig.setProperty("[@id]", newId);
		logger.debug(pageId + "/" + controlId + ", change id: "+newId);
	}
	
	public Vector<HashMap<String, String>> getPorts() {
		return getPorts("*");
	}
	
	public Vector<HashMap<String, String>> getPorts(String search) {
		Vector<HashMap<String, String>> ports = new Vector<HashMap<String, String>>();
		for (Device d : Controller.getController().getDevices(new Address(search))) {
			for (DevicePort devicePort : d.getPorts()) {
				HashMap<String, String> p = new HashMap<String, String>();
				p.put("Address",devicePort.getAddress().toString());
				p.put("Class",devicePort.getClass().getSimpleName());
				p.put("Description",devicePort.getDescription());						
				ports.add(p);
			}			
		}	
		return ports;
	}
	
}
