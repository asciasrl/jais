package it.ascia.modbus;

/**
 * Registra un valore Short per contenere un intero a 8 bit senza segno
 * @author Sergio
 * @since 20120207
 */
public class ModbusBytePort extends ModbusIntegerPort {

	public ModbusBytePort(int PhysicalAddress) {
		super(PhysicalAddress, 1);
	}

	@Override
	protected void setValue(long l) {
		setValue(new Long(l).intValue());		
	}

	protected void setValue(int i) {
		setValue(new Integer(i).shortValue());		
	}

	protected void setValue(short s) {
		setValue(new Short(s));		
	}

}
