package it.ascia.ais;

/**
 * <p>Address, when is fully qualified, identify the port of one device of one connector.</p>
 * <p>Each part can be null o a wildcard (*): in this case the address is used to search ports, devices or connectors.</p>
 * 
 * <p>When converted to / from string the syntax is:</p>
 * <p><blockquote><pre>&lt;connector&gt;.&lt;device&gt;:&lt;port&gt;</blockquote></pre></p>
 * 
 * @author Sergio
 */
public class Address implements Comparable<Object> {
	
	private String connector;
	private String device;
	private String port;
	
	/**
	 * 
	 * @param fullAddress Address to be parsed
	 */
	public Address(String fullAddress) {
		this.parse(fullAddress);	
	}

	public Address() {	
	}
	
	/**
	 * 
	 * @param connector Connector name
	 * @param device Device name
	 * @param port Port id
	 */
	public Address(String connector, String device, String port) {
		this.connector = connector;
		this.device = device;
		this.port = port;
	}
	
	/**
	 * 
	 * @param fullAddress Address to be parsed
	 */
	public void parse(String fullAddress) {
		if (fullAddress == null) {
			return;
		}
		int iDot = fullAddress.indexOf(".");
		int iColon = fullAddress.indexOf(":");
		if (iDot > 0) {
			connector = fullAddress.substring(0, iDot);
			if (connector.equals("") || connector.equals("*")) {
				connector = null;
			}
		}
		if (iColon >= 0) {
			port = fullAddress.substring(iColon+1);
			if (port.equals("") || port.equals("*")) {
				port = null;
			}
		} else {
			iColon = fullAddress.length();
		}
		if (iColon > iDot) {
			device = fullAddress.substring(iDot + 1, iColon);
			if (device.equals("") || device.equals("*")) {
				device = null;
			}
		}
	}

	/**
	 * Example: *.*:* 
	 * @return address as connector.device:port
	 */
	public String getFullAddress() {
		return getConnectorName() + "." + getDeviceAddress() + ":" + getPortId();
	}
	
	public String toString() {
		return getFullAddress();
	}
	
	/**
	 * @return Connector name (null if wildcarded or undefined)
	 */
	public String getConnector() {
		return connector;		
	}

	/**
	 * @return Connector name ("*" if wildcarded or undefined)
	 */	
	public String getConnectorName() {
		if (connector == null) {
			return "*";
		} else {
			return connector;
		}
	}

	/**
	 * @return Device name (null if wildcarded or undefined)
	 */
	public String getDevice() {
		return device;
	}

	/**
	 * @return Device name ("*" if wildcarded or undefined)
	 */	
	public String getDeviceAddress() {
		if (device == null) {
			return "*";
		} else {
			return device;
		}
	}

	/**
	 * @return Port id (null if wildcarded or undefined)
	 */
	public String getPort() {
		return port;
	}
	
	public String getPortId() {
		if (port == null) {
			return "*";
		} else {
			return port;
		}
	}
	
	/**
	 * 
	 * @return true if all parts are not null, so denote a specific port
	 */
	public boolean isFullyQualified() {
		return connector != null && device != null && port != null;
	}

	public boolean equals(Object arg0) {
		return compareTo(arg0) == 0;
	}
	
	public int compareTo(Object arg0) {
		if (arg0 == null) {
			throw(new IllegalArgumentException());
		}
		Address arg1;
		if (String.class.isInstance(arg0)) {
			arg1 = new Address((String)arg0);
		} else if (getClass().isInstance(arg0)) {
			arg1 = (Address) arg0; 			
		} else {
			throw(new ClassCastException("Invalid class: "+arg0.getClass().getCanonicalName()));			
		}
		int res = getConnector().compareTo(arg1.getConnector());
		if (res == 0) {
			res = getDevice().compareTo(arg1.getDevice());
			if (res == 0) {
				res = getPort().compareTo(arg1.getPort());				
			}
		}		
		return res;
	}
	
	/**
	 * Test if this address matches a supplied one.
	 * Two address matches if each part (connector, device, port) matches
	 * Parts matches if are identical or either or both are undefined.
	 * Examples of matching addresses:
	 * <ul>
	 *   <li>a.b:c , a.b:c</li>
	 *   <li>*.b:c , a.b:c</li>
	 *   <li>a.*:c , a.b:c</li>
	 *   <li>a.b:* , a.b:c</li>
	 *   <li>*.b:c , a.*:c</li>
	 *   <li>b:c , a.b:c</li>
	 * </ul>  
	 * @param address 
	 * @return true if matches
	 */
	public boolean matches(Address address) {
		return matchPort(address.getConnector(),address.getDevice(),address.getPort());
	}

	public boolean matches(String fullAddress) {
		return matches(new Address(fullAddress));
	}

	public boolean matchConnector(String connector) {
		return match(getConnector(),connector);
	}

	public boolean matchDevice(String connector, String device) {
		return matchConnector(connector) && match(getDevice(),device);
	}

	public boolean matchPort(String connector, String device, String port) {
		return matchDevice(connector,device) && match(getPort(),port);
	}

	private boolean match(String a, String b) {
		return ( a == null || b == null || a.equalsIgnoreCase(b) || a.equals("*") || b.equals("*")); 
	}

}
