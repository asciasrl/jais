package it.ascia.calendar;

import it.ascia.ais.ConnectorImpl;

public class CalendarConnector extends ConnectorImpl {

	static final String CALENDAR_DEVICE_NAME = "Calendar";
	static final String LOCATION_DEVICE_NAME = "Location";
	static final String SUNRISESUNSET_DEVICE_NAME = "SunriseSunset";
	
	private long autoupdate = 60000;

	private UpdatingThread updatingThread;

	public CalendarConnector(String name) {
		super(name);
	}

	public void start() {
		super.start();
		updatingThread = new UpdatingThread();
		updatingThread.setName("Updating-"+getClass().getSimpleName()+"-"+getConnectorName());
		updatingThread.setDaemon(true);
		updatingThread.start();		
	}

	/**
	 * Questo thread esegue l'aggiornamento delle porte che sono state messe nella apposita coda.
	 * Esegue il metodo DevicePort.update()
	 * @author Sergio
	 */
    private class UpdatingThread extends Thread {
        
    	public void run() {
			logger.debug("Start.");
			CalendarDevice calendar = (CalendarDevice) getDevice(CALENDAR_DEVICE_NAME);
    		while (isRunning()) {
				try {
					calendar.update();
					long t = System.currentTimeMillis();
					long timeout = autoupdate - ( t - autoupdate * (t / autoupdate));
					logger.trace("wait "+timeout+" mS");
					synchronized (this) {
						wait(timeout);
					}
				} catch (InterruptedException e) {
					logger.debug("Interrotto.");
				} catch (Exception e) {
					logger.error("Errore:",e);
				}
    		}
			logger.debug("Stop.");
    	}
    }

}
