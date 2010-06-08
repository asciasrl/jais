package it.ascia.ais;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.sqlite.SQLiteJDBCLoader;

public class SQLiteControllerModule extends ControllerModule implements NewDevicePortListener, PropertyChangeListener {

	private Connection conn = null;
	
	public SQLiteControllerModule() {
		Controller.getController().addNewDevicePortListener(this);
	}
	
	public void start() {
		HierarchicalConfiguration config = getConfiguration();
		String dbPath = config.getString("db","jais.db");
		System.setProperty("sqlite.purejava", config.getString("purejava","false"));
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			throw(new AISException("While opening org.sqlite.JDBC:",e));
		}
		try {
			conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
			logger.debug(String.format("running in %s mode", SQLiteJDBCLoader.isNativeMode() ? "native" : "pure-java"));
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
			//stat.executeUpdate("PRAGMA synchronous =  NORMAL;");
			ResultSet rs = stat.executeQuery("select count(*) from portChange");
			logger.info("Initialized database, "+rs.getLong(1)+" portChange records.");	
			conn.setAutoCommit(false);
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
		recordPortChange(evt.getFullAddress(), evt.getTimeStamp(), evt.getOldValue(), evt.getNewValue());
	}
	
	public void recordPortChange(String addess,long ts, Object oldValue, Object newValue) {
		if (conn == null) {
			logger.error("SQL connection not open!");
			return;
		}
		try {
			PreparedStatement ps = conn.prepareStatement("insert into portChange (address,ts,oldValue,newValue) values (?,?,?,?);");
			ps.setString(1, addess);
			ps.setLong(2, ts);
			if (oldValue == null) {
				ps.setNull(3, java.sql.Types.JAVA_OBJECT);
			} else {
				ps.setObject(3, oldValue);
			}
			ps.setObject(4, newValue);
			int n = ps.executeUpdate();
			conn.commit();

			Statement stat = conn.createStatement();
			logger.trace("Rows="+n);
			ResultSet rs = stat.executeQuery("select count(*) from portChange;");
			logger.debug("portChange records: "+rs.getLong(1));
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
