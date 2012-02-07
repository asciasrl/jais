package it.ascia.ais;

/**
 * Is a message that have a response
 * @author Sergio
 *
 */
public interface RequestMessage {

	/**
	 * @param res Response Message to this Request
	 */
	public void setResponse(Message res);
	
	/**
	 * @return Response Message to this Request
	 */
	public Message getResponse();

}
