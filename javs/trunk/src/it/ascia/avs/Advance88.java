package it.ascia.avs;

import it.ascia.ais.Connector;
import it.ascia.ais.port.DigitalInputPort;
import it.ascia.ais.port.DigitalOutputPort;
import it.ascia.ais.port.StatePort;

public class Advance88 extends CentraleAVS {

	private static final int NUM_ZONE_DIG = 88;

	private static final int NUM_OC_DIG = 40;
	private static final int NUM_RELAYS = 5;
	private static final int NUM_SECTORS = 4;
	private static final int NUM_SIRENS = 4;

	/**
	 * Add the device "Advance88" with ports and add zones to connector
	 */
	public Advance88() {
		super("Advance88");
	}

	@Override
	protected int getNumZone() {
		return NUM_ZONE_DIG;
	}

}
