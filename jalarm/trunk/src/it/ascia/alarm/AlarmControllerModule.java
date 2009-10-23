package it.ascia.alarm;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import it.ascia.ais.ControllerModule;
import it.ascia.ais.DevicePort;
import it.ascia.ais.DevicePortChangeEvent;

public class AlarmControllerModule extends ControllerModule implements PropertyChangeListener {

	private String alarmArmedPortId;
	private DevicePort alarmArmedPort;

	private String preAlarmPortId;
	private DevicePort preAlarmPort;

	private String alarmPortId;
	private DevicePort alarmPort;

	// tempo di uscita
	private long exitDelay = 30000;
	private long exittime = 0;
	
	// tempo di entrata 	
	private long enterDelay = 30000;

	// tempo di allarme
	private long alarmDuration = 120000;

	private Thread alarmThread;
	
	public void start() {
		// FIXME leggere porte dal file di configurazione 
		preAlarmPort = controller.getDevicePort(alarmArmedPortId);
		preAlarmPort.addPropertyChangeListener(this);
		alarmArmedPort = controller.getDevicePort(alarmArmedPortId);
		alarmArmedPort.addPropertyChangeListener(this);
		alarmPort = controller.getDevicePort(alarmPortId);
		super.start();
	}
	
	private boolean isAlarmActive() {
		return ((Boolean) (alarmArmedPort.getValue())).booleanValue();
	}
	
	public void propertyChange(PropertyChangeEvent evt) {
		if (DevicePortChangeEvent.class.isInstance(evt)) {
			String address = ((DevicePortChangeEvent)evt).getFullAddress();
			Object value = ((DevicePortChangeEvent)evt).getNewValue();
			if (address.equals(preAlarmPortId) && 
					((Boolean)value).booleanValue() && 
					isAlarmActive()) {
				if (exittime > 0 && 
						System.currentTimeMillis() < (exittime + exitDelay)) {
					logger.debug("In exit time, ignore prealarms.");
				} else if (alarmThread != null && alarmThread.isAlive()) {
					logger.debug("Already in alarm");
				} else {
					alarmThread = new AlarmThread();
					alarmThread.start();
				}					 
			} else if (address.equals(alarmPortId)) {
				if (((Boolean)value).booleanValue()) {
					exittime = System.currentTimeMillis();
				} else {
					if (alarmThread != null && alarmThread.isAlive()) {
						alarmThread.interrupt();
						try {
							alarmThread.join();
						} catch (InterruptedException e) {
							logger.debug("Interrotto:",e);
						}
						alarmThread = null;						
					}
				}
			} 			
		}
	}

	class AlarmThread extends Thread {
		public void run() {
			try {
				logger.info("Start entering delay "+enterDelay/1000+"sec");
				Thread.sleep(enterDelay);
				alarmPort.writeValue(new Boolean(true));
				logger.info("Start alarm for "+alarmDuration/1000+"sec");
				Thread.sleep(alarmDuration);
			} catch (InterruptedException e) {
				logger.info("Interrupted alarm");
			}
			alarmPort.writeValue(new Boolean(false));
			logger.info("End alarme");
		}
	}
	
}