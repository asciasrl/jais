package it.ascia.ais.port;

import it.ascia.ais.DevicePort;

/**
 * Store java.lang.Integer value, optionally bounded
 * @author Sergio
 *
 */
public class IntegerPort extends DevicePort {

	Integer minValue = null;
	Integer maxValue = null;

	public IntegerPort(String portId) {
		super(portId);
	}

	public IntegerPort(String portId, Integer minValue, Integer maxValue) {
		this(portId);
		this.minValue = minValue;
		this.maxValue = maxValue;		
	}

	@Override
	protected Object normalize(Object newValue) throws IllegalArgumentException {
		if (newValue == null) {
			return null;
		} else if (Integer.class.isInstance(newValue)) {
			Integer integerValue = (Integer) newValue;
			if (minValue != null && minValue.compareTo(integerValue) > 0) {
				throw(new IllegalArgumentException("Value of "+getAddress()+"("+integerValue+") cannot be less than "+minValue));
			}
			if (maxValue != null && maxValue.compareTo(integerValue) < 0) {
				throw(new IllegalArgumentException("Value of "+getAddress()+"("+integerValue+") cannot be more than "+maxValue));
			}
			return integerValue;
		} else if (Long.class.isInstance(newValue)) {
			return normalize(((Long)newValue).intValue());
		} else if (Double.class.isInstance(newValue)) {
			return normalize(((Double)newValue).intValue());
		} else {
			throw(new IllegalArgumentException("Value of "+getAddress()+" cannot be a "+newValue.getClass().getCanonicalName()));
		}
	}


}
