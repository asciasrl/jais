package it.ascia.aui;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.json.simple.JSONObject;
import org.mortbay.log.Log;

import it.ascia.ais.AISException;
import it.ascia.ais.ControllerModule;
import it.ascia.ais.Device;
import it.ascia.ais.DeviceEvent;

public class AUIControllerModule extends ControllerModule  {

	public HierarchicalConfiguration getConfig() {
		HierarchicalConfiguration cfg = config.configurationAt("AUI");
		return cfg;
	}

	public void onDeviceEvent(DeviceEvent event) {
		// TODO Auto-generated method stub
		
	}

	public void start() {
		// TODO Auto-generated method stub
		
	}

	public void stop() {
		// TODO Auto-generated method stub
	}
	
	/**
	 * 
	 * @param mapId
	 * @return
	 */
	public String getMapControls(String mapId) {
		JSONObject j =new JSONObject();
		HierarchicalConfiguration auiConfig = getConfig();
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

	public String getControls() {
		JSONObject j =new JSONObject();
		HierarchicalConfiguration auiConfig = getConfig();
		List maps = auiConfig.configurationsAt("map");
		for (Iterator im = maps.iterator(); im.hasNext(); ) {
			HierarchicalConfiguration mapConfig = (HierarchicalConfiguration) im.next();
			String mapId = mapConfig.getString("id");
			List controls = mapConfig.configurationsAt("control");
			for (Iterator ic = controls.iterator(); ic.hasNext(); ) {
				HierarchicalConfiguration controlConfig = (HierarchicalConfiguration) ic.next();
				String id = "control-" + mapId + "-" + controlConfig.getString("id");
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

	public String doCommand(String command, HashMap params) throws AISException {
		String retval = "";
		if (command.equals("getControls")) {
			if (params.containsKey("mapId")) {
				String mapId = (String) params.get("mapId");
				retval = getMapControls(mapId);
			} else {
				retval = getControls();
			}
		} else if (command.equals("get")) {
			// Comando "get"
			String name = (String) params.get("name");
			if (name == null) {
				throw(new AISException("Parametro 'name' richiesto"));
			}
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
			if (params.containsKey("timestamp")) {
				try {
					timestamp = Long.parseLong((String) params.get("timestamp"));
				} catch (NumberFormatException e) {
					// Manteniamo il valore di default: zero
				}
			}
			for (int i = 0; i < devices.length; i++) {
				retval += devices[i].getStatus("*", timestamp);
			}
		} else if (command.equals("set")) {
			// Comando "set"
			String name = (String) params.get("name");
			if (name == null) {
				throw(new AISException("Parametro 'name' richiesto"));
			}
			String value = (String) params.get("value");
			if (value == null) {
				throw(new AISException("Parametro 'value' richiesto"));
			}
			String deviceAddress = controller.getDeviceFromAddress(name);
			String portName = controller.getPortFromAddress(name);
			Device devices[] = controller.findDevices(deviceAddress);
			if (devices.length == 1) {
				devices[0].poke(portName, value);
				retval = "OK";
			} else {
				throw(new AISException("ERROR: indirizzo ambiguo"));
			}			
		} else {
			throw(new AISException("ERROR: comando '"+command+"' non implementato.")); 
		}
		return retval; 		
	}	
	

}
