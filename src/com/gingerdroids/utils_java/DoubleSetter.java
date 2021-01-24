package com.gingerdroids.utils_java;

/**
 * Provides write-access to an <code>double</code>. 
 * Useful when setting the value requires side-effects, such as saving settings to persistent store. 
 */
public interface DoubleSetter { 
	public void setDouble(double number); 
}