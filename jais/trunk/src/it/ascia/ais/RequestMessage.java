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
	public void setResponse(ResponseMessage res);
	
	/**
	 * @return Response Message to this Request
	 */
	public ResponseMessage getResponse();

	/**
	 * 
	 * @param m The 
	 * @return true if given message is the response to this request 
	 */
	public boolean isAnsweredBy(ResponseMessage m);

	/**
	 * @param b Set answered status
	 */
	public void setAnswered(boolean b);
	
	/**
	 * @return the isAnswered status
	 */
	public boolean isAnswered();


}
