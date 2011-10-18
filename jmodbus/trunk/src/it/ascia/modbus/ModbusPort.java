package it.ascia.modbus;

import it.ascia.ais.DevicePort;

public abstract class ModbusPort extends DevicePort {

	private int PhysicalAddress;
	
	public ModbusPort(int PhysicalAddress) {
		super((new Integer(PhysicalAddress)).toString());
		this.PhysicalAddress = PhysicalAddress;
	}

	public int getPhysicalAddress() {
		return PhysicalAddress;
	}


}
