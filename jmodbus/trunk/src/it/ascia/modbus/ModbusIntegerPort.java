package it.ascia.modbus;

public class ModbusIntegerPort extends ModbusPort {

	public ModbusIntegerPort(int PhysicalAddress, int bytes) {
		super(PhysicalAddress, bytes);
	}

	public ModbusIntegerPort(int physicalAddress) {
		super(physicalAddress,4);
	}

	public ModbusIntegerPort(int address, double factor, String decimalformat) {
		this(address);
		setFactor(factor);
		setDecimalFormat(decimalformat);		
	}

	@Override
	protected Object normalize(Object newValue) throws IllegalArgumentException {
		if (Integer.class.isInstance(newValue)) {
			return newValue;
		} else if (Long.class.isInstance(newValue)) {
			if ((Long)newValue > Integer.MAX_VALUE || (Long)newValue < Integer.MIN_VALUE) {
		        throw new IllegalArgumentException(newValue + " cannot be cast to integer without changing its value.");
			}
			return new Integer(((Long)newValue).intValue());
		} else {
			throw(new IllegalArgumentException("Not an integer: " + newValue.getClass().getCanonicalName()));
		}
	}
		
}
