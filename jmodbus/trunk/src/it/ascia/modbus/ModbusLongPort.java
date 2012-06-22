package it.ascia.modbus;

/**
 * Registra un valore Long per contenere un intero a 32 bit senza segno
 * 
 * @author Sergio
 * @since 20120207
 */
public class ModbusLongPort extends ModbusPort {

	public ModbusLongPort(int physicalAddress) {
		super(physicalAddress, 4);
	}

	public ModbusLongPort(int address, double factor, String decimalformat) {
		this(address);
		setFactor(factor);
		setDecimalFormat(decimalformat);		
	}

	@Override
	protected Object normalize(Object newValue) throws IllegalArgumentException {
		if (Long.class.isInstance(newValue)) {
			return newValue;
		} else {
			throw (new IllegalArgumentException("Not a Long"));
		}
	}

	protected void setValue(long l) {
		setValue(new Long(l));
	}

}
