package it.ascia.bentel;

import java.util.Vector;

@SuppressWarnings("serial")
public class BufferVector<T> extends Vector<Integer> {
	
	public void shift(int howMany) {
		synchronized (this) {			
			removeRange(0,howMany-1);
		}
	}
	
}
