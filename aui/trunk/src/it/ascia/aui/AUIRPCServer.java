/**
 * (C) 2009 Ascia S.r.l.
 */
package it.ascia.aui;

import it.ascia.ais.AISException;
import it.ascia.ais.Address;
import it.ascia.ais.Controller;
import it.ascia.ais.Device;
import it.ascia.ais.DevicePort;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.servlet.http.HttpSession;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.configuration.tree.xpath.XPathExpressionEngine;
import org.apache.log4j.Logger;
import org.jabsorb.JSONRPCBridge;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

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
		return aui.isLogged(session);
	}
	
	/**
	 * Check user credential and enable access to AUIConfig methods
	 * @param session
	 * @param username
	 * @param password
	 * @return
	 */
	public boolean login(HttpSession session, String username, String password) {
		HierarchicalConfiguration auiConfig = aui.getConfiguration();
		auiConfig.setExpressionEngine(new XPathExpressionEngine());
		if (aui.login(session, username, password)) {
			JSONRPCBridge bridge = new JSONRPCBridge();
			session.setAttribute("JSONRPCBridge", bridge);
			bridge.registerObject("AUIConfig", new AUIConfigRPCServer(aui));
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
	
	public JSONObject getControls() {
		JSONObject j =new JSONObject();
		HierarchicalConfiguration auiConfig = aui.getConfiguration();
		HierarchicalConfiguration skinConfig = aui.getSkinConfiguration();
		List pages = auiConfig.configurationsAt("pages.page");
		for (Iterator iPages = pages.iterator(); iPages.hasNext(); ) {
			HierarchicalConfiguration pageConfig = (HierarchicalConfiguration) iPages.next();
			String pageId = pageConfig.getString("[@id]");
			List controls = pageConfig.configurationsAt("control");
			for (Iterator ic = controls.iterator(); ic.hasNext(); ) {
				HierarchicalConfiguration controlConfig = (HierarchicalConfiguration) ic.next();
				String id = "control-" + pageId + "-" + controlConfig.getString("[@id]");
				String type = controlConfig.getString("type");
				HashMap controlMap = new HashMap();
				if (skinConfig.containsKey("controls."+type+".default")) {
					SubnodeConfiguration typeConfig = skinConfig.configurationAt("controls."+type);
					for (Iterator ip = typeConfig.getKeys(); ip.hasNext(); ) {
						String k = (String) ip.next();
						controlMap.put(k, typeConfig.getString(k));
					}
				}
				for (Iterator ip = controlConfig.getKeys(); ip.hasNext(); ) {
					String k = (String) ip.next();
					controlMap.put(k, controlConfig.getString(k));
				}
				j.put(id, controlMap);
			}
		}
		return j;
	}
	
	public JSONObject getAddresses() {
		JSONObject j =new JSONObject();
		HierarchicalConfiguration auiConfig = aui.getConfiguration();
		List pages = auiConfig.configurationsAt("pages.page");
		for (Iterator iPages = pages.iterator(); iPages.hasNext(); ) {
			HierarchicalConfiguration pageConfig = (HierarchicalConfiguration) iPages.next();
			String pageId = pageConfig.getString("[@id]");
			List controls = pageConfig.configurationsAt("control");
			for (Iterator ic = controls.iterator(); ic.hasNext(); ) {
				HierarchicalConfiguration controlConfig = (HierarchicalConfiguration) ic.next();
				String id = "control-" + pageId + "-" + controlConfig.getString("[@id]");
				List<String> addresses = controlConfig.getList("address");
				if (addresses.size() > 0) {
					for (String address : addresses) {
						if (j.containsKey(address)) {
							((JSONArray) j.get(address)).add(id);
						} else {
							JSONArray ja = new JSONArray();
							ja.add(id);
							j.put(address, ja);
						}
					}
				}
			}
		}
		return j;
	}
	
	public JSONObject getPageLayerControls() {
		JSONObject j =new JSONObject();
		HierarchicalConfiguration auiConfig = aui.getConfiguration();
		List pages = auiConfig.configurationsAt("pages.page");
		for (Iterator iPages = pages.iterator(); iPages.hasNext(); ) {
			HierarchicalConfiguration pageConfig = (HierarchicalConfiguration) iPages.next();
			String pageId = pageConfig.getString("[@id]");
			JSONObject jp = new JSONObject();
			j.put("page-" + pageId, jp);
			List controls = pageConfig.configurationsAt("control");
			for (Iterator ic = controls.iterator(); ic.hasNext(); ) {
				HierarchicalConfiguration controlConfig = (HierarchicalConfiguration) ic.next();
				String id = pageId + "-" + controlConfig.getString("[@id]");
				String layer = controlConfig.getString("layer","");
				if (layer.equals("")) {
					layer = "null";
				}
				if (jp.containsKey(layer)) {
					((JSONArray) jp.get(layer)).add("control-" + id);
				} else {
					JSONArray jl = new JSONArray();
					jl.add("control-" + id);
					jp.put(layer, jl);
				}
			}
		}
		return j;				
	}
	
	/**
	 * End config session
	 * @param session
	 * @return
	 */
	public boolean logout(HttpSession session) {
		logger.debug("Invalidate session "+session.getId());
		session.invalidate();
		return true;
	}
	
	/**
	 * Read cached value of the port
	 * @param session
	 * @param fullAddress
	 * @return
	 */
	public Object getPortValue(HttpSession session,String fullAddress) {
		DevicePort p = controller.getDevicePort(new Address(fullAddress));
		if (p == null) {
			throw(new AISException("Porta non trovata: "+fullAddress));
		}
		return p.getCachedValue();
	}

	/**
	 * Search for ports matching address and return values
	 * @param session
	 * @param fullAddress Address of ports to find
	 * @return
	 */
	public Map<String,Object> getPortsValue(HttpSession session,String fullAddress) {
		Address addr = new Address(fullAddress);
		Vector<Device> devices = controller.getDevices(addr);
		if (devices.size() == 0) {
			throw(new AISException("Nessun device trovato: "+fullAddress));
		}		
		HashMap<String,Object> res = new HashMap<String,Object>();
		for (Device device : devices) {
			for (DevicePort p : device.getPorts()) {
				res.put(p.getAddress().toString(),p.getCachedValue());			
			}
		}
		return res;
	}

	/**
	 * Write a new value to a port
	 * @param session
	 * @param fullAddress
	 * @param newValue
	 * @return
	 */
	public boolean writePortValue(HttpSession session,String fullAddress, Object newValue) {
		DevicePort p = controller.getDevicePort(new Address(fullAddress));
		if (p == null) {
			throw(new AISException("Porta non trovata: "+fullAddress));
		}
		logger.info("writePortValue: "+fullAddress+"="+newValue);
		return p.writeValue(newValue);
	}
	
}
