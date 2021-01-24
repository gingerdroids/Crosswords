package com.gingerdroids.utils_java;

/**
 * Maintains a circular buffer of <code>char</code>. 
 * <p>
 * Useful for list or stream processing classes where error messages, log messages, etc need some context. 
 */
public class CircularCharBuffer { 
	
	private final char[] buffer; 
	private final int bufferSize ; 
	boolean isFull = false ; 
	int nextIndex = 0 ; 

	public CircularCharBuffer(int bufferSize) { 
		this.buffer = new char[bufferSize] ; 
		this.bufferSize = bufferSize ; 
	}

	public void write(char[] inChars, final int inStart, final int inCount) { 
		if (nextIndex+inCount<=bufferSize) { 
			System.arraycopy(inChars, inStart, buffer, nextIndex, inCount); 
			nextIndex += inCount ; 
		} else { 
			int topSize = bufferSize - nextIndex ; 
			int inWrappedCount = inCount - topSize ; 
			try { 
				System.arraycopy(inChars, inStart, buffer, nextIndex, topSize); 
				System.arraycopy(inChars, inStart+topSize, buffer, 0, inWrappedCount); 
			} catch (Exception e) { 
				System.out.println("Blah"); 
			}
			nextIndex = inWrappedCount ; 
			isFull = true ; 
		}
	}
	
	public char [] getRecent(int requestLength) { 
		if (isFull) { 
			int actualCount = Math.min(requestLength, bufferSize); 
			char [] chars = new char[actualCount] ; 
			int topSectionLength = bufferSize - nextIndex ; 
			if (topSectionLength<actualCount) { 
				System.arraycopy(buffer, nextIndex, chars, 0, topSectionLength); 
				System.arraycopy(buffer, 0, chars, topSectionLength, actualCount-topSectionLength); 
			} else { 
				System.arraycopy(buffer, nextIndex, chars, 0, actualCount); 
			}
			return chars ; 
		} else { 
			int actualCount = Math.min(requestLength, nextIndex); 
			char [] chars = new char[actualCount] ; 
			System.arraycopy(buffer, 0, chars, 0, actualCount); 
			return chars ; 
		}
	}
	
	public char [] getRecent() { 
		return getRecent(bufferSize); 
	}
}