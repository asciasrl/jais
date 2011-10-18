package it.ascia.modbus;

public class ModbusInt32Port extends ModbusPort {

	public ModbusInt32Port(int i) {
		super(i);
	}

	@Override
	protected Object normalize(Object newValue) throws IllegalArgumentException {
		if (Integer.class.isInstance(newValue)) {
			return newValue;
		}
		return null;
	}

}
