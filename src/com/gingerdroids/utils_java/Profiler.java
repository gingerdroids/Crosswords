package com.gingerdroids.utils_java;

/**
 * Rough As Guts profiler, to measure what fraction of time the execution spends in a section of code. 
 * <p>
 * Usage: Create a static instance somewhere. 
 * Call {@link #enterWhole()} and {@link #leaveWhole()} once each, at the beginning and end of the program. 
 * Call {@link #enterModule()} and {@link #leaveModule()} when you enter and leave the section to be measured. 
 * Call {@link #println()} after the call to {@link #leaveWhole()}. 
 * Run the program on typical data, and read the result on the console. 
 * <p>
 * Uses the {@link Timer} class (two instances). Note that this is not thread-safe.  
 */
public class Profiler { 
	
	public final Timer wholeTimer = new Timer(); 
	
	public final Timer moduleTimer = new Timer(); 

	public Profiler() {}

	public void enterWhole() { 
		wholeTimer.startTicking(); 
	}
	
	public void leaveWhole() { 
		wholeTimer.stopTicking(); 
	}
	
	public void enterModule() { 
		moduleTimer.startTicking(); 
	}
	
	public void leaveModule() { 
		moduleTimer.stopTicking(); 
	}
	
	public void println() { 
		String moduleTime = Str.timeIntervalOneless(moduleTimer.millisTicked());
		String wholeTime = Str.timeIntervalOneless(wholeTimer.millisTicked());
		System.out.println(getResultString("Time in module is ", "%. ("+(moduleTime+" /"+wholeTime)+")")); 
	}
	
	/**
	 * Returns a string, with the percent of time in the module between the given args. 
	 */
	public String getResultString(String prefix, String suffix) { 
		return prefix + getPercent() + suffix ; 
	}
	
	/**
	 * Returns the percent of time in the module. 
	 * It's an integer, so the precision is limited. 
	 * For very small fractions, you probably want to call {@link #getFraction()}. 
	 */
	public int getPercent() { 
		double frac = getFraction(); 
		int percent = (int) Math.round(frac*100); 
		return percent ; 
	}

	/**
	 * Returns the fraction of time in the module. 
	 */
	public double getFraction() {
		return moduleTimer.millisTicked() / (double)wholeTimer.millisTicked();
	}

}
