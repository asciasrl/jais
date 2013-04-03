package it.ascia.ais;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import org.apache.commons.configuration.HierarchicalConfiguration;

public class MySQLControllerModule extends ControllerModule implements NewDevicePortListener, PropertyChangeListener {

	private Connection conn = null;
	
	protected BlockingQueue<DevicePortChangeEvent> eventsQueue;

	private String url;

	private String username;

	private String password;

	private RecordPortChangeThread recordPortChangeThread;
	
	public MySQLControllerModule() {
		Controller.getController().addNewDevicePortListener(this);
	}
	
	public void start() {
		HierarchicalConfiguration config = getConfiguration();
		url = config.getString("url","jdbc:mysql://localhost:3306/mysql");
		username = config.getString("username","root");
		password = config.getString("password","");
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			throw(new AISException("While opening mysql driver:",e));
		}
		connect();
		initDb();
		eventsQueue = new LinkedBlockingDeque<DevicePortChangeEvent>();
		recordPortChangeThread = new RecordPortChangeThread();
		recordPortChangeThread.setName("Record-"+getClass().getSimpleName()+"-"+getName());
		//recordPortChangeThread.setDaemon(true);
		recordPortChangeThread.start();
		super.start();
	}
	
	private void connect() {
		try {
			conn = DriverManager.getConnection(url,username, password);
			conn.setAutoCommit(false);
			logger.info("Connected to: "+url);
		} catch (SQLException e) {
			throw(new AISException("While connecting to "+url+" :",e));
		}
	}

	private void initDb() {
	    try {
			Statement stat = conn.createStatement();
			stat.executeUpdate("CREATE TABLE IF NOT EXISTS `portChange` (" +
					  "`address` varchar(60) NOT NULL,"+
					  "`oldTs` timestamp NULL DEFAULT NULL,"+
					  "`newTs` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,"+
					  "`oldValue` varchar(60),"+
					  "`newValue` varchar(60) NOT NULL,"+
					  "KEY `address` (`address`)"+
					") ENGINE=MyISAM DEFAULT CHARSET=utf8;");
			ResultSet rs = stat.executeQuery("select count(*) from portChange;");
			rs.first();
			logger.info("Initialized database, "+rs.getLong(1)+" events records.");	
		} catch (SQLException e) {
			throw(new AISException("While initDb :",e));
		}
	}

	public void stop() {
		super.stop();
		try {
			conn.commit();
			conn.close();
		} catch (SQLException e) {
			logger.fatal(e);
		}
	}

	public void newDevicePort(NewDevicePortEvent evt) {
		DevicePort p = evt.getDevicePort();
		logger.debug("New device port "+p);
		p.addPropertyChangeListener(this);		
	}

	public void devicePortChange(DevicePortChangeEvent evt) {
		try {
			eventsQueue.add(evt);
		} catch (IllegalStateException e) {
			logger.error("Unable to queue event:" + evt.toString());
		}
		logger.debug("Events to record: "+eventsQueue.size());
	}
	
	private void recordPortChange(String address,long oldTs, long newTs, Object oldValue, Object newValue) throws SQLException {
		PreparedStatement ps = conn.prepareStatement("insert into portChange (address,oldTs,newTs,oldValue,newValue) values (?,?,?,?,?);");
		ps.setString(1, address);
		if (oldTs == 0) {
			ps.setNull(2, java.sql.Types.TIMESTAMP);			
		} else {
			ps.setTimestamp(2, new Timestamp(oldTs));
		}
		if (newTs == 0) {
			ps.setNull(3, java.sql.Types.TIMESTAMP);			
		} else {
			ps.setTimestamp(3, new Timestamp(newTs));
		}
		if (oldValue == null) {
			ps.setNull(4, java.sql.Types.VARCHAR);
		} else {
			ps.setString(4, oldValue.toString());
		}
		if (newValue == null) {
			ps.setNull(5, java.sql.Types.VARCHAR);
		} else {
			ps.setString(5, newValue.toString());
		}			
		int n = ps.executeUpdate();
		ps.close();
		conn.commit();
		logger.trace("Inserted rows="+n);
	}

	public void propertyChange(PropertyChangeEvent evt) {
		if (DevicePortChangeEvent.class.isInstance(evt)) {
			devicePortChange((DevicePortChangeEvent) evt);
		}		
	}
	
	/**
	 * Questo thread esegue il dispacciamento dei messaggi ricevuti che sono stati messi nella apposita coda
	 * Esegue il metodo Connector.dispatchMessage()
	 * @author Sergio
	 * @since 20100513
	 */
    private class RecordPortChangeThread extends Thread {
        
		public void run() {
			logger.debug("Start.");
    		while (isRunning()) {
				try {
					DevicePortChangeEvent e = eventsQueue.take();
					// lo rimette in coda
					eventsQueue.offer(e);
					try {
						DevicePort p = (DevicePort) e.getSource();
						recordPortChange(e.getFullAddress(), p.getPreviuosTimeStamp(), e.getTimeStamp(), e.getOldValue(), e.getNewValue());						
						// lo elimina solo se la registrazione non ha dato errori
						eventsQueue.remove(e);
					} catch (SQLException e1) {
						logger.error("Error inserting:",e1);
						try {
							conn.close();
						} catch (SQLException e2) {
						}
						synchronized (this) {
							wait(1000);
						}
						connect();
					}
					logger.debug("Events to record: "+eventsQueue.size());
				} catch (InterruptedException e) {
					logger.debug("Interrupted.");
				} catch (Exception e) {
					logger.fatal("Error:",e);
				}
    		}
			logger.debug("Stop.");
    	}
    }

	
}
