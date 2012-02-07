package it.ascia.modbus;

/**
 * Registra un valore Long per contenere un intero a 32 bit senza segno
 * @author Sergio
 * @since 20120207
 */
public class ModbusLongPort extends ModbusIntegerPort {

	public ModbusLongPort(int i) {
		super(i,4);
	}

	@Override
	protected void setValue(long l) {
		setValue(new Long(l));
	}

}
