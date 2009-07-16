package it.ascia.aui.tests;

import it.ascia.ais.Controller;
import junit.extensions.TestSetup;
import junit.framework.Test;

public class AuiTestSetup extends TestSetup {

	public AuiTestSetup(Test test) {
		super(test);
	}

	public void setUp() {
		Controller c = Controller.getController();
		c.configure();
		c.start();
		// wait startup
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}		
	}

	public void tearDown() {
		Controller c = Controller.getController();
		c.stop();
	}

}
