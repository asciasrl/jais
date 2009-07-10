package it.ascia.aui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.configuration.tree.xpath.XPathExpressionEngine;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import it.ascia.ais.AISException;
import it.ascia.ais.Controller;
import it.ascia.ais.ControllerModule;
import it.ascia.ais.Device;
import it.ascia.ais.DevicePort;

public class AUIControllerModule extends ControllerModule {
		
	public AUIControllerModule() {
		super();
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
		JSONArray ja = new JSONArray();
		Device[] devices = controller.findDevices("*");		
		for (int i = 0; i < devices.length; i++) {
			Device d = devices[i]; 
			LinkedHashMap m = new LinkedHashMap();
			m.put("Class",d.getClass().getSimpleName());
			m.put("Address",d.getFullAddress());
			m.put("Info",d.getInfo());
			m.put("Status", d.getStatus());
			ja.add(m);
		}
		return ja.toJSONString();
	}
	
	public String doCommand(String command, HashMap params) throws AISException {
		String retval = "";
		if (command.equals("getControls")) {
			if (params.containsKey("mapId")) {
				String mapId = (String) params.get("mapId");
				retval = getMapControls(mapId);
			} else {
				retval = getControls();
			}
		} else if (command.equals("send")) {
			if (params.size() == 0) {
				throw(new AISException("mancano parametri"));
			}
			Iterator iterator = params.entrySet().iterator();
			Entry entry = (Entry) iterator.next();
			String message = (String) entry.getKey();
			String value = (String) entry.getValue();
			if (controller.sendMessage(message,value)) {
				retval = "OK";
			} else {
				retval = "ERROR";
			}
		} else if (command.equals("get")) {
			// Comando "get"
			String fullAddress = (String) params.get("address");
			if (fullAddress == null) {
				throw(new AISException("Parametro 'address' richiesto"));
			}
			// TODO semplificare
			String deviceAddress = controller.getDeviceFromAddress(fullAddress);
			String portName = controller.getPortFromAddress(fullAddress);
			Device devices[] = controller.findDevices(deviceAddress);
			if (devices.length > 0) {
				retval = "";
				for (int i = 0; i < devices.length; i++) {
					retval += devices[i].getStatus(portName, 0);
				}
			} else {
				retval = "ERROR: address " + fullAddress + " not found.";
			}
		} else if (command.equals("getAll")) {
			return getAll();
		} else if (command.equals("set")) {
			// Comando "set"
			if (params.size() == 0) {
				throw(new AISException("mancano parametri"));
			}
			Iterator iterator = params.entrySet().iterator();
			Entry entry = (Entry) iterator.next();
			String fullAddress = (String) entry.getKey();
			if (fullAddress == null) {
				throw(new AISException("Parametro 'address' richiesto"));
			}
			String value = (String) entry.getValue();				
			if (value == null) {
				throw(new AISException("Parametro 'value' richiesto"));
			}
			DevicePort p = controller.getDevicePort(fullAddress);
			if (p == null) {
				throw(new AISException("Port not found"));
			}			
			if (p.writeValue(value)) {
				retval = "OK";
			} else {
				retval = "ERROR";
			}
		} else {
			throw(new AISException("comando '"+command+"' non implementato.")); 
		}
		return retval; 		
	}

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
				String address = (String) ((SubnodeConfiguration) addresses.get(i)).getRoot().getValue();
				DevicePort p = controller.getDevicePort(address);
				if (p != null) {
					ports.add(p);
				}
			}
		}
		return ports;
	}
	
	public void addPage(String id, String title) {
		if (configuration.getProperty("pages.page[@id='"+id+"']") != null) {
			throw(new AISException("Duplicated page id="+id));
		}
		configuration.addProperty("pages.page(-1).id", id);	
	}	

}
