package it.ascia.modbus;

import it.ascia.ais.AISException;
import net.wimpi.modbus.msg.ReadInputRegistersResponse;
import net.wimpi.modbus.procimg.InputRegister;

public abstract class ModbusIntegerPort extends ModbusPort {

	public ModbusIntegerPort(int PhysicalAddress, int bytes) {
		super(PhysicalAddress, bytes);
	}

	@Override
	protected Object normalize(Object newValue) throws IllegalArgumentException {
		if (Integer.class.isInstance(newValue)) {
			return newValue;
		}
		return null;
	}
	
	@Override
	public void setValue(ReadInputRegistersResponse res) {
		InputRegister[] reg = res.getRegisters();
		if (reg.length == getWords()) {
			long newValue = 0;
			for (int i = 0; i < reg.length; i++ ) {
				newValue |= reg[i].getValue() << (i * 8);
			}
			setValue(newValue);
		} else {
			throw(new AISException("Lunghezza risposta errata: " + reg.length + " invece di " + getWords()));
		}
		
	}
	
	protected abstract void setValue(long l);
}
