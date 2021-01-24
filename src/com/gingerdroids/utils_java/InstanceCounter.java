package com.gingerdroids.utils_java;

import java.util.WeakHashMap;

/**
 * Rough-as-guts tool to see if class's instances are garbage-collected, or just accumulate. 
 * <p>
 * Usage: declare and instantiate an instance of this in a field in the questionable class. 
 * It will automagically report growing and shrinking numbers of instances. 
 */
public class InstanceCounter {
	
	public static final WeakHashMap<InstanceCounter, Class> instances = new WeakHashMap<InstanceCounter, Class>(); 
	
	private static final int ratio = 4 ; 
	
	public static int lastReportedCount = 0 ; 
	
	public static int aggregate = 0 ; 
	
	public static int lastReportedAggregate = 0 ; 
	
	public static long lastReportedNanos = System.nanoTime(); 
	
	public InstanceCounter() { 
		instances.put(this, InstanceCounter.class); 
		aggregate ++ ; 
		int currentCount = instances.size(); 
		if (lastReportedCount==0) { 
			reportIncrease(currentCount); 
		} else if (currentCount>=ratio*lastReportedCount) { 
			reportIncrease(currentCount); 
		} else if (currentCount<=lastReportedCount/ratio) { 
			reportDecrease(currentCount); 
		}
	}

	private void reportIncrease(int currentCount) {
		long freeMemory = Runtime.getRuntime().freeMemory() >> 20 ;
		long currentNanos = System.nanoTime(); 
		long rate = (currentNanos-lastReportedNanos) / (aggregate-lastReportedAggregate); 
		String message = 
				"Instance count has increased to " + currentCount +
				"\t\tFree memory (MB) " + freeMemory +
				"\t\tnanosec/creation " + rate ;
		System.err.println(message); 
		lastReportedCount = currentCount ; 
		lastReportedAggregate = aggregate ; 
		lastReportedNanos = currentNanos ; 
	}
	
	private void reportDecrease(int currentCount) {
		System.err.println("Instance count is "+currentCount); 
		lastReportedCount = currentCount ; 
	}

}
