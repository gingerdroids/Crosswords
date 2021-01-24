package com.gingerdroids.utils_java;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Takes a stream of strings, and counts how often each one occurs in the stream. 
 */
public class StringCounter { 
	
	/*
	 * The implementation seems to be oriented to efficiency for very long streams. 
	 */
	
	private final HashMap<String,Integer> indicesByString = new HashMap<String, Integer>() ; 
	
	private int[] counts = new int[256] ; 
	
	/**
	 * Add next string in stream. 
	 */
	public void add(String str) { 
		int index = getIndex(str); 
		counts[index]++ ; 
	}

	private int getIndex(String str) { 
		if (!indicesByString.containsKey(str)) { 
			int newIndex = indicesByString.size(); 
			indicesByString.put(str, newIndex); 
			if (newIndex>=counts.length) { 
				extendCountsArray(); 
			}
			counts[newIndex] = 0 ; 				
		}
		return indicesByString.get(str); 
	}

	private void extendCountsArray() { 
		int oldLength = counts.length;
		int [] oldArray = counts ; 
		int [] newArray = new int[oldLength*2] ; 
		System.arraycopy(oldArray, 0, newArray, 0, oldLength); 
		this.counts = newArray ; 
	}
	
	/**
	 * How many times has the given string occurred so far? 
	 */
	public int getCount(String str) { 
		if (!indicesByString.containsKey(str)) return 0 ; 
		int index = indicesByString.get(str);
		return counts[index] ;  
	}
	
	/**
	 * Returns all the strings added so far. 
	 */
	public Set<String> getStringSet() { 
		return new HashSet<String>(indicesByString.keySet()); 
	}

	/**
	 * Returns all the strings added so far, in no particular order. 
	 */
	public String [] getStrings() { 
		return Str.toArray(getStringSet()); 
		/* Implementation pre 22oct20. 
		String [] array = new String[indicesByString.size()] ; 
		indicesByString.keySet().toArray(array); 
		return array ; 
		 */
	}

}

