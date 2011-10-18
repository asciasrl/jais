package it.ascia.modbus;

import net.wimpi.modbus.Modbus;
import net.wimpi.modbus.ModbusCoupler;
import net.wimpi.modbus.io.ModbusSerialTransaction;
import net.wimpi.modbus.msg.ModbusRequest;
import net.wimpi.modbus.msg.ModbusResponse;
import net.wimpi.modbus.msg.ReadInputRegistersRequest;
import net.wimpi.modbus.msg.ReadInputRegistersResponse;
import net.wimpi.modbus.net.SerialConnection;
import net.wimpi.modbus.util.SerialParameters;
import it.ascia.ais.AISException;
import it.ascia.ais.Address;
import it.ascia.ais.Connector;
import it.ascia.ais.ConnectorInterface;
import it.ascia.ais.DevicePort;
import it.ascia.ais.Message;
import it.ascia.ais.SerialTransport;

public class ModbusConnector extends Connector implements
		ConnectorInterface {

	private long lastWrite = 0;
	
	/* The important instances of the classes mentioned before */
	private SerialConnection con = null; // the connection

	public ModbusConnector(String name, String portname) {
		super(1000,name);

		// TODO Gestire valori da file di configurazione

		// 2. Set master identifier
		ModbusCoupler.getReference().setUnitID(1);

		// 3. Setup serial parameters
		SerialParameters params = new SerialParameters();
		params.setPortName(portname);
		params.setBaudRate(9600);
		params.setDatabits(8);
		params.setParity("None");
		params.setStopbits(1);
		params.setEncoding(Modbus.SERIAL_ENCODING_RTU);
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
		if (!ModbusRequestMessage.class.isInstance(m)) {
			throw (new AISException("Can send only request, not "
					+ m.getClass()));
		}
		// 6. Prepare a transaction
		ModbusSerialTransaction trans = new ModbusSerialTransaction(con);
		trans.setRetries(3);
		trans.setRequest(((ModbusRequestMessage) m).getRequest());

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
    	ModbusResponse res = m.getResponse();
    	int unitid = m.getResponse().getUnitID();
    	Address a = new Address(this, null, null);
    	a.setDeviceAddress((new Integer(unitid)).toString());
    	ModbusDevice d = (ModbusDevice) getDevice(a);
    	ModbusRequest req = m.getRequest();
    	String portId = null;
    	Object newValue = null;
    	if (ReadInputRegistersRequest.class.isInstance(req)) {
    		int ref = ((ReadInputRegistersRequest)req).getReference();
    		portId = new Integer(ref).toString();
    		newValue = ((ReadInputRegistersResponse)res).getRegisterValue(0);
    	}
    	if (portId != null) {
    		DevicePort p = d.getPort(portId);
    		if (p != null) {
	    		if (newValue != null) {
		    		p.setValue(newValue);
	    		}
	    	}
	    }
    	
    }

	
	

}
