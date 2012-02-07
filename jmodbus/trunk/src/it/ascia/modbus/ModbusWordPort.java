package it.ascia.modbus;


/**
 * Registra un valore Integer per contenere un intero a 16 bit senza segno
 * @author Sergio
 * @since 20120207
 */
public class ModbusWordPort extends ModbusIntegerPort {

	public ModbusWordPort(int i) {
		super(i,2);
	}

	@Override
	protected void setValue(long l) {
		setValue(new Long(l).intValue());		
	}

	protected void setValue(int i) {
		setValue(new Integer(i));		
	}

}
