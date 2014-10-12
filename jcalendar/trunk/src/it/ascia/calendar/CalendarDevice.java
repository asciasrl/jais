package it.ascia.calendar;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;
import com.luckycatlabs.sunrisesunset.dto.Location;

import it.ascia.ais.AISException;
import it.ascia.ais.Device;
import it.ascia.ais.DevicePort;
import it.ascia.ais.port.BooleanPort;
import it.ascia.ais.port.StringPort;

public class CalendarDevice extends Device {

	static final String CURRENT_TIME_PORT = "CurrentTime";
	static final String TIMEZONE_IDENTIFIER_PORT = "TimeZone";
	static final String LOCALE_IDENTIFIER_PORT = "Locale";
	static final String LATITUDE_PORT = "latitude";
	static final String LONGITUDE_PORT = "longitude";
	
	private final String ASTRONOMICAL_SUNRISE = "AstronomicalSunrise";
	private final String ASTRONOMICAL_SUNSET = "AstronomicalSunset";
	private final String ASTRONOMICAL_DAYTIME = "AstronomicalDaytime";

	private SunriseSunsetCalculator sun;

	public CalendarDevice(String name) {
		super(name);
		addPort(new CalendarPort(CURRENT_TIME_PORT));
		addPort(new StringPort(TIMEZONE_IDENTIFIER_PORT));
		addPort(new StringPort(LOCALE_IDENTIFIER_PORT));
		addPort(new StringPort(LATITUDE_PORT));
		addPort(new StringPort(LONGITUDE_PORT));
		addPort(new CalendarPort(ASTRONOMICAL_SUNRISE));
		addPort(new CalendarPort(ASTRONOMICAL_SUNSET));
		addPort(new BooleanPort(ASTRONOMICAL_DAYTIME));
	}

	@Override
	public boolean sendPortValue(String portId, Object newValue)
			throws AISException {
		// TODO Auto-generated method stub
		throw(new AISException("unimplemented"));
	}
	
	

	@Override
	public boolean updatePort(String portId) throws AISException {
		if (sun==null) {
			sun = new SunriseSunsetCalculator(new Location(getLongitude(),getLongitude()), getTimeZoneIdentifier());
		}
		if (portId.equals(CURRENT_TIME_PORT)) {
			setPortValue(portId, getCalendar());
		} else if (portId.equals(ASTRONOMICAL_SUNRISE)) {
			setPortValue(portId, getAstronomicalSunrise()); 					
			return true;
		} else if (portId.equals(ASTRONOMICAL_SUNSET)){
			setPortValue(portId, getAstronomicalSunset());
			return true;
		} else if (portId.equals(ASTRONOMICAL_DAYTIME)){
			setPortValue(portId, isAstronomicalDaytime());
			return true;
		}
		DevicePort p = getPort(portId);
		p.setValue(p.getCachedValue());
		return true;
	}

	void setTimeZoneIdentifier(String timezoneIdentifier) {
		if (timezoneIdentifier == null) {
			timezoneIdentifier = TimeZone.getDefault().getID();
		} else {
			timezoneIdentifier = TimeZone.getTimeZone(timezoneIdentifier).getID();
		}
		setPortValue(TIMEZONE_IDENTIFIER_PORT, timezoneIdentifier);		
	}

	String getTimeZoneIdentifier() {
		return (String) getPortCachedValue(TIMEZONE_IDENTIFIER_PORT);
	}

	TimeZone getTimeZone() {
		return TimeZone.getTimeZone(getTimeZoneIdentifier());
	}
	
	/**
	 * 
	 * @return The Calendar returned is based on the current time in the device time zone with the device locale.
	 */
	Calendar getCalendar() {
		return Calendar.getInstance(getTimeZone(),getLocale());
	}
	
	Date getDate() {
		return getCalendar().getTime();
	}
	
	long getTime() {
		return getDate().getTime();
	}

	Locale getLocale() {
		String localeidentifier = (String) getPortCachedValue(LOCALE_IDENTIFIER_PORT);
		if (localeidentifier == null) {
			return Locale.getDefault();
		} else {
			return Locale.forLanguageTag(localeidentifier);
		}
	}

	public void setLatitude(String latitude) {
		setPortValue(LATITUDE_PORT, latitude);
	}

	public void setLongitude(String longitude) {
		setPortValue(LONGITUDE_PORT, longitude);
	}
	
	public String getLatitude() {
		return (String) getPortCachedValue(LATITUDE_PORT);
	}

	public String getLongitude() {
		return (String) getPortCachedValue(LONGITUDE_PORT);
	}

	private Calendar getAstronomicalSunset() {
		return sun.getAstronomicalSunsetCalendarForDate(getCalendar());
	}

	private Calendar getAstronomicalSunrise() {
		return sun.getAstronomicalSunriseCalendarForDate(getCalendar());
	}

	private boolean isAstronomicalDaytime() {
		Date date = getDate(); 
		Date dateStart = getAstronomicalSunrise().getTime();
		Date dateEnd = getAstronomicalSunset().getTime();
	    if (date != null && dateStart != null && dateEnd != null) {
	        if (date.after(dateStart) && date.before(dateEnd)) {
	            return true;
	        } else {
	            return false;
	        }
	    }
	    return false;
	}

	public void setLocale(String localeIdentifier) {
		setPortValue(LOCALE_IDENTIFIER_PORT, localeIdentifier);		
	}



}
