package com.gingerdroids.utils_java;

/**
 * Accumulates wall-clock time between tick-starting and tick-stopping, over several sessions. 
 * Is not particularly smart - in particular, is completely naive regarding concurrent threads. 
 */
public class Timer { 
	
	private boolean isTicking = false ; 
	
	private long whenTickingStarted ; 
	
	private long millisTicked = 0 ; 
	
	public Timer startTicking() { 
		this.isTicking = true ; 
		this.whenTickingStarted = Util.milliTime(); 
		return this ; 
	}
	
	public Timer stopTicking() { 
		if (!isTicking) return this ; 
		long whenTickingStopped = Util.milliTime(); 
		this.isTicking = false ; 
		this.millisTicked += whenTickingStopped - whenTickingStarted ; 
		return this ; 
	}
	
	public long millisTicked() { 
		long currentSession ; 
		if (isTicking) { 
			currentSession = Util.milliTime() - whenTickingStarted ; 
		} else { 
			currentSession = 0 ; 
		}
		return millisTicked + currentSession ; 
	}

}
