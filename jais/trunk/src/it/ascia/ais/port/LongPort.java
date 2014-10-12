package it.ascia.ais.port;

import it.ascia.ais.DevicePort;

/**
 * Store java.lang.Long value, optionally bounded
 * @author Sergio
 *
 */
public class LongPort extends DevicePort {

	Long minValue = null;
	Long maxValue = null;

	public LongPort(String portId) {
		super(portId);
	}

	public LongPort(String portId, Long minValue, Long maxValue) {
		this(portId);
		this.minValue = minValue;
		this.maxValue = maxValue;		
	}

	@Override
	protected Object normalize(Object newValue) throws IllegalArgumentException {
		if (newValue == null) {
			return null;
		} else if (Long.class.isInstance(newValue)) {
			Long longValue = (Long) newValue;
			if (minValue != null && minValue.compareTo(longValue) > 0) {
				throw(new IllegalArgumentException("Value of "+getAddress()+"("+longValue+") cannot be less than "+minValue));
			}
			if (maxValue != null && maxValue.compareTo(longValue) < 0) {
				throw(new IllegalArgumentException("Value of "+getAddress()+"("+longValue+") cannot be more than "+maxValue));
			}
			return longValue;
		} else if (Integer.class.isInstance(newValue)) {
			return normalize(((Integer)newValue).intValue());
		} else if (Double.class.isInstance(newValue)) {
			return normalize(((Double)newValue).longValue());
		} else {
			throw(new IllegalArgumentException("Value of "+getAddress()+" cannot be a "+newValue.getClass().getCanonicalName()));
		}
	}


}
