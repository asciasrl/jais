package it.ascia.ais.port;

/**
 * An integer port bounded to 0-100
 * @author Sergio
 *
 */
public class DimmerPort extends IntegerPort {

	public DimmerPort(String portId) {
		super(portId);
		minValue = 0;
		maxValue = 100;
	}
	
	protected Object normalize(Object newValue) throws IllegalArgumentException {
		if (newValue == null) {
			return null;
		} else if (String.class.isInstance(newValue)) {
			String s = (String) newValue;
			if (s.toLowerCase().equals("off") || s.toLowerCase().equals("on")) {
				return s;
			}
			try {
				int n = Integer.parseInt(s);
				return n;
			} catch (NumberFormatException e) {
				logger.error("Not a number:"+s);
			}
			throw(new IllegalArgumentException("Value of "+getAddress()+" cannot be "+newValue));				
		}
		return super.normalize(newValue);
	}

}
