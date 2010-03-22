
package it.ascia.alarm;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.jabsorb.JSONRPCBridge;

import it.ascia.ais.Address;
import it.ascia.ais.ControllerModule;
import it.ascia.ais.DevicePort;
import it.ascia.ais.DevicePortChangeEvent;

/**
 * Allarme antintrusione software
 * @author Sergio
 * (C) 2009-2010 Ascia S.r.l.
 */

public class AlarmControllerModule extends ControllerModule implements PropertyChangeListener {

	private String alarmArmedPortId;
	private DevicePort alarmArmedPort;

	private String preAlarmPortId;
	private DevicePort preAlarmPort;

	private String alarmPortId;
	private DevicePort alarmPort;

	// tempo di uscita (mS)
	private long exitDelay;
	private long exittime = 0;
	
	// tempo di entrata (mS)
	private long enterDelay;

	// tempo di allarme (mS)
	private long alarmDuration;

	private Thread alarmThread;
	
	/**
	 * Read configuration parameters and register alarm RPC server
	 */
	public void start() {		
		alarmArmedPortId = getConfiguration().getString("alarmArmedPortId",null);
		alarmArmedPort = controller.getDevicePort(new Address(alarmArmedPortId));
		alarmArmedPort.addPropertyChangeListener(this);
		logger.info("Alarm armed port=" + alarmArmedPortId);

		preAlarmPortId = getConfiguration().getString("preAlarmPortId",null);;
		preAlarmPort = controller.getDevicePort(new Address(preAlarmPortId));
		preAlarmPort.addPropertyChangeListener(this);
		exitDelay = getConfiguration().getLong("exitDelay",30);
		enterDelay = getConfiguration().getLong("enterDelay",30);
		logger.info("Prealarm port=" + preAlarmPortId + " exit=" + exitDelay + "s enter=" + enterDelay + "s");
		exitDelay *= 1000;
		enterDelay *= 1000;
		
		alarmPortId = getConfiguration().getString("alarmPortId",null);;
		alarmPort = controller.getDevicePort(new Address(alarmPortId));
		alarmDuration = getConfiguration().getLong("alarmDuration",120);		
		logger.info("Alarm port=" + alarmPortId + " duration=" + alarmDuration + "s");
		alarmDuration *= 1000;

		JSONRPCBridge.getGlobalBridge().registerObject("Alarm", new AlarmRPCServer(this));
		super.start();
	}
	
	/**
	 * Check if alarm is armed or not (disarmed)
	 * @return value of alarmArmedPort
	 */
	public boolean isArmed() {
		return ((Boolean) (alarmArmedPort.getValue())).booleanValue();
	}
	
	/**
	 * Handle sensor events and alarm cancellation
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		if (DevicePortChangeEvent.class.isInstance(evt)) {
			String address = ((DevicePortChangeEvent)evt).getFullAddress();
			Object value = ((DevicePortChangeEvent)evt).getNewValue();
			if (value == null) {
				logger.warn("Port "+address+", value null");
				return;
			}
			if (!Boolean.class.isInstance(value)) {
				logger.error("Port "+address+" must be Boolean, not "+value.getClass().getSimpleName());
				return;
			}
			boolean bValue = ((Boolean)value).booleanValue(); 
			logger.debug("Ricevuto: "+address+"="+value);
			if (address.equals(preAlarmPortId) && bValue) {
				if (!isArmed()) {
					logger.trace("Alarm disabled, ignore prealarms.");
				} else if (exittime > 0 && 
						System.currentTimeMillis() < (exittime + exitDelay)) {
					logger.debug("In exit time, ignore prealarms.");
				} else if (alarmThread != null && alarmThread.isAlive()) {
					logger.debug("Already in alarm");
				} else {
					alarmThread = new AlarmThread();
					alarmThread.setName("Alarm "+alarmPortId);
					alarmThread.start();
				}					 
			} else if (address.equals(alarmArmedPortId)) {
				if (bValue) {
					logger.info("Alarm armed");
					exittime = System.currentTimeMillis();
				} else {
					logger.info("Alarm disarmed");
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

	/**
	 * Perform an alarm cycle
	 */
	private class AlarmThread extends Thread {
		public void run() {
			try {
				logger.info("Start entering delay "+enterDelay/1000+"sec");
				Thread.sleep(enterDelay);
				try {
					alarmPort.writeValue(new Boolean(true));
					logger.info("Start alarm for "+alarmDuration/1000+"sec");
					Thread.sleep(alarmDuration);
				} catch (InterruptedException e) {
					logger.info("Alarm interrupted during alarm");
				}
				alarmPort.writeValue(new Boolean(false));
			} catch (InterruptedException e) {
				logger.info("Alarm interrupted during enter delay");
			}
			logger.info("End alarm cycle");
		}
	}

	/**
	 * Check user code
	 * @param pin
	 * @return true if pin is valid
	 */
	public boolean checkPin(String pin) {
		String pin1 = getConfiguration().getString("PIN",null);
		return pin.equals(pin1);
	}
	
	/**
	 * arm (enable) alarm
	 * @param pin
	 * @return true if pin is valid and successfully armed
	 */
	public boolean arm(String pin) {
		if (checkPin(pin)) {
			if (alarmArmedPort.writeValue(new Boolean(true))) {
				logger.info("Alarm armed with PIN");
				return true;
			}
		}
		return false;
	}
	
	/**
	 * disarm (disable) alarm
	 * @param pin
	 * @return true if pin is valid and successfully disarmed
	 */
	public boolean disarm(String pin) {
		if (checkPin(pin)) {
			if (alarmArmedPort.writeValue(new Boolean(false))) {
				logger.info("Alarm disarmed with PIN");
				return true;
			}
		}		
		return false;
	}

	/**
	 * Toggle (arm/disarm) status
	 * @param pin
	 * @return true if pin is valid and successfully armed or disarmed
	 */
	public boolean toggle(String pin) {
		if (isArmed()) {
			return disarm(pin);
		} else {
			return arm(pin);
		}
	}
	
}