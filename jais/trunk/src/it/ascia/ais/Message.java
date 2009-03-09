package it.ascia.ais;

public abstract class Message {

	public abstract String getMessageDescription();
	
	public abstract String toString();
	
	/**
	 *  TODO rivedere Message
	 *  Eventuali metodi da aggiungere:
	 *  - getSource
	 *  - getDestination
	 *  - getValue
	 */
	
	public abstract String getSource();
	
	public abstract String getDestination();
}