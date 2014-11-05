package it.ascia.calendar;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import it.ascia.ais.DevicePort;

public class CalendarPort extends DevicePort {

	public CalendarPort(String portId) {
		super(portId);
	}

	@Override
	protected Object normalize(Object newValue) throws IllegalArgumentException {
		if (newValue == null) {
			return newValue;
		} else if (Long.class.isInstance(newValue)) {
			return newValue;
		} else if (Calendar.class.isInstance(newValue)) {
			return (Long)((Calendar)newValue).getTimeInMillis();
		} else {
			throw(new IllegalArgumentException("Cannot set to "+newValue));
		}
	}
	
	@Override
	public String getStringValue() {
		Calendar calendar = Calendar.getInstance(getTimeZone(),getLocale());
		Long timeInMillis = (Long) getCachedValue();
		if (timeInMillis == null) {
			return null;
		}
		calendar.setTimeInMillis(timeInMillis);
		return DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL, getLocale()).format(calendar.getTime());
	}
	
	private TimeZone getTimeZone() {
		return ((CalendarDevice)getDevice()).getTimeZone();
	}

	Locale getLocale() {
		return ((CalendarDevice)getDevice()).getLocale();
	}

}
