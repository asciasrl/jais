/**
 * Copyright (C) 2008 ASCIA S.r.l.
 */
package it.ascia.ais;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Questa classe deve essere estesa da controllori per bus domotici.
 * 
 * @author arrigo
 */
public abstract class Controller {
	/**
	 * Connector registrati.
	 * 
	 * <p>I Connector qui dentro sono accessibili dal loro nome (stringa).</p>
	 */
	private Map connectors;
	
	/**
	 * Aggiunge un Connector alla lista di quelli gestiti.
	 * 
	 * @param connector il connector da aggiungere.
	 */
	public void registerConnector(Connector connector) {
		connectors.put(connector.getName(), connector);
	}
	
	/**
	 * Effettua il parsing di un indirizzo nella forma "device:porta".
	 * 
	 * @return il nome del Device indicato nell'indirizzo (la parte prima dei 
	 * ":").
	 * 
	 * @throws un'eccezione se l'indirizzo non e' valido.
	 */
	protected String getDeviceFromAddress(String address) throws AISException {
		String temp[];
		// Prima cosa: trovare i ":" per dividere porte e device.
		temp = address.split(":");
		if (temp.length != 2) {
			throw new AISException("L'indirizzo deve contenere uno e un solo " +
					"\":\"");
		}
		return temp[0];
	}
	
	/**
	 * Effettua il parsing di un indirizzo nella forma "device:porta".
	 * 
	 * @return il nome della porta indicata nell'indirizzo (la parte dopo i 
	 * ":").
	 * 
	 * @throws un'eccezione se l'indirizzo non e' valido.
	 */
	protected String getPortFromAddress(String address) throws AISException {
		String temp[];
		// Prima cosa: trovare i ":" per dividere porte e device.
		temp = address.split(":");
		if (temp.length != 2) {
			throw new AISException("L'indirizzo deve contenere uno e un solo " +
					"\":\"");
		}
		return temp[1];
	}

	
	/**
	 * Cerca uno o piu' Device a partire da un indirizzo.
	 * 
	 * <p>Perche' i Device siano rintracciabili, il loro Connector deve essere
	 * stato preventivamente registrato usando registerConnector.
	 * 
	 * @param address indirizzo del/dei Device.
	 * 
	 * @return i Device rispondenti all'indirizzo indicato.
	 */
	protected Device[] findDevices(String address) throws AISException {
		String connectorName = null;
		String deviceAddress = null;
		Connector connector = null;
		Device retval[];
		Iterator it;
		if (address.equals("*")) {
			// Interroghiamo tutti i connettori che conosciamo
			Set devices = new HashSet();
			Device temp[] = null;
			it = connectors.values().iterator();
			while (it.hasNext()) {
				connector = (Connector)it.next();
				temp = connector.getDevices("*");
				for (int i = 0; i < temp.length; i++) {
					devices.add(temp[i]);
				}
			}
			retval = (Device[])devices.toArray(temp);
		} else {
			// Ricerchiamo il connettore tra tutti quelli che conosciamo.
			it = connectors.keySet().iterator();
			while (it.hasNext() && (connector == null)) {
				connectorName = (String)it.next();
				if (address.indexOf(connectorName) == 0) {
					// L'indirizzo comincia per questo!
					connector = (Connector)connectors.get(connectorName);
				}
			}
			if (connector == null) {
				throw new AISException("Impossibile trovare il connector per " +
						"l'indirizzo \"" + address + "\".");
			}
			try {
				// Sanity check
				if (address.charAt(connectorName.length()) != '.') {
					throw new AISException("Il nome del connector \"" + 
							connectorName +	
							"\" non e' seguito da un '.' nell'indirizzo: \"" +
							address + "\"");
				}
				deviceAddress = address.substring(connectorName.length() + 1);
				retval = connector.getDevices(deviceAddress);
			} catch (StringIndexOutOfBoundsException e) {
				throw new AISException("Impossibile distinguere connector e " +
						"device nell'indirizzo \"" + address + "\"");
			}
			if (retval.length == 0) {
				throw new AISException("Il device " + deviceAddress + " non " +
						"esiste nel connector " + connectorName);
			}
		}
		return retval;
	}
	
	public Controller() {
		connectors = new HashMap();
	}
	
	/**
	 * Il cuore del controllore: riceve la richiesta e produce una risposta.
	 * 
	 * @param command comando
	 * @param name indirizzo del/dei device interessati
	 * @param value parametri del comando (puo' essere null)
	 * @param pin pin
	 */
	public abstract String receiveRequest(String command, String name, 
			String value, String pin);
}

