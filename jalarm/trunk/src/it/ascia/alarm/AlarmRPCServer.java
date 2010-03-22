/**
 * (C) 2009 Ascia S.r.l.
 */
package it.ascia.alarm;

import java.io.Serializable;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

/**
 * Server RPC, espone i metodi usati dal client RPC
 * @author Sergio
 *
 */
public class AlarmRPCServer implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8944342209414623443L;

	private static final String ATTRIBUTE_PIN = "Alarm.pin";

    protected Logger logger;
    
	private AlarmControllerModule module; 
	
	public AlarmRPCServer(AlarmControllerModule controllerModule ) {
		logger = Logger.getLogger(getClass());		
		module = controllerModule;
	}
	
	/**
	 * Check PIN and save in the session
	 * @param session
	 * @param pin
	 * @return
	 */
	public boolean pin(HttpSession session, String pin) {
		if (module.checkPin(pin)) {
			session.setAttribute(ATTRIBUTE_PIN,pin);
			return true;
		} else {
			session.removeAttribute(ATTRIBUTE_PIN);
			return false;
		}
	}
	
	public boolean isArmed(HttpSession session) {
		return module.isArmed();
	}

	public boolean toggle(HttpSession session) {
		return module.toggle((String) session.getAttribute(ATTRIBUTE_PIN));
	}
	
	public boolean arm(HttpSession session) {
		return module.arm((String) session.getAttribute(ATTRIBUTE_PIN));
	}	

	public boolean disarm(HttpSession session) {
		return module.disarm((String) session.getAttribute(ATTRIBUTE_PIN));
	}	

}
