package com.gingerdroids.utils_java;

/**
 * Abstract class for generating strings for an array of objects. 
 * Implementations should override {@link #getString(Object)}. 
 * <p>
 * All the work is done in the constructor. 
 * The result is immediately available in {@link #string}. 
 */
public abstract class StringMaker<Thing> { 
	
	/**
	 * The computed string. 
	 * This is available as soon as the constructor completes. 
	 */
	public final String string ;
	
	public StringMaker(Thing[] things, String separator) { 
		StringBuffer sb = new StringBuffer(); 
		String sep = null ; 
		for (Thing thing : things) { 
			if (sep!=null) sb.append(sep); 
			sb.append(getString(thing)); 
			if (sep==null) sep = separator ; 
		}
		this.string = sb.toString(); 
	}
	
	/**
	 * Maps from a single object to a string. 
	 * This will be called for each item in the constructor argument. 
	 */
	public abstract String getString(Thing thing); 
	
	/**
	 * Returns a string: these numbers, in numeric form, with the given separator. 
	 */
	public static String toStringWithSep(int[] intArray, String separator) {
		StringBuffer sb = new StringBuffer(); 
		String sep = null ; 
		for (int phraseNumber : intArray) { 
			if (sep!=null) sb.append(sep); 
			sb.append(phraseNumber); 
			if (sep==null) sep = separator ; 
		}
		return sb.toString();
	}

	/**
	 * Returns a string: these numbers, in numeric form, comma-separated. 
	 */
	public static String toStringCommaSep(int[] intArray) { 
		String sep = ", ";
		return toStringWithSep(intArray, sep); 
	}
	
}