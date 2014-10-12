package it.ascia.calendar;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Locale;

import it.ascia.ais.DevicePort;

public class CalendarPort extends DevicePort {

	public CalendarPort(String portId) {
		super(portId);
	}

	@Override
	protected Object normalize(Object newValue) throws IllegalArgumentException {
		if (Calendar.class.isInstance(newValue)) {
			return newValue;
		} else {
			throw(new IllegalArgumentException());
		}
	}
	
	public String getStringValue() {
		Calendar calendar = (Calendar) getCachedValue();
		logger.trace(calendar);
		
		return DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL, getLocale()).format(calendar.getTime());
	}
	
	Locale getLocale() {
		return ((CalendarDevice)getDevice()).getLocale();
	}

}
