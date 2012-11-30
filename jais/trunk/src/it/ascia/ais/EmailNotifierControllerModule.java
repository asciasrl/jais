/**
 * 
 */
package it.ascia.ais;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DateFormat;
import java.util.GregorianCalendar;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import it.ascia.ais.ControllerModule;
import it.ascia.ais.DevicePort;
import it.ascia.ais.DevicePortChangeEvent;
import it.ascia.ais.NewDevicePortEvent;
import it.ascia.ais.NewDevicePortListener;

/**
 * @author Sergio
 * @since 20121102
 */
public class EmailNotifierControllerModule extends ControllerModule implements NewDevicePortListener, PropertyChangeListener  {
	
	private String hostname;
	private String from;
	private String to;

	protected LinkedBlockingQueue<DevicePortChangeEvent> notifierQueue;
	private NotifierThread notifierThread;

	/**
	 * 
	 */
	public EmailNotifierControllerModule() {
	}
	
	@Override
	public void start() {
		super.start();
		
		HierarchicalConfiguration config = getConfiguration();
		hostname = config.getString("Hostname","smtp.ascia.net");
		from = config.getString("From","JAIS <sergio@ascia.net>");
		to = config.getString("To","Me <sergio@ascia.net>");
		logger.info("Hostname: "+hostname + " From:" + from + " To:" + to);
				
		Controller.getController().addNewDevicePortListener(this);
		notifierQueue = new LinkedBlockingQueue<DevicePortChangeEvent>();
		notifierThread = new NotifierThread();
		notifierThread.setName("Dispatching-"+getClass().getSimpleName()+"-"+getName());
		notifierThread.setDaemon(true);
		notifierThread.start();
	}
	
	@Override
	public void stop() {
		super.stop();
		notifierThread.interrupt();
    	try {
    		notifierThread.join();
		} catch (InterruptedException e) {
			logger.error("Interrupted:",e);
		}
	}
	
	

    private class NotifierThread extends Thread {
        
		public void run() {
			logger.debug("Start.");
    		while (isRunning()) {
    			DevicePortChangeEvent evt;
				try {
					evt = notifierQueue.take();
					devicePortChange(evt);
				} catch (InterruptedException e) {
					logger.debug("Interrotto.");
				} catch (Exception e) {
					logger.fatal("Errore:",e);
				}
    		}
			logger.debug("Stop.");
    	}
    }


	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (DevicePortChangeEvent.class.isInstance(evt)) {
			notifierQueue.add((DevicePortChangeEvent) evt);
			logger.trace("Event added to the queue: "+ evt);
		}		
	}

	public void devicePortChange(DevicePortChangeEvent evt) {		
		notifyPortChange(evt.getFullAddress(), evt.getTimeStamp(), evt.getOldValue(), evt.getNewValue());
	}

	public void notifyPortChange(String addess,long ts, Object oldValue, Object newValue) {
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTimeInMillis(ts);
		DateFormat df = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL);
		sendEmail("Port "+addess+" changed from '"+oldValue+"' to '"+newValue+"' at "+df.format(cal.getTime()));
	}
	
	private void sendEmail(String msg) {
		SimpleEmail email = new SimpleEmail();
		email.setHostName(hostname);
		try {
			email.setFrom(from);
			email.addTo(to);
			email.setSubject("JAIS Email Notifier");
			email.setMsg(msg);
			logger.trace("Sending email: "+msg);
			String res = email.send();
			logger.debug("Sent email ("+res+"):" + msg);
		} catch (EmailException e) {
			logger.fatal(e);
		}
	}

	@Override
	public void newDevicePort(NewDevicePortEvent evt) {
		DevicePort p = evt.getDevicePort();
		logger.debug("New device port "+p);
		// TODO aggiungere solo le porte di cui si vogliono notificare le modifiche in base al file di configurazione
		if (p.getAddress().toString().startsWith("*.Group6:")) {
			logger.debug("Attivate notifiche per "+p);
			p.addPropertyChangeListener(this);
		} else {
			logger.trace("Non attivate notifiche per "+p);
		}
	}

}
