package com.gingerdroids.utils_java;

/**
 * Provides write-access to a <code>boolean</code>. 
 * Useful when setting the value requires side-effects, such as saving settings to persistent store. 
 */
public interface BooleanSetter { 
	public void setBoolean(boolean isTrue); 
}