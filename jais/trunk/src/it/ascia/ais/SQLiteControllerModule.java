package it.ascia.ais;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.configuration.HierarchicalConfiguration;

public class SQLiteControllerModule extends ControllerModule implements NewDevicePortListener, PropertyChangeListener {

	private Connection conn = null;
	
	public SQLiteControllerModule() {
		Controller.getController().addNewDevicePortListener(this);
	}
	
	public void start() {
		HierarchicalConfiguration config = getConfiguration();
		String dbPath = config.getString("db","jais.db");
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			throw(new AISException("While opening org.sqlite.JDBC:",e));
		}
		try {
			conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
		} catch (SQLException e) {
			throw(new AISException("While connecting to dbPath="+dbPath+" :",e));
		}
		initDb();
		super.start();
	}

	private void initDb() {
	    try {
			Statement stat = conn.createStatement();
			stat.executeUpdate("create table if not exists portChange (address,ts,oldValue,newValue)");
			ResultSet rs = stat.executeQuery("select count(*) from portChange");
			logger.info("Initialized database, "+rs.getLong(1)+" portChange records.");			
		} catch (SQLException e) {
			throw(new AISException("While initDb :",e));
		}
	}

	public void stop() {
		super.stop();
		try {
			conn.rollback();
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
		recordPortChange(evt.getFullAddress(), evt.getTimeStamp(), evt.getOldValue().toString(), evt.getNewValue().toString());
	}
	
	public void recordPortChange(String addess,long ts, String oldValue, String newValue) {
		if (conn == null) {
			logger.error("SQL connection not open!");
			return;
		}
		String sql = "insert into portChange (address,ts,oldValue,newValue) values ('"+addess+"',"+ts+",'"+oldValue+"','"+newValue+"')";
		try {
			logger.trace(sql);
			Statement stat = conn.createStatement();
			stat.executeUpdate(sql);
		} catch (SQLException e) {
			logger.error(sql,e);
		}	    				
	}

	public void propertyChange(PropertyChangeEvent evt) {
		if (DevicePortChangeEvent.class.isInstance(evt)) {
			devicePortChange((DevicePortChangeEvent) evt);
		}		
	}
	
}
