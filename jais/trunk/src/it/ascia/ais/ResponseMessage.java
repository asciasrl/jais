package it.ascia.ais;

/**
 * Is a message that is the response to a request
 * @author Sergio
 *
 */
public interface ResponseMessage {

	/**
	 * @param messageToBeAnswered Request Message to this Response
	 */
	public void setRequest(RequestMessage messageToBeAnswered);
	
	/**
	 * @return Request Message for this Response
	 */
	public Message getRequest();

	/**
	 * 
	 * @param m Request
	 * @return true if this message is a response to given request message 
	 */
	public abstract boolean isResponseTo(RequestMessage m);

}
