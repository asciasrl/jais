package it.ascia.ais;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * TODO gestire comandi:
 * - doCommand deve chiamare writePort di Device o di DevicePort ?  
 * - levare metodo writePort di device ?
 * - delegare il writePort a sottoclassi di DevicePort ?
 * - fare BinaryOutputPort, BinaryInputPort, DimmerPort, BlindPort ?
 * - il metodo writeValue delle sottoclassi di DevicePort deve comunque usare writePort di Device 
 * @author Sergio
 *
 */
public class BlindPort extends DevicePort implements PropertyChangeListener {
	
	private DevicePort closePort; 
	private DevicePort openPort; 
	
	public BlindPort(Device device, String portId, String closePortId, String openPortId) throws AISException {
		super(device,portId);
		closePort = device.getPort(closePortId);
		openPort = device.getPort(openPortId);
		closePort.addPropertyChangeListener(this);
		openPort.addPropertyChangeListener(this);
	}

	public Object getValue() {	
		return getValue(false);
	}
	
	/**
	 * 
	 * @param useCache Se true usa i valori cache delle porte fisiche per calcolare il suo stato
	 * @return
	 */
	public Object getValue(boolean useCache) {
		try {
			Object cachedValue = getCachedValue();
			boolean opening;
			boolean closing;
			try {
				if (useCache) {
					opening = ((Boolean) openPort.getCachedValue()).booleanValue();
					closing = ((Boolean) closePort.getCachedValue()).booleanValue();					
				} else {
					opening = ((Boolean) openPort.getValue()).booleanValue();
					closing = ((Boolean) closePort.getValue()).booleanValue();
				}
			} catch (NullPointerException e) {
				if (!useCache) {
					logger.warn("getValue("+portId+"): stato porte open e/o close non determinato");
				}
				return null; 
			}
			logger.debug(portId+" cachedValue="+cachedValue+" opening("+openPort.getPortId()+")="+opening+" closing("+closePort.getPortId()+")="+closing);
			String newValue = null;
			if (!opening && !closing) {
				if (cachedValue == null) {
					newValue="stopped";
				} else if (cachedValue.equals("closing")) {
					newValue="closed";						
				} else if (cachedValue.equals("opening")) {
					newValue="opened";
				} else {				
					newValue = (String) cachedValue;
				}
			} else if (opening) {
				newValue="opening";
			} else if (closing) {
				newValue="closing";
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
	
	public boolean isDirty()
	{
		return openPort.isDirty() || closePort.isDirty();
	}

	public boolean isExpired()
	{
		return openPort.isExpired() || closePort.isExpired();
	}

	public void propertyChange(PropertyChangeEvent evt) {
		getValue(true);
	}

	public String[] getTags() {
		return new String[] {"open","close","stop"};
	}

	public boolean writeValue(Object newValue) throws IllegalArgumentException {
		if (String.class.isInstance(newValue)) {
			return writeValue((String) newValue);
		}
		throw new IllegalArgumentException(getFullAddress() + " Tipo di valore non valido: "+newValue.getClass().getName());
	}

	public boolean writeValue(String text) throws IllegalArgumentException {
		if (text.toLowerCase().equals("open")) {
			if (Boolean.TRUE.equals(closePort.getCachedValue())) {
				closePort.invalidate();
			}
			return openPort.writeValue(new Boolean(true));
		} else if (text.toLowerCase().equals("close")) {
			if (Boolean.TRUE.equals(openPort.getCachedValue())) {
				//openPort.writeValue(new Boolean(false));
			}
			openPort.invalidate();
			return closePort.writeValue(new Boolean(true));
		} else if (text.toLowerCase().equals("stop")) {
			if (Boolean.TRUE.equals(openPort.getCachedValue())) {
				return openPort.writeValue(new Boolean(false));
			} else if (Boolean.TRUE.equals(closePort.getCachedValue())) {
				return closePort.writeValue(new Boolean(false));			
			} else {
				return true;
			}
		} else {
			throw new IllegalArgumentException(getFullAddress()+ " valore non valido: " + text);
		}
	}

}
