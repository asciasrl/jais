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

import org.apache.commons.configuration.HierarchicalConfiguration;

public class MySQLControllerModule extends ControllerModule implements NewDevicePortListener, PropertyChangeListener {

	private Connection conn = null;
	
	public MySQLControllerModule() {
		Controller.getController().addNewDevicePortListener(this);
	}
	
	public void start() {
		HierarchicalConfiguration config = getConfiguration();
		String url = config.getString("url","jdbc:mysql://localhost:3306/mysql");
		String username = config.getString("username","root");
		String password = config.getString("password","");
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			throw(new AISException("While opening mysql driver:",e));
		}
		try {
			conn = DriverManager.getConnection(url,username, password);
		} catch (SQLException e) {
			throw(new AISException("While connecting to "+url+" :",e));
		}
		initDb();
		super.start();
	}

	private void initDb() {
	    try {
			Statement stat = conn.createStatement();
			stat.executeUpdate("CREATE TABLE IF NOT EXISTS `portChange` (" +
					  "`address` varchar(60) NOT NULL,"+
					  "`ts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,"+
					  "`oldValue` varchar(60),"+
					  "`newValue` varchar(60) NOT NULL,"+
					  "KEY `address` (`address`)"+
					") ENGINE=MyISAM DEFAULT CHARSET=utf8;");
			ResultSet rs = stat.executeQuery("select count(*) from portChange;");
			rs.first();
			logger.info("Initialized database, "+rs.getLong(1)+" events records.");	
			conn.setAutoCommit(false);
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
		recordPortChange(evt.getFullAddress(), evt.getTimeStamp(), evt.getOldValue(), evt.getNewValue());
	}
	
	public void recordPortChange(String address,long ts, Object oldValue, Object newValue) {
		if (conn == null) {
			logger.error("SQL connection not open!");
			return;
		}
		try {
			PreparedStatement ps = conn.prepareStatement("insert into portChange (address,ts,oldValue,newValue) values (?,?,?,?);");
			ps.setString(1, address);
			ps.setTimestamp(2, new Timestamp(ts));
			if (oldValue == null) {
				ps.setNull(3, java.sql.Types.VARCHAR);
			} else {
				ps.setString(3, oldValue.toString());
			}
			ps.setString(4, newValue.toString());
			int n = ps.executeUpdate();
			conn.commit();
			logger.trace("Inserted rows="+n);
			/*
			Statement stat = conn.createStatement();
			logger.trace("Rows="+n);
			ResultSet rs = stat.executeQuery("select count(*) from portChange;");
			rs.first();
			logger.debug("portChange records: "+rs.getLong(1));
			*/
		} catch (SQLException e) {
			logger.error("Error inserting:",e);
		}	    				
	}

	public void propertyChange(PropertyChangeEvent evt) {
		if (DevicePortChangeEvent.class.isInstance(evt)) {
			devicePortChange((DevicePortChangeEvent) evt);
		}		
	}
	
}
