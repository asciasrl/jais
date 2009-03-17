package it.ascia.aui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.json.simple.JSONObject;
import org.mortbay.util.ajax.JSONObjectConvertor;

import it.ascia.ais.AISException;
import it.ascia.ais.ControllerModule;
import it.ascia.ais.Device;
import it.ascia.ais.DevicePortChangeEvent;

public class AUIControllerModule extends ControllerModule implements PropertyChangeListener {
	
	/**
	 * Code di streaming attive
	 */
	private ArrayList streams;
	
	public AUIControllerModule() {
		super();
		streams = new ArrayList();
	}

	public HierarchicalConfiguration getConfig() {
		HierarchicalConfiguration cfg = config.configurationAt("AUI");
		return cfg;
	}

	public void start() {
		controller.addPropertyChangeListener(this);
	}

	public void stop() {
		controller.removePropertyChangeListener(this);
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

	/**
	 * Proprieta' dei controlli presenti nelle mappe 
	 * @return JSONString
	 */
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
	
	/**
	 * Controlli corrispondenti agli indirizzi 
	 * @return JSONString
	 */
	public String getAddresses() {
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
				String address = controlConfig.getString("address");
				if (address != null) {
					j.put(address, id);
				}
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
				retval += devices[i].getStatus(timestamp);
			}
		} else if (command.equals("set")) {
			// Comando "set"
			String fullAddress = (String) params.get("address");
			if (fullAddress == null) {
				throw(new AISException("Parametro 'address' richiesto"));
			}
			String value = (String) params.get("value");
			if (value == null) {
				throw(new AISException("Parametro 'value' richiesto"));
			}
			// TODO semplificare
			String deviceAddress = controller.getDeviceFromAddress(fullAddress);
			String portId = controller.getPortFromAddress(fullAddress);
			Device devices[] = controller.findDevices(deviceAddress);
			if (devices.length == 1) {
				devices[0].poke(portId, value);
				retval = "OK";
			} else {
				throw(new AISException("ERROR: indirizzo ambiguo"));
			}			
		} else {
			throw(new AISException("ERROR: comando '"+command+"' non implementato.")); 
		}
		return retval; 		
	}

	/**
	 * Riceve un evento e lo inoltra alle code degli stream aperti
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		logger.info(evt.getPropertyName()+"="+evt.getOldValue()+" -> "+ evt.getNewValue());
		logger.debug("Stream aperti: "+streams.size());
		for (int i = 0; i < streams.size(); i++) {
			LinkedBlockingQueue q = (LinkedBlockingQueue) streams.get(i);
			q.offer(evt);
			logger.trace("Coda "+i+", offerto evento "+evt.toString());
		}
	}

	public void addStreamQueue(LinkedBlockingQueue q) {
		streams.add(q);		
	}

	public void removeStreamQueue(LinkedBlockingQueue q) {
		streams.remove(q);		
	}


}
