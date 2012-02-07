package it.ascia.modbus;

import net.wimpi.modbus.msg.ReadInputRegistersResponse;
import it.ascia.ais.DevicePort;

public abstract class ModbusPort extends DevicePort {

	/**
	 * Indirizzo iniziale della porta: 0 = 0x301 
	 */
	private int PhysicalAddress;
	
	/**
	 * Numero di bytes che compongono il valore della porta 
	 */
	private int bytes;
	
	public ModbusPort(int PhysicalAddress, int bytes) {
		super((new Integer(PhysicalAddress)).toString());
		this.PhysicalAddress = PhysicalAddress;
		this.bytes = bytes;
	}

	/**
	 * @return Indirizzo del registro iniziale della porta: 0 = 0x301
	 */
	public int getPhysicalAddress() {
		return PhysicalAddress;
	}

	/**
	 * @return Numero di bytes (8 bit)
	 */
	public int getBytes() {
		return bytes;
	}
	
	/**
	 * @return Numero di word (16 bit)
	 */
	public int getWords() {
		return (bytes + 1) / 2;
		//return (new Double(Math.ceil(bytes / 2))).intValue();
	}
	
	abstract void setValue(ReadInputRegistersResponse res);

}
