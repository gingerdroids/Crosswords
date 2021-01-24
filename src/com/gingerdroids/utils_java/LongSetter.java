package com.gingerdroids.utils_java;

/**
 * Provides write-access to an <code>long</code>. 
 * Useful when setting the value requires side-effects, such as saving settings to persistent store. 
 */
public interface LongSetter { 
	public void setLong(long number); 
}