package it.ascia.webconsole;

import java.util.HashMap;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import it.ascia.ais.AISException;
import it.ascia.ais.Address;
import it.ascia.ais.Controller;
import it.ascia.ais.Device;
import it.ascia.ais.DevicePort;
import it.ascia.ais.port.StatePort;

public class WebconsoleRPCServer {

	private Logger logger;

	public WebconsoleRPCServer(
			WebconsoleControllerModule webconsoleControllerModule) {
		logger = Logger.getLogger(getClass());	
	}
	
	public Vector<HashMap<String,String>> getAllPorts() {
		Vector<Device> devices = Controller.getController().getDevices(new Address());
		if (devices.size() == 0) {
			throw(new AISException("Nessun device trovato"));
		}		
		Vector<HashMap<String,String>> res = new Vector<HashMap<String,String>>();
		for (Device device : devices) {
			for (DevicePort p : device.getPorts()) {
				HashMap<String,String> res1 = new HashMap<String,String>();
				res1.put("Address",p.getAddress().toString());			
				res1.put("ClassName",p.getClass().getCanonicalName());
				res1.put("SimpleClassName",p.getClass().getSimpleName());
				if (StatePort.class.isInstance(p)) {
					res1.put("Tags",StringUtils.join(((StatePort)p).getTags(),";"));					
				}
				Object value = p.getCachedValue();
				if (value == null) {
					res1.put("Value","null");
				} else {
					res1.put("Value",p.getStringValue());
				}
				res.add(res1);
			}
		}
		return res;
	
	}
	
	public boolean writePortValue(String address, Object newValue) {
		return writePortValue(new Address(address), newValue);
	}

	public boolean writePortValue(Address address, Object newValue) {
		logger.trace("Writing "+newValue+" to "+address);
		DevicePort port = Controller.getController().getDevicePort(address);
		return port.writeValue(newValue);
	}

}
