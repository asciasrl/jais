package it.ascia.bentel;

import java.util.Vector;

import org.slf4j.LoggerFactory;

import it.ascia.bentel.msg.ReadRealtimeStatusMessage;
import it.ascia.bentel.msg.ReadStatusMessage;
import it.ascia.bentel.msg.ReadZonesDescriptionsMessage;

public abstract class BentelKyoUnit {

	protected String version;
	protected BentelKyoControllerModule module;

    protected org.slf4j.Logger logger;
    
	public BentelKyoUnit(String version) {
		logger = LoggerFactory.getLogger(getClass().getCanonicalName());
		this.version = version;
	}

	/**
	 * Max number of zones
	 * @return
	 */
	public abstract int maxZones();
	
	/**
	 * Max number of partitions (in italiano: Aree)
	 * @return
	 */
	public abstract int maxPartitions();

	/**
	 * Max number of outputs (in italiano: Uscite)
	 * @return
	 */
	public abstract int maxOutputs();

	public void updateZonesDescriptions(BentelKyoConnector connector) {
		logger.debug("Updating zones descriptions");
		ReadZonesDescriptionsMessage m;
		for (int j = 0; j < (maxZones() / 4); j++) {
			m = new ReadZonesDescriptionsMessage(j);
			connector.sendMessage(m);
			for (int i = 0; i <= 3; i++) {
				connector.getDevice("Zone"+(j * 4 + i+1)).getPort("Description").setValue(m.getDescription(i).trim());
			}
		}		
	}

	public void updatePartitionsDescriptions(BentelKyoConnector connector) {
		logger.debug("Updating parttions descriptions");
		// TODO
		ReadPartitionsDescriptionsMessage m;
		for (int j = 0; j < (maxPartitions() / 4); j++) {
			m = new ReadPartitionsDescriptionsMessage(j);
			connector.sendMessage(m);
			for (int i = 0; i <= 3; i++) {
				connector.getDevice("Partition"+(j * 4 + i+1)).getPort("Description").setValue(m.getDescription(i).trim());
			}
		}		
	}

	public void updateRealTime(BentelKyoConnector connector) {
		ReadRealtimeStatusMessage m = new ReadRealtimeStatusMessage();
		connector.sendMessage(m);
		updateRealTime(connector,m.getResponse());
	}

	protected abstract void updateRealTime(BentelKyoConnector connector, Vector<Integer> response);
	
	public void updateStatus(BentelKyoConnector connector) {
		ReadStatusMessage m = new ReadStatusMessage();
		connector.sendMessage(m);
		updateStatus(connector,m.getResponse());
	}

	protected abstract void updateStatus(BentelKyoConnector connector, Vector<Integer> response);


}
