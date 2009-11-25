/**
 * 
 */
package it.ascia.ais.port;

import it.ascia.ais.DevicePort;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;


/**
 * La porta slave e' una porta il cui valore, Boolean, dipende dal valore di un'altra porta
 * 
 * @author Sergio
 *
 */
public class SlaveStatePort extends DigitalVirtualPort implements PropertyChangeListener {

	private DevicePort masterPort;
	private String refValue;
	
	/**
	 * Il valore della porta e' true se il valore della porta master e' uguale al nome di questa porta
	 * @param device
	 * @param portId
	 * @param masterPortId Porta master
	 */
	public SlaveStatePort(String portId, DevicePort masterPort) {
		this(portId, masterPort, portId);
	}
	
	/**
	 * Il valore della porta e' true se il valore della porta master e' uguale al valore di riferimento
	 * @param device
	 * @param portId
	 * @param masterPortId Porta master
	 * @param refValue Valore di riferimento che deve assumere la porta master
	 */
	public SlaveStatePort(String portId, DevicePort masterPort, String refValue) {
		super(portId);
		this.masterPort = masterPort;
		this.masterPort.addPropertyChangeListener(this);
		this.refValue = refValue; 
	}

	/**
	 * @return Stato dirty della porta master
	 */
	public boolean isDirty() {
		return masterPort.isDirty();
	}
	
	/**
	 * @return Stato expired della porta master
	 */
	public boolean isExpired() {
		return masterPort.isExpired();
	}
	
	public Object getValue() {
		return getValue(false);		
	}
	
	/**
	 * Calcola il valore di questa porta e ne aggiorna il valore, provocando eventualmente un PropertyChangeEvent 
	 * @see DevicePort.setValue
	 * @param useCache se true previene la richiesta di aggiornamento della porta master
	 * @return true se il valore della porta master e' uguale al valore di riferimento
	 */
	public Object getValue(boolean useCache) {
		String masterValue;
		if (useCache) {
			masterValue = (String) masterPort.getCachedValue();
		} else {
			masterValue = (String) masterPort.getValue();
		}
		Boolean newValue = null;
		if (masterValue != null) {
			newValue = new Boolean(masterValue.equals(refValue));
		}
		setValue(newValue);
		return newValue;
	}

	/**
	 * Esegue getValue(true), avendo come effetto l'esecuzione di setValue di questa porta
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		getValue(true);
	}

	

}
