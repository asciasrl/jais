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
import java.util.Map;
import java.util.Vector;

import javax.servlet.http.HttpSession;

import org.apache.commons.configuration.HierarchicalConfiguration;
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
		return p.getValue();
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
