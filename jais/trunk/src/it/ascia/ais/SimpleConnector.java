package it.ascia.ais;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Vector;

import org.apache.log4j.Logger;

/**
 * Implementazione minima della interfaccia connettore
 * @author Sergio
 *
 */
public abstract class SimpleConnector implements ConnectorInterface {

    /**
     * Il nostro nome secondo AUI.
     */
    private String name;
    
	/**
	 * I dispositivi presenti nel sistema gestiti da questo connettore.
	 */
    private LinkedHashMap<String,Device> devices;

	/**
     * Il nostro logger.
     */
    public Logger logger;
    
    /**
     * Stato del connettore
     */
    private boolean running = false;

    /**
     * Variabile del watchdog
     */
	protected boolean isalive = true;
	
	public boolean isAlive() {
		if (isalive) {
			isalive = false;
			return true;
		} else {
			return false;
		}
	}

	/**
	 * L'elenco degli indirizzi - primari o alias - dei dispositivi.
	 */
    private LinkedHashMap<String, Device> devicesAlias;

    /**
     * Modulo a cui appartiene il connettore
     */
	private ControllerModule module = null;

	/**
	 * 
	 * @param name Nome del connettore
	 */
	public SimpleConnector(String name) {
		this.name = name;
        devices = new LinkedHashMap<String, Device>();
        devicesAlias = new LinkedHashMap<String, Device>();
		logger = Logger.getLogger(getClass());
		running = true;
	}

	@Override
	public String getName() {
		return name;
	}


	@Override
    public void addDevice(Device device) throws AISException {    	
    	String address = device.getSimpleAddress();
    	if (devices.containsKey(address)) {
    		throw(new AISException("Dispositivo duplicato con indirizzo "+address+" connettore "+getName()));
    	}
    	devices.put(address, device);
    	addDeviceAlias(address, device);
    	device.setConnector(this);
    }
    
    /**
     * Aggiunge un indirizzo "alias" che corrisponde allo stesso device fisico
     * Se l'alias e' gia' presente viene semplicemente ridefinito
     * @param address Indirizzo alias del dispositivo
     * @param device Dispositivo effettivo
     */
    public void addDeviceAlias(String address, Device device) {
    	devicesAlias.put(address, device);
    }
    
    public Device getDevice(Address address) {
    	return devicesAlias.get(address.getDeviceAddress());
    }

	/**
     * Ritorna tutti i Device collegati che rispondono a un certo indirizzo.
     * 
     * @param addr l'indirizzo da cercare.
     * @return Elenco devices (eventualmente vuoto)
     */
    public Vector<Device> getDevices(Address addr) {
    	Vector<Device> res = new Vector<Device>();
    	Device device1 = getDevice(addr);
    	if (device1 != null) {
    		res.add(device1);
    	} else {
			for (Device device : devicesAlias.values()) {
				if (addr.matches(device.getAddress())) {
					res.add(device);
				}
			}
    	}
    	return res;    	
    }

    /**
     * 
     * @return all devices belonging to connector
     */
    public Collection<Device> getDevices() {
		return devices.values();
    }

	public boolean isRunning() {
		return running;
	}
        
	@Override
	public void close() {
		running = false;
	}

	public void setModule(ControllerModule controllerModule) {
		this.module = controllerModule;		
	}

	public ControllerModule getModule() {
		if (module == null) {
			throw(new IllegalStateException("Connector " + getName() + " not assigned to a module"));
		}
		return module;
	}


}
