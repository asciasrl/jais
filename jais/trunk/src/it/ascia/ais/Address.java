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
	
	private String connectorName;
	private String deviceAddress;
	private String portId;
	
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
		setConnectorName(connector);
		setDeviceAddress(device);
		setPortId(port);
	}

	public Address(ConnectorInterface connector, Device device, DevicePort port) {
		if (connector != null) {
			setConnectorName(connector.getConnectorName());
		}
		if (device != null) {
			setDeviceAddress(device.getDeviceAddress());
		}
		if (port != null) {
			setPortId(port.getPortId());
		}
	}

	/**
	 * Set connector part of the address 
	 * @param connector Name of connector
	 * @throws IllegalArgumentException if connector is already set or contains illegal chars
	 */
	public void setConnectorName(String connector) {
		if (this.connectorName != null) {
			throw(new IllegalArgumentException("Connector already set"));
		}
		if (connector != null) {
			if (connector.contains(".")) {
				throw(new IllegalArgumentException("Connector name can't contians dots (.)"));
			}
			if (connector.contains(":")) {
				throw(new IllegalArgumentException("Connector name can't contians colons (:)"));
			}
			if (connector.equals("*")) {
				connector = null;
			}
		}
		this.connectorName = connector;
	}
	
	/**
	 * Set device part of the address 
	 * @param device Address of device
	 * @throws IllegalArgumentException if device is already set or contains illegal chars
	 */
	public void setDeviceAddress(String device) {
		if (this.deviceAddress != null) {
			throw(new IllegalArgumentException("Device already set"));
		}
		if (device != null) {
			if (device.contains(":")) {
				throw(new IllegalArgumentException("Device name can't contians colons (:)"));
			}
			if (device.equals("*")) {
				device = null;
			}
		}
		this.deviceAddress = device;		
	}
	
	/**
	 * Set port part of the address 
	 * @param port Id of the port
	 * @throws IllegalArgumentException if port is already set or contains illegal chars
	 */
	public void setPortId(String port) {
		if (this.portId != null) {
			throw(new IllegalArgumentException("Port already set"));
		}
		if (port != null && port.equals("*")) {
			port = null;
		}
		this.portId = port;		
	}
	
	/**
	 * Address must have the form
	 *   connectorName.deviceAddress:portId
	 *   
	 *   connectorName cannot contains "." or ":"
	 *   deviceAddress cannot contains ":" but can contain "."
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
			connectorName = fullAddress.substring(0, iDot);
			if (connectorName.equals("") || connectorName.equals("*")) {
				connectorName = null;
			}
		}
		if (iColon >= 0) {
			portId = fullAddress.substring(iColon+1);
			if (portId.equals("") || portId.equals("*")) {
				portId = null;
			}
		} else {
			iColon = fullAddress.length();
		}
		if (iColon > iDot) {
			deviceAddress = fullAddress.substring(iDot + 1, iColon);
			if (deviceAddress.equals("") || deviceAddress.equals("*")) {
				deviceAddress = null;
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

	/**
	 * Return full address
	 */
	public String toString() {
		return getFullAddress();
	}
	
	/**
	 * @return Connector name ("*" if wildcarded or undefined)
	 */	
	public String getConnectorName() {
		if (connectorName == null) {
			return "*";
		} else {
			return connectorName;
		}
	}

	/**
	 * @return Device name ("*" if wildcarded or undefined)
	 */	
	public String getDeviceAddress() {
		if (deviceAddress == null) {
			return "*";
		} else {
			return deviceAddress;
		}
	}

	/**
	 * @return Port id ("*" if wildcarded or undefined)
	 */	
	public String getPortId() {
		if (portId == null) {
			return "*";
		} else {
			return portId;
		}
	}
	
	/**
	 * 
	 * @return true if all parts are not null, so denote a specific port
	 */
	public boolean isFullyQualified() {
		return connectorName != null && deviceAddress != null && portId != null;
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
		int res = connectorName.compareTo(arg1.getConnectorName());
		if (res == 0) {
			res = deviceAddress.compareTo(arg1.getDeviceAddress());
			if (res == 0) {
				res = portId.compareTo(arg1.getPortId());				
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
		return matchPort(address.getConnectorName(),address.getDeviceAddress(),address.getPortId());
	}

	public boolean matches(String fullAddress) {
		return matches(new Address(fullAddress));
	}

	public boolean matchConnector(String connector) {
		return match(getConnectorName(),connector);
	}

	public boolean matchDevice(String connector, String device) {
		return matchConnector(connector) && match(getDeviceAddress(),device);
	}

	public boolean matchPort(String connector, String device, String port) {
		return matchDevice(connector,device) && match(getPortId(),port);
	}

	private boolean match(String a, String b) {
		return ( a == null || b == null || a.equalsIgnoreCase(b) || a.equals("*") || b.equals("*")); 
	}

}
