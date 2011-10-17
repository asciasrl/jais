package it.ascia.sequencer;

import java.lang.Thread.State;
import java.util.List;

import it.ascia.ais.AISException;
import it.ascia.ais.Address;
import it.ascia.ais.Controller;
import it.ascia.ais.Device;
import it.ascia.ais.DevicePort;
import it.ascia.ais.port.IntegerPort;
import it.ascia.ais.port.ScenePort;

public class SequenceDevice extends Device {
	
	Thread sequenceRunner = null;
	
	static private String StepsPortId = "steps"; 
	static private String StepPortId = "step"; 
	static private String StartPortId = "start"; 
	static private String StopPortId = "stop"; 

	public SequenceDevice(String address) throws AISException {
		super(address);
		this.addPort(new StepsPort(StepsPortId));			
		this.addPort(new IntegerPort(StepPortId));
		this.addPort(new ScenePort(StartPortId));
		this.addPort(new ScenePort(StopPortId));
	}

	public void setSteps(List<String> sequenceSteps) {
		setPortValue(StepsPortId, sequenceSteps);		
	}

	@Override
	public boolean sendPortValue(String portId, Object newValue)
			throws AISException {
		if (portId.equals(StartPortId)) {
			if (sequenceRunner == null || sequenceRunner.getState() == State.TERMINATED) {
				logger.info("Starting sequence");
				sequenceRunner = new SequenceRunner(this);
				sequenceRunner.start();
				return true;
			} else if (sequenceRunner.isAlive()) {
				logger.warn("Sequence is already running");
				return false;
			} else {
				logger.warn("Sequence state is: "+sequenceRunner.getState());
				return false;				
			}
		}		
		if (portId.equals(StopPortId)) {
			if (sequenceRunner != null && sequenceRunner.isAlive()) {
				logger.info("Interrupting sequence");
				sequenceRunner.interrupt();
		    	try {
		    		sequenceRunner.join();
				} catch (InterruptedException e) {
					logger.error("Interrupted:",e);
				}
				sequenceRunner = null;
				return true;
			} else {
				logger.warn("Sequence is already interrupted");
				return false;
			}				
		}
		return false;
	}

	@Override
	public long updatePort(String portId) throws AISException {
		return 0;
	}

	private class SequenceRunner extends Thread {
		
		private SequenceDevice device;
		
		public SequenceRunner(SequenceDevice device) {
			setName(device.getSimpleAddress());
			this.device = device;
		}
		
		public void run() {
			List<String> steps = (List<String>)device.getPortValue(StepsPortId);
			logger.info("Staring sequence");
			for (int i = 0; i < steps.size(); i++) {
				device.setPortValue(StepPortId, new Integer(i));
				String step = steps.get(i);
				String[] stepParts = step.split("=",2);
				String stepAction = stepParts[0];
				String stepValue = null;
				if (stepParts.length == 2) {
					stepValue = stepParts[1];
				}
				logger.debug("Esecuzione Step: "+i + " Action: "+stepAction+" Value: "+stepValue);
				if (stepAction.equalsIgnoreCase("wait")) {
					try {
						Thread.sleep((new Integer(stepValue)).longValue());
					} catch (NumberFormatException e) {
						logger.error("Step "+i+" incorrect wait value: "+stepValue);
					} catch (InterruptedException e) {
						logger.debug("Sequence interrupted");
						break;
					}
				} else if (stepAction.equalsIgnoreCase("restart")) {
					i = -1;
				} else {
					Address address = new Address(stepAction);
					if (address.isFullyQualified()) {
						DevicePort p = Controller.getController().getDevicePort(address);
						if (p != null) {
							try {
								p.writeValue(stepValue);								
							} catch (Exception e) {
								logger.error("Sequence terminated because of error: ",e);
								break;								
							}
						} else {
							logger.error("Step "+i+" port not found: "+address);
						}
					} else {
						logger.error("Step "+i+" address not valid: "+stepAction);						
					}
				}
			}
			device.setPortValue(StepPortId, null);
			logger.info("Sequence completed.");
		}
	}

}
