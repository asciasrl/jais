package it.ascia.modbus;

public class ModbusWordPort extends ModbusPort {

	public ModbusWordPort(int PhysicalAddress, int bytes) {
		super(PhysicalAddress, bytes);
	}

	public ModbusWordPort(int physicalAddress) {
		super(physicalAddress,2);
	}

	public ModbusWordPort(int address, double factor, String decimalformat) {
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
