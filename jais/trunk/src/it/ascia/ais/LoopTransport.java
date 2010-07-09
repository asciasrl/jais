package it.ascia.ais;

import java.util.concurrent.LinkedBlockingDeque;

public class LoopTransport extends Transport {
	
	private Thread receiverThread;
	private LinkedBlockingDeque<Byte> receiverQueue;

	public LoopTransport() {
		super();
		receiverQueue = new LinkedBlockingDeque<Byte>();
		receiverThread = new Thread(new receiver());
		receiverThread.start();
	}
	
	@Override
	public void close() {
		receiverThread.interrupt();
    	try {
    		receiverThread.join();
		} catch (InterruptedException e) {
			logger.error("Interrupted:",e);
		}
	}

	@Override
	public String getInfo() {
		// TODO Auto-generated method stub
		return "Software Loop";
	}

	@Override
	public int getSpeed() {
		return 9600;
	}

	@Override
	public void write(byte[] b) {
	for (int i = 0; i < b.length; i++) {
			receiverQueue.offer(b[i]);
		}
	}

	private class receiver implements Runnable {

		public void run() {
			
			boolean nameMustBeSet = true;
			
			while(true) {
				try {
					int b = receiverQueue.take();
					if (connector != null) {
						if (nameMustBeSet) {
							receiverThread.setName(connector.getName() + "-receiver");
							nameMustBeSet = false;
						}
						connector.received(b);
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block					
				}
			}			
		}
		
	}
}
