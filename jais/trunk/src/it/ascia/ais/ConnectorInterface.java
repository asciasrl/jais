package it.ascia.ais;

import java.util.Collection;
import java.util.Vector;

public interface ConnectorInterface {

    /**
     * Restituisce il nome del connettore, come specificato alla creazione
     * @return Nome
     */
	public String getName();
	
	/**
     * Aggiunge un dispositivo all'elenco di quelli gestiti
     * Non sono ammessi duplicati
     * @param device
     * @throws AISException
     */
    public void addDevice(Device device) throws AISException;

    /**
     * 
     * @param addr Indirizzo
     * @return Tutti i device che corrispondono all'indirizzo
     */
    public Vector<Device> getDevices(Address addr);

	/**
	 * 
	 * @return Tutti i devices del connettore
	 */
    public Collection<Device> getDevices();

	/**
	 * Chiude il connettore 
	 */    
	public void close();

    /**
     * Invia un messaggio e attende una risposta dal destinatario, se il
     * messaggio lo richiede.
     * 
     * @return true se il messaggio di risposta e' arrivato, o se l'invio e'
     * andato a buon fine.
     * @throws AISException 
     */
    public boolean sendMessage(Message m);

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
	 */
	public void queueUpdate(DevicePort devicePort);

	public boolean isAlive();

	/**
	 * Avvia i thread del connettore che eseguono operazioni automatiche
	 */
	public void start();


}
