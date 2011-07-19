package it.ascia.sequencer;

import it.ascia.ais.ControllerModule;

public class SequencerControllerModule extends ControllerModule {

	public void start() {
		super.start();
		SequenceConnector conn = new SequenceConnector("sequencer");
 		//conn.setModule(this);
		controller.addConnector(conn);
		for (int i = 0; i < 256; i++) {
			conn.addDevice(new SequenceDevice("sequence"+i));
		}
	}

}
