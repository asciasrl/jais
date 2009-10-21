package it.ascia.ais;

public class TemperatureSetpointPort extends AnalogOutputPort {

	/**
	 * Porta che viene aggiornata una volta ogni ora
	 * @param device
	 * @param portId
	 */
	public TemperatureSetpointPort(Device device, String portId) {
		super(device, portId);
		this.setCacheRetention(3600000);
	}

	public TemperatureSetpointPort(Device device, String portId, String portName) {
		super(device, portId, portName);
		// TODO Auto-generated constructor stub
	}

	public TemperatureSetpointPort(Device device, String portId,
			String portName, Double minValue, Double maxValue) {
		super(device, portId, portName, minValue, maxValue);
		// TODO Auto-generated constructor stub
	}

}
