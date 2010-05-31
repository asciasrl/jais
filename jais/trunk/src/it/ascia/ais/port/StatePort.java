package it.ascia.ais.port;

import it.ascia.ais.AISException;
import it.ascia.ais.DevicePort;

public class StatePort extends DevicePort {

	private String[] tags = null;

	public StatePort(String portId, String[] tags) {
		super(portId);
		setTags(tags);
	}
	
	/**
	 * Ritorna l'indice del tag che corrisponde al valore
	 */
	public Integer getTagIndex(String tag) {
		if (tags == null) {
			return null;
		}
		for(int i=0; i < tags.length; i++) {
			if (tags[i].equalsIgnoreCase(tag)) {
				return new Integer(i);
			}
		}
		throw(new AISException("Tag is invalid: " + tag));
	}

	/**
	 * Se il valore deve essere uno dei valori di un insieme, questo metodo ne
	 * fornisce l'elenco
	 */
	public String[] getTags() {
		return this.tags;
	}

	/**
	 * @param tags the tags to set
	 */
	public void setTags(String[] tags) {
		this.tags = tags;
	}

	@Override
	protected Object normalize(Object newValue) throws IllegalArgumentException {
		if (newValue == null) {
			return newValue;
		} else if (Integer.class.isInstance(newValue)) {
			Integer i = (Integer) newValue;
			if (tags == null) {
				return super.normalize(newValue);
			}
			if (i > tags.length) {
				throw(new IllegalArgumentException("Value too high (max "+tags.length+"): "+i));				
			}
			if (i < 0) {
				throw(new IllegalArgumentException("Value too low (min 0) : "+i));				
			}
			return tags[i];
		} else if (String.class.isInstance(newValue)) {
			for(int i=0; i < tags.length; i++) {
				if (tags[i].equalsIgnoreCase((String) newValue)) {
					return i;
				}
			}
			throw(new AISException("Tag is invalid: " + newValue));
		} else {
			throw(new IllegalArgumentException("Value of wrong class: "+newValue.getClass().getCanonicalName()));
		}
		
	}

}