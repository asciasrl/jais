package it.ascia.ais;

import java.util.Set;

/**
 * Connector domotico.
 * 
 * <p>Un Connector e' un'interfaccia per JAIS verso un sistema. Ad esempio un
 * bus domotico o una centrale d'allarme.</p>
 * 
 * <p>Un Connector permette l'accesso a un insieme di {@link Device}.</p>
 * 
 * <p>A livello di protocollo, un connector e' identificato da un nome</p>
 * 
 * @author sergio@ascia.it
 */

public interface ConnectorInterface {

    /**
     * Restituisce il nome del connettore, come specificato alla creazione
     * @return Nome
     */
	public String getConnectorName();
	
	/**
     * Aggiunge un dispositivo all'elenco di quelli gestiti
     * Non sono ammessi duplicati per gli indirizzi
     * @param deviceAdrress indirizzo
     * @param device il device
     * @throws AISException
     */
    public void addDevice(String deviceAddress, Device device) throws AISException;

    public void addDevice(Device device) throws AISException;
    
    /**
     * 
     * @param deviceAddress
     * @return the device having the address or null
     */
    public Device getDevice(String deviceAddress);
    
    /**
     * 
     * @param addr Indirizzo
     * @return Tutti i device che corrispondono all'indirizzo
     */
    public Set<Device> getDevices(Address addr);

	/**
	 * 
	 * @return Tutti i devices del connettore
	 */
    public Set<Device> getDevices();

    /**
     * Invia un messaggio e attende una risposta dal destinatario, se il
     * messaggio lo richiede.
     * 
     * @return true se il messaggio di risposta e' arrivato, o se l'invio e'
     * andato a buon fine.
     * @throws AISException 
     */
    //public boolean sendMessage(Message m);

    /**
     * 
     * @return true if module is running, false if must stop or is stopped
     */
    public boolean isRunning();

    /**
     * 
     * @param the Controller Module to which connector belongs
     */
    public void setModule(ControllerModule controllerModule);
    
	/**
	 * @return Get the Controller Module to which connector belongs
	 */
	public ControllerModule getModule();

	/**
	 * Request to the connector to update the port.  This method return immediately.
	 * @param devicePort Port to be updated
	 * @deprecated Remove this method from general pourposes connector
	 */
	public void queueUpdate(DevicePort devicePort);

	/**
	 * 
	 * @return true if connetor is alive (watchdog)
	 */
	public boolean isAlive();

	/**
	 * Avvia i thread del connettore che eseguono operazioni automatiche
	 */
	public void start();

	/**
	 * Ferma il connettore 
	 */    
	public void stop();

	/**
	 * Send a message to phisical device
	 * @param m
	 * @return
	 */
	public boolean sendMessage(Message m);

	/**
	 * Questo metodo viene chiamato dal Transport per ogni byte che viene ricevuto
	 * Se il MessageParser ho ottenuto un messaggio valido, viene aggiunto alla coda di dispacciamento
	 * @param b Dato ricevuto
	 */
	public void received(int i);

}
