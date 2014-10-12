package it.ascia.calendar;

import it.ascia.ais.ControllerModule;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.configuration.HierarchicalConfiguration;

public class CalendarControllerModule extends ControllerModule {
	
	public void start() {
		List<?> connectors = getConfiguration().configurationsAt("Connector");
		for (Iterator<?> configIterator = connectors.iterator(); configIterator.hasNext();)
		{
		    HierarchicalConfiguration config = (HierarchicalConfiguration) configIterator.next();
		    if (config.getBoolean("[@disabled]",false)) {
		    	logger.debug("Connector disabled: "+config.getString("[@name]"));
		    	continue;
		    }
		    CalendarConnector calendarConnector = new CalendarConnector(config.getString("[@name]"));
		    calendarConnector.setModule(this);

		    CalendarDevice calendar = new CalendarDevice(CalendarConnector.CALENDAR_DEVICE_NAME);
		    calendar.setTimeZoneIdentifier(config.getString("TimeZone"));
		    calendar.setLocale(config.getString("Locale"));
		    calendar.setLatitude(config.getString("Location[@latitude]"));
		    calendar.setLongitude(config.getString("Location[@longitude]"));
		    calendarConnector.addDevice(calendar);

		    controller.addConnector(calendarConnector);
		    calendarConnector.start();
		}
 		super.start();
	}	
	
}
