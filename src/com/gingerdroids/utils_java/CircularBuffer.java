package com.gingerdroids.utils_java;

import java.util.ArrayList;

/**
 * Maintains a circular buffer of whatever objects are written to it. 
 * Old objects are dropped off the end. 
 * <p>
 * Useful for list or stream processing classes where error messages, log messages, etc need some context. 
 */
public class CircularBuffer<Type> { 
	
	private final ArrayList<Type> buffer; 
	private final int bufferSize ; 
	int nextIndex = 0 ; 

	public CircularBuffer(int bufferSize) { 
		this.buffer = new ArrayList<Type>(bufferSize); 
		this.bufferSize = bufferSize ; 
	}

	public void write(Type item) { 
		if (buffer.size()==bufferSize) { 
			buffer.set(nextIndex, item); 
		} else { 
			buffer.add(item); 
		}
		nextIndex ++ ; 
		if (nextIndex>=bufferSize) nextIndex = 0 ; 
	}
	
	public ArrayList<Type> getRecent(int requestLength) { 
		if (buffer.size()==bufferSize) { 
			int actualCount = Math.min(requestLength, bufferSize); 
			ArrayList<Type> result = new ArrayList<Type>(actualCount); 
			int topSectionLength = bufferSize - nextIndex ; 
			if (topSectionLength<actualCount) { 
				result.addAll(buffer.subList(nextIndex, bufferSize)); 
				result.addAll(buffer.subList(0, actualCount-topSectionLength)); 
			} else { 
				result.addAll(buffer.subList(nextIndex, nextIndex+actualCount)); 
			}
			return result ; 
		} else { 
			int actualCount = Math.min(requestLength, nextIndex); 
			ArrayList<Type> result = new ArrayList<Type>(actualCount); 
			result.addAll(buffer.subList(0, actualCount)); 
			return result ; 
		}
	}
	
	public ArrayList<Type> getRecent() { 
		return getRecent(bufferSize); 
	}
	
	public Type getMostRecent() { 
		if (buffer.isEmpty()) { 
			return null ; 
		} else if (nextIndex==0) { 
			return buffer.get(bufferSize-1); 
		} else { 
			return buffer.get(nextIndex-1); 
		}
	}
}