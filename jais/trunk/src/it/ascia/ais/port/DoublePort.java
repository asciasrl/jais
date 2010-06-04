package it.ascia.ais.port;

import it.ascia.ais.DevicePort;

/**
 * Store java.lang.Double value, optionally bounded
 * @author Sergio
 *
 */
public class DoublePort extends DevicePort {

	Double minValue = null;
	Double maxValue = null;

	public DoublePort(String portId) {
		super(portId);
	}

	public DoublePort(String portId, Double minValue, Double maxValue) {
		this(portId);
		this.minValue = minValue;
		this.maxValue = maxValue;		
	}

	@Override
	protected Object normalize(Object newValue) throws IllegalArgumentException {
		if (newValue == null) {
			return null;
		} else if (Integer.class.isInstance(newValue)) {
			return normalize(((Integer)newValue).doubleValue());
		} else if (Double.class.isInstance(newValue)) {
			Double doubleValue = (Double) newValue;
			if (minValue != null && minValue.compareTo(doubleValue) > 0) {
				throw(new IllegalArgumentException("Value of "+getAddress()+"("+doubleValue+") cannot be less than "+minValue));
			}
			if (maxValue != null && maxValue.compareTo(doubleValue) < 0) {
				throw(new IllegalArgumentException("Value of "+getAddress()+"("+doubleValue+") cannot be more than "+maxValue));
			}
			return doubleValue;
		} else {
			throw(new IllegalArgumentException("Value of "+getAddress()+" cannot be a "+newValue.getClass().getCanonicalName()));
		}
	}


}
