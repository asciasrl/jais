package it.ascia.aui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Map.Entry;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.configuration.tree.xpath.XPathExpressionEngine;
import org.jabsorb.JSONRPCBridge;
// TODO Eliminare riferimenti a jsonsimple (usare org.json di jabsorb)
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import it.ascia.ais.AISException;
import it.ascia.ais.Address;
import it.ascia.ais.CommandInterface;
import it.ascia.ais.Controller;
import it.ascia.ais.ControllerModule;
import it.ascia.ais.Device;
import it.ascia.ais.DevicePort;

public class AUIControllerModule extends ControllerModule {
	
	public static ResourceBundle messages = ResourceBundle.getBundle("it.ascia.aui.messages");
		
	public AUIControllerModule() {
		super();
	}
	
	public void start() {
		super.start();
		Controller c = Controller.getController();
		// TODO registrazione automatica comandi
		c.registerCommand("get", new getCommand());
		c.registerCommand("getAll", new getAllCommand());
		//c.registerCommand("getPages", new getPagesCommand());
		//c.registerCommand("getPorts", new getPortsCommand());
		c.registerCommand("send", new sendCommand());
		c.registerCommand("set", new setCommand());

		// FIXME JSONRPCBridge.getGlobalBridge().registerCallback(new AUIInvocationCallback(), HttpServletRequest.class);
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
					SubnodeConfiguration typeConfig = auiConfig.configurationAt("controls."+type);
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
	 * Proprieta' dei controlli presenti nelle mappe 
	 * @return JSONString
	 */
	public String getControls() {
		JSONObject j =new JSONObject();
		HierarchicalConfiguration auiConfig = getConfiguration();
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
				if (auiConfig.containsKey("controls."+type+".default")) {
					SubnodeConfiguration typeConfig = auiConfig.configurationAt("controls."+type);
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
		return j.toJSONString();
	}
	
	public String getPageLayerControls() {
		JSONObject j =new JSONObject();
		HierarchicalConfiguration auiConfig = getConfiguration();
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
				String layer = controlConfig.getString("layer");
				if (jp.containsKey(layer)) {
					((JSONArray) jp.get(layer)).add("control-" + id);
				} else {
					JSONArray jl = new JSONArray();
					jl.add("control-" + id);
					jp.put(layer, jl);
				}
			}
		}
		logger.trace("getPageLayerControls");
		return j.toJSONString();		
	}
	
	/**
	 * Controlli corrispondenti agli indirizzi 
	 * @return JSONString
	 */
	public String getAddresses() {
		JSONObject j =new JSONObject();
		HierarchicalConfiguration auiConfig = getConfiguration();
		List pages = auiConfig.configurationsAt("pages.page");
		for (Iterator iPages = pages.iterator(); iPages.hasNext(); ) {
			HierarchicalConfiguration pageConfig = (HierarchicalConfiguration) iPages.next();
			String pageId = pageConfig.getString("[@id]");
			List controls = pageConfig.configurationsAt("control");
			for (Iterator ic = controls.iterator(); ic.hasNext(); ) {
				HierarchicalConfiguration controlConfig = (HierarchicalConfiguration) ic.next();
				String id = "control-" + pageId + "-" + controlConfig.getString("[@id]");
				String address = controlConfig.getString("address");
				if (address != null) {
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
		return j.toJSONString();
	}
	
	public String getAll() {
		return get("*.*:*");
	}

	public String get(String fullAddress) {
		JSONArray ja = new JSONArray();
		try {
			Address addr = new Address(fullAddress);
			String portId = addr.getPortId();
			Collection<Device> devices = controller.getDevices(addr);
			for (Device d : devices) {
				LinkedHashMap m = new LinkedHashMap();
				m.put("Class",d.getClass().getSimpleName());
				m.put("Address",d.getFullAddress());
				m.put("Info",d.getInfo());
				m.put("Status", d.getStatus(portId,0));
				ja.add(m);
			}			
		} catch (Exception e) {
			logger.error("get Error: ",e);
		}
		return ja.toJSONString();
	}

	
	class sendCommand implements CommandInterface {
		public String execute(HashMap params) {
			if (params.size() == 0) {
				throw(new AISException("mancano parametri"));
			}
			Iterator iterator = params.entrySet().iterator();
			Entry entry = (Entry) iterator.next();
			String message = (String) entry.getKey();
			String value = (String) entry.getValue();
			if (controller.sendMessage(message,value)) {
				return "OK";
			} else {
				return "ERROR";
			}
		}
	}

	class getCommand implements CommandInterface {
		public String execute(HashMap params) {
			// Comando "get"
			String fullAddress = (String) params.get("address");
			if (fullAddress == null) {
				throw(new AISException("Parametro 'address' richiesto"));
			}
			return get(fullAddress);
		}
	}
	
	class getAllCommand implements CommandInterface {
		public String execute(HashMap params) {
			return getAll();
		} 
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
	
	/**
	 * @deprecated
	 */
	class getPagesCommand implements CommandInterface {
		public String execute(HashMap params) {
			JSONObject j = new JSONObject();
			j.putAll(getPages());
			return j.toJSONString();
		}		
	}
	
	/*
	public String getPorts() {
		JSONArray ja = new JSONArray();
		Device devices[] = controller.findDevices("*");
		for (int i = 0; i < devices.length; i++) {
			Device d = devices[i];
			DevicePort[] ports = d.getPorts();
			for (int j = 0; j < ports.length; j++) {
				LinkedHashMap m = new LinkedHashMap();
				DevicePort p = ports[j];
				m.put("Address",p.getFullAddress());
				m.put("Class",p.getClass().getSimpleName());
				//m.put("DeviceClass",d.getClass().getSimpleName());
				//m.put("DeviceInfo",d.getInfo());
				m.put("Name",p.getName());						
				ja.add(m);						
			}
			
		}			
		return ja.toString();
	}
	*/
	
	/*
	class getPortsCommand implements CommandInterface {
		public String execute(HashMap params) {
			return getPorts();
		} 
	}
	*/

	public String set(HashMap params) {
		// Comando "set"
		if (params.size() == 0) {
			throw(new AISException("mancano parametri"));
		}
		Iterator iterator = params.entrySet().iterator();
		Entry entry = (Entry) iterator.next();
		Address address = new Address((String) entry.getKey());
		if (address == null) {
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
	public List getPagePorts(String page) {
		List ports = new ArrayList();
		Controller controller = Controller.getController();
		HierarchicalConfiguration auiConfig = getConfiguration();
		auiConfig.setExpressionEngine(new XPathExpressionEngine());
		HierarchicalConfiguration pageConfig = auiConfig.configurationAt("pages/page[@id='"+page+"']");
		List controls = pageConfig.configurationsAt("control");
		for (Iterator ic = controls.iterator(); ic.hasNext(); ) {
			HierarchicalConfiguration controlConfig = (HierarchicalConfiguration) ic.next();
			List addresses = controlConfig.configurationsAt("address");
			for (int i = 0; i < addresses.size(); i++) {
				Address address = new Address((String) ((SubnodeConfiguration) addresses.get(i)).getRoot().getValue());
				DevicePort p = controller.getDevicePort(address);
				if (p != null) {
					ports.add(p);
				}
			}
		}
		return ports;
	}

	public String getImagesPath() {
		return getConfiguration().getString("images","/images/");
	}

}
