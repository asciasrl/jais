/**
 * Copyright (C) 2008 ASCIA S.R.L.
 */
package it.ascia.ais;

import it.ascia.ais.Bus;
import it.ascia.ais.AISException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


/**
 * Parser di indirizzi.
 * 
 * <p>Questa classe e' necessaria perche' gli indirizzi nel formato dato dal
 * protocollo possono fare riferimento a piu' bus.</p>
 * 
 * <p>Questa classe deve essere istruita su quali bus esistono, quindi puo' 
 * creare oggetti di tipo BusAddress a partire dagli indirizzi.</p>
 * 
 * @author arrigo
 *
 */
public class BusAddressParser {
	/**
	 * Bus registrati.
	 * 
	 * <p>I bus qui dentro sono accessibili dal loro nome (stringa).</p>
	 */
	private Map busses;
	
	public BusAddressParser() {
		busses = new HashMap();
	}
	
	/**
	 * Aggiunge un bus.
	 * 
	 * @param bus il bus da aggiungere.
	 */
	public void registerBus(Bus bus) {
		busses.put(bus.getName(), bus);
	}
	
	/**
	 * Ritorna un Bus a partire dal nome.
	 * 
	 * @return il bus o null se non e' registrato nessun bus con quel nome.
	 */
	public Bus getBus(String name) throws AISException {
		return (Bus)busses.get(name);
	}
	
	/**
	 * Effettua il parsing di address e trova le porte e i Device 
	 * corrispondenti.
	 * 
	 * @return un oggetto di tipo BusAddress, oppure null se il device non e'
	 * stato trovato.
	 * 
	 * @throws un'eccezione se l'indirizzo non e' valido.
	 */
	public BusAddress parseAddress(String address) throws AISException {
		String ports, temp[], temp2, busName, deviceString;
		int index, deviceAddress;
		Bus bus;
		Device device;
		BusAddress retval = null;
		// Prima cosa: trovare i ":" per dividere porte e device.
		temp = address.split(":");
		if (temp.length != 2) {
			throw new AISException("L'indirizzo deve contenere uno e un solo " +
					"\":\"");
		}
		temp2 = temp[0]; // [[luogo.]tipo_bus.]numero_bus.indirizzo_device
		ports = temp[1]; // porta/e
		index = temp2.lastIndexOf(".");
		if (index == -1) {
			throw new AISException("Numero bus e indirizzo non sono separati " +
					"da \".\"");
		}
		try {
			deviceString = temp2.substring(index + 1);
			deviceAddress = Integer.parseInt(deviceString);
		} catch (NumberFormatException e) {
			throw new AISException("Indirizzo dispositivo non numerico.");
		}
		// L'indirizzo e' valido -- ora bisogna vedere a cosa punta
		busName = temp2.substring(0, index);
		bus = getBus(busName);
		if (bus == null) {
			throw new AISException("Bus \"" + busName + 
					"\" inesistente o non registrato.");
		}
		retval = new BusAddress(address, busName, deviceString, ports);
		device = bus.getDevice(deviceAddress);
		if (device == null) {
			throw new AISException("Il device " + deviceAddress + " non " +
					"esiste nel bus " + busName);
		}
		retval.addDevice(device);
		return retval;
	}
}
