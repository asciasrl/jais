package it.ascia.bentel.msg;

import java.util.List;
import java.util.Vector;

import org.apache.tools.ant.taskdefs.condition.IsReference;

import it.ascia.ais.AISException;
import it.ascia.ais.Message;

public abstract class BentelKyoMessage extends Message {

	Vector<Integer> request;
	Vector<Integer> response;
	private boolean isResponded = false;
	
	@SuppressWarnings("unchecked")
	protected void loadRequest(Vector<Byte> buffer) {
		request = (Vector<Integer>) buffer.clone();
		if (!verifyChecksum(request)) {
			throw(new AISException("Uncorrect request checksum"));
		}
	}
	
	/**
	 * 
	 * @param buffer
	 * @param calculateChecksum if true calculate checksum, if false check checksum
	 * @throws AISException If checksum in incorrect 
	 */
	protected void loadRequest(int[] buffer, boolean calculateChecksum) {
		request = new Vector<Integer>(buffer.length);
		for (int b : buffer) {
			request.add(new Integer(b & 0xff));			
		}
		if (calculateChecksum) {
			request.add(new Integer(checkSum(buffer)));
		} else {
			if (!verifyChecksum(request)) {
				throw(new AISException("Uncorrect checksum"));
			}
		}
	}

	/**
	 * Load request and check checksum
	 * @param buffer
	 */
	protected void loadRequest(int[] buffer) {
		loadRequest(buffer,false);
	}

	/**
	 * Load response and verify checksum
	 * @param list
	 */
	public void loadResponse(List<Integer> list) {
		response =  new Vector<Integer>(list);
		if (!verifyChecksum(response)) {
			throw(new AISException("Uncorrect response checksum"));
		}
		setResponded();
	}

	private void setResponded() {
		isResponded = true;		
	}

	/*
	public void loadResponse(byte[] buffer) {
		response = new Vector<Integer>(buffer.length);
		for (byte b : buffer) {
			response.add(new Integer(b));			
		}
	}
	*/

	public byte[] getBytesMessage() {
		byte[] b = new byte[request.size()];
		for (int i=0; i<b.length; i++) {
		   b[i] = request.get(i).byteValue();
		}
		return b;
	}


	/**
	 * Calcola il checksum.
	 */
	protected int checkSum(int[] b) {
		int v = 0;
		for (int i = 0; i < (b.length - 1); i++) {
			v = (v + b[i]) & 0xff;
		}
		return v;
	}
	
	protected boolean verifyChecksum(Vector<Integer> msg) {
		if (msg.size() < 2) {
			return false;
		}
		int chk = 0;
		for (int i = 0; i < msg.size() - 1; i++) {
			chk = (chk + msg.elementAt(i).intValue()) & 0xff;
		}
		return chk == msg.get(msg.size()-1).intValue();
	}

	public int getRequestSize() {
		return 6;
	}
	
	/**
	 * 
	 * @return Length in byte of expected response
	 */
	public abstract int getResponseSize();

	/**
	 * Check if buffer cointains the echo of this request message
	 * @param list buffer
	 */
	public boolean checkEcho(List<Integer> list) {
		synchronized (list) {
			if (list.size() > request.size()) {
				return request.equals(list.subList(0, request.size()));
			} else if (list.size() < request.size()) {
				return false;
			} else {
				return request.equals(list); 
			}
		}
	}

	/**
	 * 
	 * @return time to wait for response
	 */
	public abstract long getResponseTimeout();

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(getClass().getSimpleName());
		sb.append(" Request:"+dumpRequest());
		sb.append(" Response:"+dumpResponse());		
		return sb.toString();
	}

	/**
	 * 
	 * @return request as hex string and text string
	 */
	protected String dumpRequest() {
		if (request == null) {
			return "[]";
		} else {
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < request.size() - 1; i++) {
				sb.append(" "+b2h(request.elementAt(i).intValue()));
			}
			sb.append(" '");
			for (int i = 0; i < request.size() - 1; i++) {
				sb.append((char)request.elementAt(i).intValue());
			}
			sb.append("'");
			return sb.toString();
		}
	}

	/**
	 * 
	 * @return response as hex string and text string
	 */
	protected String dumpResponse() {
		if (response == null) {
			return "[]";
		} else {
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < response.size() - 1; i++) {
				sb.append(" "+b2h(response.elementAt(i).intValue()));
			}
			sb.append(" '"+getResponseAsText()+"'");
			return sb.toString();
		}
	}
	
	/**
	 * @return Response as text string
	 */
	protected String getResponseAsText() {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < response.size() - 1; i++) {
			sb.append((char)response.elementAt(i).intValue());
		}		
		return sb.toString();
	}
	
	@SuppressWarnings("unchecked")
	public Vector<Integer> getResponse() {
		if (response != null && response.size() > 1) {
			Vector<Integer> res = (Vector) response.clone();
			res.removeElementAt(res.size() - 1);
			return res;
		} else {
			return null;
		}
	}

	/**
	 * @return the isResponded
	 */
	public boolean isResponded() {
		if (getResponseSize() == 0) {
			return isSent();			
		}
		return isResponded;
	}
}
