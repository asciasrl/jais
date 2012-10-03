package it.ascia.sequencer;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.configuration.HierarchicalConfiguration;

import it.ascia.ais.ControllerModule;
import it.ascia.ais.Transport;

public class SequencerControllerModule extends ControllerModule {

	public void start() {
		super.start();
		SequenceConnector conn = new SequenceConnector("sequencer");
 		conn.setModule(this);
		controller.addConnector(conn);
		/*
		for (int i = 0; i < 256; i++) {
			conn.addDevice(new SequenceDevice("sequence"+i));
		}
		*/
		List<HierarchicalConfiguration> sequences = getConfiguration().configurationsAt("sequence");
		for (Iterator<HierarchicalConfiguration> sequenceIterator = sequences.iterator(); sequenceIterator.hasNext();)
		{			
		    HierarchicalConfiguration sequenceConfiguration = sequenceIterator.next();
		    String sequenceId = (String) sequenceConfiguration.getString("[@id]");
			List<String> sequenceSteps = sequenceConfiguration.getList("step");
			SequenceDevice sequence = new SequenceDevice("sequence"+sequenceId);
			sequence.setSteps(sequenceSteps);
			conn.addDevice(sequence);
			
		}				

	}

}
