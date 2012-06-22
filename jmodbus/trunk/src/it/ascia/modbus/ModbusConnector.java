package it.ascia.modbus;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.configuration.HierarchicalConfiguration;

import net.wimpi.modbus.ModbusCoupler;
import net.wimpi.modbus.io.ModbusSerialTransaction;
import net.wimpi.modbus.msg.ModbusRequest;
import net.wimpi.modbus.net.SerialConnection;
import net.wimpi.modbus.util.SerialParameters;
import it.ascia.ais.AISException;
import it.ascia.ais.Connector;
import it.ascia.ais.ConnectorInterface;
import it.ascia.ais.Message;
import it.ascia.ais.SerialTransport;

public class ModbusConnector extends Connector implements
		ConnectorInterface {

	private long lastWrite = 0;
	
	/* The important instances of the classes mentioned before */
	private SerialConnection con = null; // the connection

	public ModbusConnector(int unitID, String name, String portname, String encoding) {
		super(1000,name);


		// 2. Set master identifier
		ModbusCoupler.getReference().setUnitID(unitID);
		
		// 3. Setup serial parameters
		SerialParameters params = new SerialParameters();
		params.setPortName(portname);
		params.setBaudRate(9600);
		params.setDatabits(8);
		params.setParity("None");
		params.setStopbits(1);
		params.setEncoding(encoding);
		params.setEcho(false);
		// params.setReceiveTimeout(200);

		// apre e chiude, per caricare le librerie rxtx
		(new SerialTransport(portname)).close();

		// 4. Open the connection
		con = new SerialConnection(params);
		try {
			con.open();
		} catch (Exception e1) {
			throw (new AISException("modbus connection error", e1));
		}

	}
	
	@Override
	public boolean sendMessage(Message m) {
		if (ModbusRequestMessage.class.isInstance(m)) {
			return sendMessage((ModbusRequestMessage)m); 
		} else {
			throw (new AISException("Can send only request, not "
					+ m.getClass()));
		}
	}
	
	private boolean sendMessage(ModbusRequestMessage m) {
		// 6. Prepare a transaction
		ModbusSerialTransaction trans = new ModbusSerialTransaction(con);
		trans.setRetries(3);
		ModbusRequest req = m.getRequest();
		trans.setRequest(req);

		long delay = lastWrite + 200 - System.currentTimeMillis();
		if (delay > 0) {
			synchronized (this) {
				try {
					logger.trace("Wait("+delay+")");
					wait(delay);
				} catch (InterruptedException e) {
				}
			}
		}		
		try {
			trans.execute();
		} catch (Exception e) {
			throw (new AISException("modbus execute error", e));
		}
		
		ModbusResponseMessage res = new ModbusResponseMessage(trans);
		m.setResponse(res);
		
    	if (dispatchQueue.remainingCapacity() > 0) {
	    	logger.debug("Received: " + res);
    	} else {
    		logger.error("Queue full for messagge: " + res);
    	}
		try {
			dispatchQueue.put(res);
		} catch (InterruptedException e) {
			logger.error("Interrupted while putting message in dispatch queue");
		}

		lastWrite = System.currentTimeMillis();
		
		return true;
	}

	@Override
	protected void dispatchMessage(Message m) throws AISException {
		if (ModbusResponseMessage.class.isInstance(m)) {
			dispatchMessage((ModbusResponseMessage) m);
		} else {
			logger.error("Cannot dispatch message: "+m);
		}
	}
	
    protected void dispatchMessage(ModbusResponseMessage m) throws AISException {
		// TODO Auto-generated method stub	    	
    }

	
	/**
	 * Aggiunge un dispositivo modbus slave al connettore
	 * @param sub configurazione del device
	 */
    public void addSlave(HierarchicalConfiguration sub) {
    	int unitID = sub.getInt("unitid");
		ModbusDevice device = new ModbusDevice(unitID);
		device.setDescription(sub.getString("description"));
		//logger.info("Adding modbus slave device, ID = " + unitID);
		addDevice(device);
		logger.info("Added " + device);
		List<HierarchicalConfiguration> registers = sub.configurationsAt("register");
		for (Iterator<HierarchicalConfiguration> i = registers.iterator(); i.hasNext();) {
			HierarchicalConfiguration registerConfig = i.next();
			String type = registerConfig.getString("type");			
			double factor = registerConfig.getDouble("factor",1);
			String decimalformat = registerConfig.getString("decimalformat","");
			int address;
			String hexaddress = registerConfig.getString("address","");
			try {
				if (hexaddress.startsWith("0x")) {
					address = Integer.parseInt(hexaddress.substring(2),16);
				} else {
					address = Integer.parseInt(hexaddress);
				}
			} catch (NumberFormatException e) {
				logger.error("Invalid format of port address: '" + hexaddress + "'");
				continue;
			}
			ModbusPort port = null;
			if (type.equals("byte")) {
				port = new ModbusBytePort(address, factor, decimalformat);
			} else if (type.equals("word") || type.equals("int16")) {
				port = new ModbusWordPort(address, factor, decimalformat);
			} else if (type.equals("integer") || type.equals("int32")) {
				port = new ModbusIntegerPort(address, factor, decimalformat);
			} else if (type.equals("long") || type.equals("int64")) {
				port = new ModbusLongPort(address, factor, decimalformat);
			} else {
				logger.error("Unsupported port type: '" + type + "'");
				continue;
			}
			int retention = registerConfig.getInt("retention",-1);
			if (retention > 0) {
				port.setCacheRetention(retention);
			}
			port.setDescription(registerConfig.getString("description",null));
			device.addPort(port);
			logger.info("Added " + port);			
		}
	}	

}
