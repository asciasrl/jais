package it.ascia.webconsole;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import org.apache.log4j.Logger;

import it.ascia.ais.AISException;
import it.ascia.ais.Address;
import it.ascia.ais.Controller;
import it.ascia.ais.Device;
import it.ascia.ais.DevicePort;

public class WebconsoleRPCServer {

	private Logger logger;

	public WebconsoleRPCServer(
			WebconsoleControllerModule webconsoleControllerModule) {
		// TODO Auto-generated constructor stub
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
				res1.put("Class",p.getClass().getCanonicalName());
				Object value = p.getValue();
				if (value == null) {
					res1.put("Value","null");
				} else {
					res1.put("Value",value.toString());
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
		DevicePort port = Controller.getController().getDevicePort(address);
		return port.writeValue(newValue);
	}

}
