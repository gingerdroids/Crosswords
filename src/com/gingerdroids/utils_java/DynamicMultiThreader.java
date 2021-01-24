package com.gingerdroids.utils_java;

/**
 * Runs subtasks in separate threads. Client code must create a subclass, implementing {@link #processItem(int)}. 
 * <p>
 * The entire task must be divided into a known number of subtasks, and each of these numbered. 
 * <p>
 * The {@linkplain #processItem(int)} method should not throw an exception. 
 * That subtask will never be recorded as finished, and {@link #waitUntilQuiet()} will not return. 
 * (Functionality as at October 2020.) 
 * <p>
 * The number of threads created is limited, 
 * either one for each processor available (@link {@link #DynamicMultiThreader(int)}) 
 * or a given number by the client code (@link {@link #DynamicMultiThreader(int, int)}). 
 * Each thread may call {@link #processItem(int)} several times. 
 * <p>
 * This class looks after the thread-safety of its own data, 
 * but the client is responsible for the thread-safety of its own data in {@link #processItem(int)}. 
 * <p>
 * Typical usage:<ul>
 * <li> Instantiate subclass. 
 * <li> Call {@link #startAll()}. 
 * <li> [optional] Other work the client wants to do in the meantime.
 * <li> Call {@link #waitUntilQuiet()}. 
 * <li> <i>All subtasks should be completed.</i> Client carries on. 
 * </ul>
 * <p>
 * The calls to {@link #processItem(int)} will probably be in order, but this isn't guaranteed. 
 */
public abstract class DynamicMultiThreader { 
	
	private final Object lock = new Object(); 
	
	private int finishedCount = 0 ; 
	
	private int nextItemNumber = 0 ; 
	
	public final int itemCount ; 
	
	private final DynamicItemThread [] threads ; 
	
	public DynamicMultiThreader(int itemCount, int threadCount) { 
		this.itemCount = itemCount ; 
		this.threads = new DynamicItemThread[itemCount] ; 
		for (int i=0 ; i<itemCount ; i++) threads[i] = new DynamicItemThread(); 
	}

	public DynamicMultiThreader(int itemCount) { 
		this(itemCount, Runtime.getRuntime().availableProcessors()); 
	}
	
	protected abstract void processItem(int itemNumber); 
	
	public DynamicMultiThreader startAll() { 
		for (DynamicItemThread thread : threads) thread.start(); 
		return this ; 
	}

	public DynamicMultiThreader waitUntilQuiet() { 
		synchronized (lock) {
			while (finishedCount<itemCount) { 
				try {
					lock.wait(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				} 
			}
		}
		return this ; 
	}
	
	private class DynamicItemThread extends Thread { 
		
		@Override
		public void run() {
			while(true) { 
				int itemNumber ; 
				synchronized (lock) {
					if (nextItemNumber>=itemCount) break ; 
					itemNumber = nextItemNumber ++ ; 
				}
				processItem(itemNumber); 
				synchronized (lock) {
					finishedCount ++ ; 
					if (finishedCount>=itemCount) { 
						lock.notifyAll(); 
						break ; 
					}
				}
			}
		}
	}
}