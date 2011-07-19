package it.ascia.sequencer;

import org.apache.commons.collections.map.StaticBucketMap;

import com.sun.org.apache.xalan.internal.xsltc.util.IntegerArray;

import it.ascia.ais.AISException;
import it.ascia.ais.Address;
import it.ascia.ais.Controller;
import it.ascia.ais.Device;
import it.ascia.ais.DevicePort;
import it.ascia.ais.port.IntegerPort;
import it.ascia.ais.port.ScenePort;

public class SequenceDevice extends Device {
	
	Thread sequenceRunner;
	
	static private String SequencePortId = "start"; 
	static private String StepPortId = "step"; 
	static private String StartPortId = "start"; 

	public SequenceDevice(String address) throws AISException {
		super(address);
		this.addPort(new SequencePort(SequencePortId));			
		this.addPort(new IntegerPort(StepPortId));
		this.addPort(new ScenePort(StartPortId));
		sequenceRunner = new SequenceRunner(this);
	}

	@Override
	public boolean sendPortValue(String portId, Object newValue)
			throws AISException {
		if (portId.equals(StartPortId)) {
			if ((Boolean) newValue) {
				if (sequenceRunner.isAlive()) {
					logger.info("Sequence is already running");
					return false;
				} else {
					sequenceRunner.start();
					return true;
				}
			} else {
				if (sequenceRunner.isAlive()) {
					sequenceRunner.interrupt();
					return true;
				} else {
					logger.info("Sequence is already interrupted");
					return false;
				}				
			}
		}
		return false;
	}

	@Override
	public long updatePort(String portId) throws AISException {
		// TODO Auto-generated method stub
		return 0;
	}

	private class SequenceRunner extends Thread {
		
		private SequenceDevice device;
		
		public SequenceRunner(SequenceDevice device) {
			setName(device.getSimpleAddress());
			this.device = device;
		}
		
		public void run() {
			String[] steps = ((String)device.getPortValue(SequencePortId)).split(";");
			for (int i = 0; i < steps.length; i++) {
				device.setPortValue(StepPortId, new Integer(i));
				String step = steps[i];
				String[] stepParts = step.split("=",2);
				String stepAction = stepParts[0];
				String stepValue = stepParts[1];
				logger.debug("action: "+stepAction+" value: "+stepValue);
				if (stepAction.equalsIgnoreCase("wait")) {
					try {
						Thread.sleep((new Integer(stepValue)).longValue());
					} catch (NumberFormatException e) {
						logger.error("Step "+i+" incorrect wait value: "+stepValue);
					} catch (InterruptedException e) {
						logger.debug("interrupted");
					}
				} else if (stepAction.equalsIgnoreCase("restart")) {
					i = 0;
					continue;
				} else {
					Address address = new Address(stepAction);
					if (address.isFullyQualified()) {
						DevicePort p = Controller.getController().getDevicePort(address);
						if (p != null) {
							p.setValue(stepValue);
						} else {
							logger.error("Step "+i+" port not found: "+address);
						}
					} else {
						logger.error("Step "+i+" address not valid: "+stepAction);						
					}
				}
			}
		}
	}

}
