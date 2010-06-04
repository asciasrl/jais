package it.ascia.ais.port;

public class AnalogOutputPort extends DoublePort {
	
	/*
	Double minValue = null;
	Double maxValue = null;

	*/
	
	public AnalogOutputPort(String portId) {
		super(portId);
	}

	/*
	public AnalogOutputPort(String portId, Double minValue, Double maxValue) {
		this(portId);
		this.minValue = minValue;
		this.maxValue = maxValue;		
	}

	public boolean writeValue(Object newValue) throws IllegalArgumentException {
		if (Integer.class.isInstance(newValue)) {
			return super.writeValue((Integer)newValue);
		} else if (Double.class.isInstance(newValue)) {
			Double d = (Double)newValue;
			if (maxValue != null && d.compareTo(maxValue) > 0) {
				throw new IllegalArgumentException(getAddress() + " Valore superiore al massimo ("+maxValue+"): "+newValue);				
			}
			if (minValue != null && d.compareTo(minValue) < 0) {
				throw new IllegalArgumentException(getFullAddress() + " Valore minore del minimo ("+minValue+"): "+newValue);				
			}
			return super.writeValue(newValue);
		} else if (String.class.isInstance(newValue)) {
			return super.writeValue((String) newValue);
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
	*/

}
