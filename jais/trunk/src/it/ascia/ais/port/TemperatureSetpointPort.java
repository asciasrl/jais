package it.ascia.ais.port;

import it.ascia.ais.Device;

public class TemperatureSetpointPort extends AnalogOutputPort {

	/**
	 * Porta che viene aggiornata una volta ogni ora
	 * @param device
	 * @param portId
	 */
	public TemperatureSetpointPort(String portId) {
		super(portId);
		this.setCacheRetention(36000000); // 1 ora
	}

	public TemperatureSetpointPort(Device device, String portId,
			Double minValue, Double maxValue) {
		super(portId, minValue, maxValue);
	}

}
