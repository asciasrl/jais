package it.ascia.bentel;

import java.util.Vector;

import it.ascia.ais.AISException;

public class BentelKyo8 extends BentelKyoUnit {

	public BentelKyo8(String version) {
		super(version);
		if (version.equals("2.10")) {
		} else {
			throw(new AISException("Firmware version not recognized: " +version));
		}
	}
	
	public int maxZones() {
		return 8;
	}
	
	public int maxPartitions() {
		return 4;
	}

	@Override
	public int maxOutputs() {
		return 5;
	}

	@Override
	protected void updateRealTime(BentelKyoConnector connector, Vector<Integer> response) {
		int zoneAlarm = response.get(0).intValue();
		for (int i = 0; i < maxZones(); i++) {
			boolean bit = ((zoneAlarm >> i) & 0x01) == 1; 
			connector.getDevice("Zone"+(i+1)).getPort("Alarm").setValue(new Boolean(bit));
		}		
		int zoneTamper = response.get(1).intValue();
		for (int i = 0; i < maxZones(); i++) {
			boolean bit = ((zoneTamper >> i) & 0x01) == 1; 
			connector.getDevice("Zone"+(i+1)).getPort("Tamper").setValue(new Boolean(bit));
		}		
		int partitionAlarm = response.get(3).intValue();
		for (int i = 0; i < maxPartitions(); i++) {
			boolean bit = ((partitionAlarm >> i) & 0x01) == 1; 
			connector.getDevice("Partition"+(i+1)).getPort("Alarm").setValue(new Boolean(bit));
		}		
	}

	@Override
	protected void updateStatus(BentelKyoConnector connector, Vector<Integer> response) {
		int partitionAway = response.get(0).intValue();
		for (int i = 0; i < maxPartitions(); i++) {
			boolean bit = ((partitionAway >> i) & 0x01) == 1; 
			connector.getDevice("Partition"+(i+1)).getPort("Away").setValue(new Boolean(bit));
		}				
		int partitionStay = response.get(1).intValue();
		for (int i = 0; i < maxPartitions(); i++) {
			boolean bit = ((partitionStay >> i) & 0x01) == 1; 
			connector.getDevice("Partition"+(i+1)).getPort("Stay").setValue(new Boolean(bit));
		}				
		int partitionStay0 = response.get(2).intValue();
		for (int i = 0; i < maxPartitions(); i++) {
			boolean bit = ((partitionStay0 >> i) & 0x01) == 1; 
			connector.getDevice("Partition"+(i+1)).getPort("Stay0").setValue(new Boolean(bit));
		}				
		int partitionDisarmed = response.get(3).intValue();
		for (int i = 0; i < maxPartitions(); i++) {
			boolean bit = ((partitionDisarmed >> i) & 0x01) == 1; 
			connector.getDevice("Partition"+(i+1)).getPort("Disarmed").setValue(new Boolean(bit));
		}
		
		int outputs = response.get(4).intValue();
		for (int i = 0; i < maxOutputs(); i++) {
			boolean bit = ((outputs >> i) & 0x01) == 1; 
			connector.getDevice("Outputs").getPort("Out"+(i+1)).setValue(new Boolean(bit));
		}
		
		int zoneBypassed = response.get(5).intValue();
		for (int i = 0; i < maxZones(); i++) {
			boolean bit = ((zoneBypassed >> i) & 0x01) == 1; 
			connector.getDevice("Zone"+(i+1)).getPort("Bypassed").setValue(new Boolean(bit));
		}				
		int zoneAlarmMemory = response.get(6).intValue();
		for (int i = 0; i < maxZones(); i++) {
			boolean bit = ((zoneAlarmMemory >> i) & 0x01) == 1; 
			connector.getDevice("Zone"+(i+1)).getPort("AlarmMemory").setValue(new Boolean(bit));
		}				
		int zoneTamperMemory = response.get(7).intValue();
		for (int i = 0; i < maxZones(); i++) {
			boolean bit = ((zoneTamperMemory >> i) & 0x01) == 1; 
			connector.getDevice("Zone"+(i+1)).getPort("TamperMemory").setValue(new Boolean(bit));
		}				
	}

}
