package it.ascia.ais;

public class AnalogOutputPort extends DevicePort {
	
	Double minValue = null;
	Double maxValue = null;

	public AnalogOutputPort(Device device, String portId) {
		super(device, portId);
	}

	public AnalogOutputPort(Device device, String portId, String portName) {
		super(device, portId, portName);
	}

	public AnalogOutputPort(Device device, String portId, String portName, Double minValue, Double maxValue) {
		this(device, portId, portName);
		this.minValue = minValue;
		this.maxValue = maxValue;		
	}

	public boolean writeValue(Object newValue) throws IllegalArgumentException {
		if (Integer.class.isInstance(newValue)) {
			return super.writeValue((Integer)newValue);
		} else if (Double.class.isInstance(newValue)) {
			Double d = (Double)newValue;
			if (d.compareTo(maxValue) > 0) {
				throw new IllegalArgumentException(getFullAddress() + " Valore superiore al massimo ("+maxValue+"): "+newValue);				
			}
			if (d.compareTo(minValue) < 0) {
				throw new IllegalArgumentException(getFullAddress() + " Valore minore del minimo ("+minValue+"): "+newValue);				
			}
			return super.writeValue((Double)newValue);
		} else if (String.class.isInstance(newValue)) {
			return writeValue((String) newValue);
		}
		throw new IllegalArgumentException(getFullAddress() + " Tipo di valore non valido: "+newValue.getClass().getName());
	}

	public boolean writeValue(String text) throws IllegalArgumentException {
		try {
			double d = Double.parseDouble(text);
			return writeValue(new Double(d));
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException(getFullAddress() + " Valore non valido: "+text);
		}
	}

}
