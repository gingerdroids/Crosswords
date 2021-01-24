package com.gingerdroids.utils_java;

/**
 * Provides write-access to an <code>int</code>. 
 * Useful when setting the value requires side-effects, such as saving settings to persistent store. 
 */
public interface IntSetter { 
	public void setInt(int number); 
}