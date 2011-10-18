package it.ascia.modbus;

import it.ascia.ais.AISException;
import net.wimpi.modbus.msg.ModbusResponse;

public class GavazziEM24 extends ModbusDevice {

	public GavazziEM24(int unitId) throws AISException {
		super(unitId);
		for (int i=0; i < 24; i++) {
			addPort(new ModbusInt32Port(i*2));
		}
	}

}
