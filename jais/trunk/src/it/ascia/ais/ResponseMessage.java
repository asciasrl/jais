package it.ascia.ais;

/**
 * Is a message that is the response to a request
 * @author Sergio
 *
 */
public interface ResponseMessage {

	/**
	 * @param requestMessage Request Message to this Response
	 */
	public void setRequest(RequestMessage requestMessage);
	
	/**
	 * @return Request Message for this Response
	 */
	public RequestMessage getRequest();

	/**
	 * 
	 * @param requestMessage Request
	 * @return true if this message is a response to given request message 
	 */
	public abstract boolean isResponseTo(RequestMessage requestMessage);

}
