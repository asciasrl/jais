/**
 * Copyright (C) 2008-2012 ASCIA S.r.l.
 * $Id$
 */
package it.ascia.ais;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;

/**
 * Implementazione minima della interfaccia connettore
 * @author Sergio
 */
public class ConnectorImpl implements ConnectorInterface {

    /**
     * Il nostro nome secondo AUI.
     */
    private String name;
    
	/**
	 * I dispositivi presenti nel sistema gestiti da questo connettore.
	 * Se un device corrisponde a piu' indirizzi va aggiunto piu' volte
	 */
    Map<String,Device> devices = Collections.synchronizedMap(new LinkedHashMap<String, Device>());

	/**
     * Il nostro logger.
     */
    public Logger logger;
    
    /**
     * Stato del connettore
     */
    private boolean running = false;

    /**
     * Modulo a cui appartiene il connettore
     */
	private ControllerModule module = null;

	/**
	 * 
	 * @param name Nome del connettore
	 */
	public ConnectorImpl(String name) {
		if (name == null) {
			throw(new AISException("Name of connector cannot be null"));
		}
		Address.testConnectorName(name);
		this.name = name;
		logger = Logger.getLogger(getClass());
	}

	@Override
	public String getConnectorName() {
		return name;
	}


	@Override
    public void addDevice(String deviceAddress, Device device) throws AISException {
    	if (devices.containsKey(deviceAddress)) {
    		if (devices.get(deviceAddress).equals(device)) {
    			logger.trace("Already added device "+device);
    		} else {
    			throw(new AISException("Address "+deviceAddress + " already used by " + devices.get(deviceAddress)));
    		}
    	} else {
	    	devices.put(deviceAddress, device);
			if (device.getConnector() == null) {
				device.setConnector(this);
			}
    	}
    }

	@Override
    public void addDevice(Device device) throws AISException {
		addDevice(device.getDeviceAddress(), device);
	}
    
	@Override
    public Device getDevice(String deviceAddress) {
    	return devices.get(deviceAddress);
    }

	/**
     * Ritorna tutti i Device collegati che rispondono a un certo indirizzo.
     * 
     * @param addr l'indirizzo da cercare.
     * @return Elenco devices (eventualmente vuoto)
     */
    public Set<Device> getDevices(Address addr) {
    	Set<Device> res = new HashSet<Device>();
    	Device device1 = getDevice(addr.getDeviceAddress());
    	if (device1 != null) {
    		res.add(device1);
    	} else {
			for (Device device : devices.values()) {
				if (addr.matches(device.getAddress())) {
					res.add(device);
				}
			}
    	}
    	return res;    	
    }

    /**
     * 
     * @return all devices belonging to the connector
     */
    public Set<Device> getDevices() {
		return new HashSet<Device>(devices.values());
    }

	public boolean isRunning() {
		return running;
	}

	@Override
	public void start() {
		running = true;		
	}

	@Override
	public void stop() {
		running = false;
	}

	public void setModule(ControllerModule controllerModule) {
		this.module = controllerModule;		
	}

	public ControllerModule getModule() {
		if (module == null) {
			throw(new IllegalStateException("Connector " + getConnectorName() + " not assigned to a module"));
		}
		return module;
	}

	@Override
	public boolean isAlive() {
		return isRunning();
	}

	@Override	
	public void queueUpdate(DevicePort devicePort) {
		// FIXME Auto-generated method stub
		logger.error("FIXME Auto-generated method stub");
	}

	@Override
	public boolean sendMessage(Message m) {
		// FIXME Auto-generated method stub
		logger.error("FIXME Auto-generated method stub");
		return false;
	}

	@Override
	public void received(int i) {
		// FIXME Auto-generated method stub
		logger.error("FIXME Auto-generated method stub");
	}

}
