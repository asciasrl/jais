/**
 * 
 */
package it.ascia.ais;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DateFormat;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
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
 * @TODO Condizione sul valore 
 */
public class EmailNotifierControllerModule extends ControllerModule implements NewDevicePortListener, PropertyChangeListener  {
	
	private String hostname;
	private String from;
	private String to;
	private String subject;
	
	protected LinkedBlockingQueue<DevicePortChangeEvent> notifierQueue;
	private NotifierThread notifierThread;

	@Override
	public void start() {		
		HierarchicalConfiguration config = getConfiguration();
		hostname = config.getString("Hostname","smtp.ascia.net");
		from = config.getString("From","JAIS <sergio@ascia.net>");
		to = config.getString("To","Me <sergio@ascia.net>");
		subject = config.getString("Subject","JAIS Email Notifier");
		logger.info("Hostname: "+hostname + " From:" + from + " To:" + to + " Subject:"+subject);
				
		Controller.getController().addNewDevicePortListener(this);
		notifierQueue = new LinkedBlockingQueue<DevicePortChangeEvent>();
		notifierThread = new NotifierThread();
		notifierThread.setName("Dispatching-"+getClass().getSimpleName()+"-"+getName());
		notifierThread.setDaemon(true);
		notifierThread.start();
		
		super.start();
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
		notifyPortChange((DevicePort) evt.getSource(), evt.getTimeStamp(), evt.getOldValue(), evt.getNewValue());
	}

	public void notifyPortChange(DevicePort p,long ts, Object oldValue, Object newValue) {
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTimeInMillis(ts);
		DateFormat df = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL);
		sendEmail("Port "+p+" changed from '"+oldValue+"' to '"+newValue+"' at "+df.format(cal.getTime()));
	}
	
	private void sendEmail(String msg) {
		SimpleEmail email = new SimpleEmail();
		try {
			email.setHostName(hostname);
			email.setFrom(from);
			email.addTo(to);
			email.setSubject(subject);
			email.setMsg(msg);
			logger.info("Sending email to "+to+" : "+msg);
			String res = email.send();
			logger.debug("Sent email ("+res+"):" + msg);
		} catch (EmailException e) {
			logger.fatal(e);
		}
	}

	@Override
	public void newDevicePort(NewDevicePortEvent evt) {
		DevicePort p = evt.getDevicePort();
		
		HierarchicalConfiguration config = getConfiguration();
		List notifyList = config.configurationsAt("notify");
		for (Iterator notify = notifyList.iterator(); notify.hasNext();)
		{
		    HierarchicalConfiguration notifyConfig = (HierarchicalConfiguration) notify.next();
		    String address = (String) notifyConfig.getString("address");
		    String description = (String) notifyConfig.getString("description");
			if (p.getAddress().matches(address)) {
				if (p.getDescription().equals(p.getAddress().toString()) && description != null) {
					logger.debug("Descrizione porta "+p+" : "+description);
					p.setDescription(description);
				}
				logger.debug("Attivate notifiche per "+p);
				p.addPropertyChangeListener(this);
			}
		}
		
	}

}
