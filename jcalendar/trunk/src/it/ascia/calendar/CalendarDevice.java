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

	static final String TIMEZONE_IDENTIFIER_PORT = "TimeZone";
	static final String LOCALE_IDENTIFIER_PORT = "Locale";
	static final String LATITUDE_PORT = "latitude";
	static final String LONGITUDE_PORT = "longitude";

    /** Astronomical sunrise/set is when the sun is 18 degrees below the horizon. */
	private final String ATRONOMICAL_SUNRISE = "AstronomicalSunrise";
	private final String ATRONOMICAL_SUNSET = "AstronomicalSunset";
	private final String ATRONOMICAL_DAYTIME = "AstronomicalDaytime";

    /** Nautical sunrise/set is when the sun is 12 degrees below the horizon. */
	private final String NAUTICAL_SUNRISE = "NauticalSunrise";
	private final String NAUTICAL_SUNSET = "NauticalSunset";
	private final String NAUTICAL_DAYTIME = "NauticalDaytime";

    /** Civil sunrise/set (dawn/dusk) is when the sun is 6 degrees below the horizon. */
	private final String CIVIL_SUNRISE = "CivilSunrise";
	private final String CIVIL_SUNSET = "CivilSunset";
	private final String CIVIL_DAYTIME = "CivilDaytime";

    /** Official sunrise/set is when the sun is 50' below the horizon. */
	private final String OFFICIAL_SUNRISE = "OfficialSunrise";
	private final String OFFICIAL_SUNSET = "OfficialSunset";
	private final String OFFICIAL_DAYTIME = "OfficialDaytime";

	private SunriseSunsetCalculator sun;

	public CalendarDevice(String name) {
		super(name);
		addPort(new StringPort(TIMEZONE_IDENTIFIER_PORT));
		addPort(new StringPort(LOCALE_IDENTIFIER_PORT));
		addPort(new StringPort(LATITUDE_PORT));
		addPort(new StringPort(LONGITUDE_PORT));

		addPort(new CalendarPort(ATRONOMICAL_SUNRISE));
		addPort(new CalendarPort(ATRONOMICAL_SUNSET));
		addPort(new BooleanPort(ATRONOMICAL_DAYTIME));
		
		addPort(new CalendarPort(NAUTICAL_SUNRISE));
		addPort(new CalendarPort(NAUTICAL_SUNSET));
		addPort(new BooleanPort(NAUTICAL_DAYTIME));

		addPort(new CalendarPort(CIVIL_SUNRISE));
		addPort(new CalendarPort(CIVIL_SUNSET));
		addPort(new BooleanPort(CIVIL_DAYTIME));
		
		addPort(new CalendarPort(OFFICIAL_SUNRISE));
		addPort(new CalendarPort(OFFICIAL_SUNSET));
		addPort(new BooleanPort(OFFICIAL_DAYTIME));

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
			sun = new SunriseSunsetCalculator(new Location(getLatitude(),getLongitude()), getTimeZoneIdentifier());
		}
		if (portId.equals(ATRONOMICAL_SUNRISE)) {
			setPortValue(portId, getAstronomicalSunrise()); 					
			return true;
		} else if (portId.equals(ATRONOMICAL_SUNSET)){
			setPortValue(portId, getAstronomicalSunset());
			return true;
		} else if (portId.equals(ATRONOMICAL_DAYTIME)){
			setPortValue(portId, isAstronomicalDaytime());
			return true;
		} else if (portId.equals(NAUTICAL_SUNRISE)) {
			setPortValue(portId, getNauticalSunrise()); 					
			return true;
		} else if (portId.equals(NAUTICAL_SUNSET)){
			setPortValue(portId, getNauticalSunset());
			return true;
		} else if (portId.equals(NAUTICAL_DAYTIME)){
			setPortValue(portId, isNauticalDaytime());
			return true;
		} else if (portId.equals(CIVIL_SUNRISE)) {
			setPortValue(portId, getCivilSunrise()); 					
			return true;
		} else if (portId.equals(CIVIL_SUNSET)){
			setPortValue(portId, getCivilSunset());
			return true;
		} else if (portId.equals(CIVIL_DAYTIME)){
			setPortValue(portId, isCivilDaytime());
			return true;
		} else if (portId.equals(OFFICIAL_SUNRISE)) {
			setPortValue(portId, getOfficialSunrise()); 					
			return true;
		} else if (portId.equals(OFFICIAL_SUNSET)){
			setPortValue(portId, getOfficialSunset());
			return true;
		} else if (portId.equals(OFFICIAL_DAYTIME)){
			setPortValue(portId, isOfficialDaytime());
			return true;
		}
		DevicePort p = getPort(portId);
		logger.trace("Don't need to update port "+p.getAddress());
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

	private boolean isDayTime(Calendar sunRise, Calendar sunSet) {
		Date date = getDate(); 
	    if (date != null && sunRise != null && sunSet != null) {
	        if (date.after(sunRise.getTime()) && date.before(sunSet.getTime())) {
	            return true;
	        } else {
	            return false;
	        }
	    }
	    return false;
	}

	private Calendar getAstronomicalSunset() {
		return sun.getAstronomicalSunsetCalendarForDate(getCalendar());
	}

	private Calendar getAstronomicalSunrise() {
		return sun.getAstronomicalSunriseCalendarForDate(getCalendar());
	}

	private boolean isAstronomicalDaytime() {
		return isDayTime(getAstronomicalSunrise(),getAstronomicalSunset());
	}

	private Calendar getNauticalSunset() {
		return sun.getNauticalSunsetCalendarForDate(getCalendar());
	}

	private Calendar getNauticalSunrise() {
		return sun.getNauticalSunriseCalendarForDate(getCalendar());
	}

	private boolean isNauticalDaytime() {
		return isDayTime(getNauticalSunrise(),getNauticalSunset());
	}

	private Calendar getCivilSunset() {
		return sun.getCivilSunsetCalendarForDate(getCalendar());
	}

	private Calendar getCivilSunrise() {
		return sun.getCivilSunriseCalendarForDate(getCalendar());
	}

	private boolean isCivilDaytime() {
		return isDayTime(getCivilSunrise(),getCivilSunset());
	}
	
	private Calendar getOfficialSunset() {
		return sun.getOfficialSunsetCalendarForDate(getCalendar());
	}

	private Calendar getOfficialSunrise() {
		return sun.getOfficialSunriseCalendarForDate(getCalendar());
	}

	private boolean isOfficialDaytime() {
		return isDayTime(getOfficialSunrise(),getOfficialSunset());
	}

	public void setLocale(String localeIdentifier) {
		setPortValue(LOCALE_IDENTIFIER_PORT, localeIdentifier);		
	}



}
