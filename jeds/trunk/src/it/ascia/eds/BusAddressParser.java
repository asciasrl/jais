/**
 * Copyright (C) 2008 ASCIA S.R.L.
 */
package it.ascia.eds;

import it.ascia.eds.device.Device;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


/**
 * Parser di indirizzi.
 * 
 * Questa classe e' necessaria perche' gli indirizzi nel formato dato dal
 * protocollo possono fare riferimento a piu' bus.
 * 
 * Questa classe deve essere istruita su quali bus esistono, quindi puo' creare
 * oggetti di tipo BusAddress a partire dagli indirizzi.
 * 
 * @author arrigo
 *
 */
public class BusAddressParser {
	/**
	 * Bus registrati.
	 * 
	 * I bus qui dentro sono accessibili dal loro nome (stringa).
	 */
	private Map busses;
	
	public BusAddressParser() {
		busses = new HashMap();
	}
	
	/**
	 * Aggiunge un bus identificato dal solo numero.
	 * 
	 * Attenzione: tutti i bus devono avere un tipo, oppure non deve averlo
	 * nessuno.
	 * 
	 * @param bus il bus da aggiungere.
	 * @param busNumber il numero del bus.
	 */
	public void registerBus(Bus bus, int busNumber) {
		busses.put(new Integer(busNumber).toString(), bus);
	}
	
	/**
	 * Aggiunge un bus identificato da nome e tipo.
	 * 
 	 * Attenzione: tutti i bus devono avere un tipo, oppure non deve averlo
	 * nessuno.
	 * 
	 * @param bus il bus da aggiungere.
	 * @param name il tipo associato al bus.
	 * @param busNumber il numero del bus.
	 */
	public void registerBus(Bus bus, String name, int busNumber) {
		busses.put(name + "." + busNumber, bus);
	}
	
	/**
	 * Ritorna un Bus a partire dal nome.
	 * 
	 * @return il bus o null se non e' registrato nessun bus con quel nome.
	 */
	public Bus getBus(String name) throws EDSException {
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
	public BusAddress parseAddress(String address) throws EDSException {
		String ports, temp[], temp2, busName, deviceString;
		int index, deviceAddress;
		Bus bus;
		Device device;
		BusAddress retval = null;
		// Prima cosa: trovare i ":" per dividere porte e device.
		temp = address.split(":");
		if (temp.length != 2) {
			throw new EDSException("L'indirizzo deve contenere uno e un solo " +
					"\":\"");
		}
		temp2 = temp[0]; // [[luogo.]tipo_bus.]numero_bus.indirizzo_device
		ports = temp[1]; // porta/e
		index = temp2.lastIndexOf(".");
		if (index == -1) {
			throw new EDSException("Numero bus e indirizzo non sono separati " +
					"da \".\"");
		}
		try {
			deviceString = temp2.substring(index + 1);
			deviceAddress = Integer.parseInt(deviceString);
		} catch (NumberFormatException e) {
			throw new EDSException("Indirizzo dispositivo non numerico.");
		}
		// L'indirizzo e' valido -- ora bisogna vedere a cosa punta
		busName = temp2.substring(0, index);
		bus = getBus(busName);
		if (bus == null) {
			throw new EDSException("Bus \"" + busName + 
					"\" inesistente o non registrato.");
		}
		retval = new BusAddress(address, busName, deviceString, ports);
		device = bus.getDevice(deviceAddress);
		if (device == null) {
			throw new EDSException("Il device " + deviceAddress + " non " +
					"esiste nel bus " + busName);
		}
		retval.addDevice(device);
		return retval;
	}
}
