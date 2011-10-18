package it.ascia.modbus;

import net.wimpi.modbus.Modbus;
import net.wimpi.modbus.ModbusCoupler;
import net.wimpi.modbus.io.ModbusSerialTransaction;
import net.wimpi.modbus.msg.ReadInputRegistersRequest;
import net.wimpi.modbus.msg.ReadInputRegistersResponse;
import net.wimpi.modbus.net.SerialConnection;
import net.wimpi.modbus.util.SerialParameters;
import it.ascia.ais.AISException;
import it.ascia.ais.ControllerModule;
import it.ascia.ais.SerialTransport;

public class ModbusControllerModule extends ControllerModule {

	public void start() {
		super.start();

		// TODO Gestire valori da file di configurazione
		ModbusConnector conn = new ModbusConnector("sq-0","COM3");
		controller.addConnector(conn);
		
		conn.addDevice(new GavazziEM24(2));
	}
	
}
