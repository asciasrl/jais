package it.ascia.modbus;

import java.text.DecimalFormat;

import net.wimpi.modbus.msg.ReadInputRegistersResponse;
import net.wimpi.modbus.procimg.InputRegister;
import it.ascia.ais.AISException;
import it.ascia.ais.DevicePort;

public abstract class ModbusPort extends DevicePort {

	/**
	 * Indirizzo iniziale della porta: 0 = 0x301 
	 */
	private int physicalAddress;
	
	/**
	 * Numero di bytes che compongono il valore della porta 
	 */
	private int bytes;
	
	/**
	 * 
	 * @param physicalAddress Indirizzo iniziale della porta: 0 = 0x301 
	 * @param bytes Numero di bytes che compongono il valore della porta
	 */
	public ModbusPort(int physicalAddress, int bytes) {
		super((new Integer(physicalAddress)).toString());
		this.physicalAddress = physicalAddress;
		this.bytes = bytes;
	}

	/**
	 * @return Indirizzo del registro iniziale della porta: 0 = 0x301
	 */
	public int getPhysicalAddress() {
		return physicalAddress;
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
	
	public void setValue(ReadInputRegistersResponse res) {
		InputRegister[] reg = res.getRegisters();
		if (reg.length == getWords()) {
			long newValue = 0;
			for (int i = 0; i < reg.length; i++ ) {
				newValue |= (reg[i].getValue() & 0xFFFF) << (i * 16);
			}
			setValue(newValue);
		} else {
			throw(new AISException("Lunghezza risposta errata: " + reg.length + " invece di " + getWords()));
		}
		
	}
		
}
