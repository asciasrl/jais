package it.ascia.ais.port;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

import it.ascia.ais.DevicePort;

/**
 * Store java.util.Date value
 * @author Sergio
 *
 */
public class DatePort extends DevicePort {

	public DatePort(String portId) {
		super(portId);
	}

	@Override
	protected Object normalize(Object newValue) throws IllegalArgumentException {
		if (newValue == null) {
			return null;
		} else if (Date.class.isInstance(newValue)) {
			return (Date) newValue;
		} else if (String.class.isInstance(newValue)) {
			DateFormat df = DateFormat.getDateTimeInstance();				
			try {
				Date date = df.parse((String) newValue);
				return date;
			} catch (ParseException e) {
				throw(new IllegalArgumentException(e));
			}
		} else {
			throw(new IllegalArgumentException("Value of "+getAddress()+" cannot be a "+newValue.getClass().getCanonicalName()));
		}
	}

}
