package it.ascia.modbus;

/**
 * Registra un valore Short per contenere un intero a 8 bit senza segno
 * @author Sergio
 * @since 20120207
 */
public class ModbusBytePort extends ModbusPort {

	public ModbusBytePort(int physicalAddress) {
		super(physicalAddress, 1);
	}

	public ModbusBytePort(int address, double factor, String decimalformat) {
		this(address);
		setFactor(factor);
		setDecimalFormat(decimalformat);
	}

	@Override
	protected Object normalize(Object newValue) throws IllegalArgumentException {
		if (Byte.class.isInstance(newValue)) {
			return newValue;
		} else if (Long.class.isInstance(newValue)) {
			if ((Long)newValue > Byte.MAX_VALUE || (Long)newValue < Byte.MIN_VALUE) {
		        throw new IllegalArgumentException(newValue + " cannot be cast to byte without changing its value.");
			}
			return new Byte(((Long)newValue).byteValue());
		} else {
			throw(new IllegalArgumentException("Not a byte: " + newValue.getClass().getCanonicalName()));
		}
	}
	

}
