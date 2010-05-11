package it.ascia.ais.port;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedHashMap;

import it.ascia.ais.AISException;

/**
 * SlaveBitStatePort is port which depends of status of others boolean ports
 * @author Sergio
 *
 */
public class SlaveBitStatePort extends StatePort implements PropertyChangeListener {
	
	private LinkedHashMap<String,BooleanPort> states = new LinkedHashMap<String,BooleanPort>();

	public SlaveBitStatePort(String portId) {
		super(portId, null);
	}
	
	public void addStatus(String tag, BooleanPort port) {
		states.put(tag, port);
		port.addPropertyChangeListener(this);
	}

	public void propertyChange(PropertyChangeEvent arg0) {
		getValue(true);
	}

	public Object getValue() {	
		return getValue(false);
	}

	public Object getValue(boolean useCache) {
		try {
			Object cachedValue = getCachedValue();
			String newValue = null;
			try {
				if (useCache) {
					for (String s : states.keySet()) {
						if (((Boolean)states.get(s).getCachedValue()).booleanValue()) {
							newValue = s;
							break;
						}
					}
				}
			} catch (NullPointerException e) {
				if (!useCache) {
					logger.warn("getValue("+portId+"): undetermined status");
				}
				return null; 
			}
			if (! newValue.equals(cachedValue)) {
				setValue(newValue);
			}
		} catch (NullPointerException e) {
			logger.warn("getValue("+portId+"):",e);
		} catch (AISException e) {
			logger.error("getValue("+portId+"):",e);
		}
		return getCachedValue();
	}

}
