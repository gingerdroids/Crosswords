package com.gingerdroids.utils_java;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Formatter;
import java.util.List;

import com.gingerdroids.utils_java.SortedList.ClassTest;


/**
 * Utility to return strings for various objects, arrays, thread, etc. 
 * Usually, but not always, these are for human consumption.  
 * <p>
 * Some of the methods handle a null argument. 
 * <p>
 * Generally, it is not possible to reconstruct the object from the string. 
 * <p>
 * To some extent, this is just {@link Object#toString} on steroids. 
 */
public class Str { 
	
	public static final String [] emptyArray = Util.emptyStringArray ; 
	
	/**
	 * Converts a space-separated string to an <code>ArrayList<String></code>. 
	 * Returns null for a null arg. 
	 */
	public static ArrayList<String> convertSpacedStringToArrayList(String spacedString) { 
		if (spacedString==null) return null ; 
		ArrayList<String> list = new ArrayList<String>(); 
		if (spacedString!=null) { 
			String [] array = spacedString.split(" "); 
			list.addAll(Arrays.asList(array)); 
		}
		return list ; 
	}
	
	/**
	 * Appends the stack-trace to the string-builder. 
	 * A compact single-line version of the stack is appended - most classes are omitted, including android library classes. 
	 * <p>
	 * @param skipCount The number of items on the stack to be skipped - mostly calls within this class. 
	 */
	protected static void build(StringBuilder sb, StackTraceElement[] stackTrace, int skipCount) {
		String prevClassFullName = null ; // Prev-iteration value of 'classFullName' 
		boolean wasInAppCode = true ; // Prev-iteration value of 'isInAppCode' 
		for (int i=skipCount ; i<stackTrace.length ; i++) {
			StackTraceElement stackItem = stackTrace[i];
			String classFullName = stackItem.getClassName();  
			boolean isInAppCode = true ; 
			if (classFullName.startsWith("java.")) isInAppCode = false ; 
			else if (classFullName.startsWith("javax.")) isInAppCode = false ; 
			else if (classFullName.startsWith("com.android.")) isInAppCode = false ; 
			else if (classFullName.startsWith("android.")) isInAppCode = false ; 
			else if (classFullName.startsWith("dalvik.")) isInAppCode = false ; 
			else if (classFullName.startsWith("org.GNOME.")) isInAppCode = false ; 
			if (isInAppCode) { 
				if (classFullName.equals(prevClassFullName)) { 
					sb.append(","); 
				} else { 
					if (prevClassFullName!=null) sb.append(" "); 
					String[] parts = classFullName.split("\\."); // 'split()' takes a regular-expression argument. 
					try { 
						sb.append(parts[parts.length-1]); 
					} catch (Exception e) {
						sb.append("*"+classFullName); 
					}
					sb.append(":"); 
				}
				sb.append(stackItem.getLineNumber()); 
			} else { 
				if (wasInAppCode) sb.append(" *"); 
			}
			prevClassFullName = classFullName ; 
			wasInAppCode = isInAppCode ; 
		}
	}

	/**
	 * The unqualified class-name of <code>object</code>. 
	 * @param object May be null. 
	 */
	public static String theClassOf(Object object) { 
		StringBuilder sb = new StringBuilder(); 
		if (object!=null) { 
			buildUnqualifiedName(object.getClass().getName(), sb); 
		} else { 
			sb.append("(null)"); 
		}
		return sb.toString(); 
	}

	/**
	 * The unqualified class-name of <code>object</code>. 
	 * @param object May be null. 
	 */
	public static String theObject(Object object) { 
		StringBuilder sb = new StringBuilder(); 
		if (object!=null) { 
			sb.append(object.toString()); 
		} else { 
			sb.append("(null)"); 
		}
		return sb.toString(); 
	}

	protected static void buildUnqualifiedName(String fullName, StringBuilder sb) {
		int dotIndex = fullName.lastIndexOf('.'); 
		if (dotIndex>=0) { 
			sb.append(fullName.substring(dotIndex+1)); 
		} else { 
			sb.append(fullName); 
		}
	}
	
	/**
	 * Returns a single-line form of the current stack. 
	 */
	public static String currentStack() { 
		return theStack(Thread.currentThread().getStackTrace(), 2); 
	}
	
	public static String currentStack(int skipCount) { 
		return theStack(Thread.currentThread().getStackTrace(), skipCount+3); 
	}

	/**
	 * Returns a single-line form of the thread's stack. 
	 */
	public static String theStack(Thread thread) { 
		return theStack(thread.getStackTrace(), 2); 
	}

	/**
	 * Returns a single-line form of the exception's stack. 
	 */
	public static String theStack(Throwable e) { 
		return theStack(e.getStackTrace(), 0) ; 
	}
	
	/**
	 * Returns a single-line form of the stack-trace. 
	 */
	public static String theStack(StackTraceElement[] stackTrace) { 
		return theStack(stackTrace, 0) ; 
	}
	
	private static String theStack(StackTraceElement[] stackTrace, int skipCount) { 
		StringBuilder sb = new StringBuilder(); 
		build(sb, stackTrace, skipCount);
		return sb.toString();
	}

	/** 
	 * Appends file and line number to <code>sb</code>. (Note: It is file, not class.) 
	 * This method grabs a stack-trace and skips up an appropriate number of calls. 
	 */
	private static void buildCurrentLine(StringBuilder sb, int skipCount) {
		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		buildLine(sb, stackTrace, 3+skipCount);
	}

	/** 
	 * Appends file and line number to <code>sb</code>. (Note: It is file, not class.) 
	 */
	private static void buildLine(StringBuilder sb, StackTraceElement[] stackTrace, int skipCount) { 
		if (skipCount>=stackTrace.length) { 
			sb.append("<short-stack>"); 
			return ; 
		}
		StackTraceElement stackItem = stackTrace[skipCount]; 
		String fileName = stackItem.getFileName();  
		int suffixIndex = fileName.indexOf(".java");
		if (suffixIndex>0) fileName = fileName.substring(0, suffixIndex); 
		sb.append(fileName); 
		sb.append(":"); 
		sb.append(stackItem.getLineNumber());
	}

	/** 
	 * String containing the current line: file and number. 
	 */
	public static String currentLine() { 
		StringBuilder sb = new StringBuilder(); 
		buildCurrentLine(sb, 0); 
		return sb.toString(); 
	}

	/** 
	 * String containing the caller's line: file and number. 
	 * <p>
	 * WARNING: Not to be confused with {@link #currentLine()}.
	 */
	public static String callerLine() { 
		StringBuilder sb = new StringBuilder(); 
		buildCurrentLine(sb, 1); 
		return sb.toString(); 
	}
	
	/** 
	 * String containing the caller's line: file and number. 
	 */
	public static String loggedLine() { 
		StringBuilder sb = new StringBuilder(); 
		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		StackTraceElement stackItem = stackTrace[4]; 
		String fileName = stackItem.getFileName();  
		int suffixIndex = fileName.indexOf(".java");
		if (suffixIndex>0) fileName = fileName.substring(0, suffixIndex); 
		sb.append(stackItem.getLineNumber()); 
		sb.append(" "); 
		sb.append(fileName); 
		return sb.toString(); 
	}

	/** 
	 * String containing file and number of line <code>skipCount</code> frames up the stack. 
	 */
	public static String currentLine(int skipCount) { 
		StringBuilder sb = new StringBuilder(); 
		buildCurrentLine(sb, skipCount); 
		return sb.toString(); 
	}

	/**
	 * Returns a space separated list of the strings.
	 * 
	 * This is suitable for lists of strings with no embedded whitespace. 
	 */
	public static String words(String[] labels) {
		StringBuffer sb = new StringBuffer();
		for (int i=0 ; i<labels.length ; i++) { 
			sb.append(" "); 
			String label = labels[i] ; 
			sb.append(label!=null ? label : "<NULL>"); 
		}
		return sb.toString();
	}
	/**
	 * Returns a comma separated list of the strings.
	 * 
	 * This is suitable for lists of short simple strings such as labels, where the demarcation of each item is obvious. 
	 */
	public static String labels(String[] labels) {
		StringBuffer sb = new StringBuffer();
		String sep = "" ; 
		for (int i=0 ; i<labels.length ; i++) { 
			sb.append(sep); 
			String label = labels[i] ; 
			sb.append(label!=null ? label : "<NULL>"); 
			sep = ", " ; 
		}
		return sb.toString();
	}

	/**
	 * Returns a comma separated list of the strings.
	 * 
	 * This is suitable for lists of short simple strings such as labels, where the demarcation of each item is obvious. 
	 */
	public static String labels(Collection<String> labelCollection) {
		String [] labels = new String[labelCollection.size()]; 
		labelCollection.toArray(labels); 
		return labels(labels); 
	}
	
	public static String these(Collection<String> stringCollection) {
		String [] strings = new String[stringCollection.size()]; 
		stringCollection.toArray(strings); 
		return these(strings); 
	}

	public static String these(String[] strings) {
		StringBuffer sb = new StringBuffer();
		String sep = "'" ; 
		for (int i=0 ; i<strings.length ; i++) { 
			sb.append(sep); 
			String label = strings[i] ; 
			sb.append(label!=null ? label : "null"); 
			sep = "', '" ; 
		}
		if (strings.length>0) sb.append("'"); 
		return sb.toString();
	}
	
	public static String [] toArray(Collection<String> collection) { 
		String[] array = new String[collection.size()] ; 
		collection.toArray(array); 
		return array ; 
	}
	
//	public static String the(Rect rect) { 
//		return ""+rect.left+","+rect.top+","+rect.right+","+rect.bottom ; 
//	}

	/**
	 * Returns a human-readable string for an array of numbers. 
	 * Type <code>double</code> are multiplied by <code>100</code> and truncated to an <code>int</code>. 
	 * @param values May be an array of int, double or boolean. 
	 */
	public static String numbers(Object values) {  
		StringBuilder sb = new StringBuilder(); 
		if (values instanceof int[]) { 
			int[] array = (int[]) values ; 
			for (int i=0 ; i<array.length ; i++) { 
				if (sb.length()>0) sb.append(" "); 
				sb.append(array[i]); 
			}
		} else if (values instanceof double[]) { 
			double[] array = (double[]) values ; 
			for (int i=0 ; i<array.length ; i++) { 
				if (sb.length()>0) sb.append(" "); 
				sb.append((int)(100*array[i])); 
			}
		} else if (values instanceof boolean[]) { 
			boolean[] array = (boolean[]) values ; 
			for (int i=0 ; i<array.length ; i++) { 
				if (sb.length()>0) sb.append(" "); 
				sb.append(array[i]); 
			}
		} else {
			throw new RuntimeException("Unknown array type: "+ values.getClass().getName()); 
		}
		return sb.toString();
	}
	
	/** Array of numbers zero to twenty. */
	public static String[] unitNumbers = new String[] {
		"zero", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine", 
		"ten", "eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen", "seventeen", "eighteen", "nineteen",
		"twenty"}; 
	
	/** Array of tens numbers - null, ten, twenty, ..., ninety. */
	public static String[] tensNumbers = new String[] {
		null, "ten", "twenty", "thirty", "forty", "fifty", "sixty", "seventy", "eighty", "ninety" 
	}; 
	
	/**
	 * Returns text for numbers 0 to 999, in lower-case. That is: zero, one, two, etc.  
	 */
	public static String number(int number) { 
		if (number < 0) { 
			throw new UnsupportedOperationException("Cannot compute text for number "+number); 
		} else if (number<unitNumbers.length) { 
			return unitNumbers[number] ; 
		} else { 
			StringBuffer sb = new StringBuffer(); 
			int unitsDigit = number % 10 ; 
			int tens = number / 10 ; 
			int tensDigit = tens % 10 ; 
			int hundreds = tens / 10 ; 
			int hundredsDigit = hundreds % 10 ; 
			int thousands = hundreds / 10 ; 
			if (thousands>0) { 
				if (thousands>=1000) throw new UnsupportedOperationException("Cannot compute text for number "+number); 
				sb.append(number(thousands)); 
				sb.append(" thousand"); 
			}
			if (hundredsDigit>0) { 
				sb.append(unitNumbers[hundredsDigit]); 
				sb.append(" hundred"); 
				if (unitsDigit>0 || tensDigit>0) sb.append(" and"); 
			}
			if (tensDigit<=1) { 
				int mod100 = number%100;
				if (mod100>0) { 
					if (hundreds>0) sb.append(" "); 
					sb.append(unitNumbers[mod100]); 
				}
			} else { 
				if (tensDigit>0) { 
					if (hundreds>0) sb.append(" "); 
					sb.append(tensNumbers[tensDigit]); 
				}
				if (unitsDigit>0) { 
					if (tens>0) sb.append(" "); 
					sb.append(unitNumbers[unitsDigit]); 
				}
			}
			return sb.toString(); 
		}
	}

	/**
	 * Returns a string for a color. 
	 * The alpha component is in brackets, then the RGB components. 
	 * They are separated by spaces. 
	 * All are two digit hex. 
	 */
	public static String color(int color) {
		return 
			"("+Integer.toHexString((color>>24)&0x0000FF)+") "
			+ Integer.toHexString((color&0x00FF0000)>>16)+" "
			+ Integer.toHexString((color&0x0000FF00)>>8)+" "
			+ Integer.toHexString((color&0x000000FF)>>0)
			; 
	}
	
	public static String padCentreJustify(String str, int fullWidth) { 
		int strLength = str.length();
		if (strLength>=fullWidth) return str ; 
		int totalPadCount = fullWidth - strLength ; 
		int leftPadCount = totalPadCount / 2 ; 
		int rightPadCount = totalPadCount - leftPadCount ; 
		return makeSpaceString(leftPadCount) + str + makeSpaceString(rightPadCount); 
	}
	
	public static final DecimalFormat format_decimalTwoPlaces = new DecimalFormat("###,##0.##"); 
	
	public static String decimalTwoPlaces(double number) { 
		return format_decimalTwoPlaces.format(number); 
	}
	
	public static final DecimalFormat format_integer2chars = new DecimalFormat("00"); 

	/**
	 * Returns the given integer zero-padded to at least two digits.  
	 */
	public static String zeroPad_2(int number) { 
		return format_integer2chars.format(number); 
	}
	
	public static String integer2chars(double number) { 
		return format_integer2chars.format(number); 
	}
	
	public static final DecimalFormat format_integer4chars = new DecimalFormat("0000"); 
	
	/**
	 * Returns the given integer zero-padded to at least four digits.  
	 */
	public static String zeroPad_4(int number) { 
		return format_integer4chars.format(number); 
	}

	public static String integer4chars(double number) { 
		return format_integer4chars.format(number); 
	}
	
	public static final DecimalFormat format_integer4chars_spaces = new DecimalFormat("###0"); 

	public static String integer4chars_spaces(double number) { 
		return format_integer4chars_spaces.format(number); 
	}

	/**
	 * Returns an integral percentage. 
	 * Currently (sep15), always rounds down. 
	 */
	public static String percent(double doubleFraction) {  
		return ""+(int)(doubleFraction*100);
	}

	/**
	 * Returns an integral percentage. 
	 * Currently (sep15), always rounds down. 
	 */
	public static String percent(int numerator, int denominator) {  
		return percent((numerator/(double)denominator)); 
	}

	/**
	 * Returns an <code>long</code> percentage. 
	 * Currently (sep15), always rounds down. 
	 */
	public static String percent(long numerator, long denominator) {  
		return percent((numerator/(double)denominator)); 
	}
	
	public static String the(Object[] objects) { 
		StringBuilder sb = new StringBuilder(); 
		for (int i=0 ; i<objects.length ; i++) { 
			if (sb.length()>0) sb.append(' '); 
			sb.append(objects[i]); 
		}
		return sb.toString();
	}
	
	public static String theHex(byte [] bytes) { 
		Formatter formatter = new Formatter(); 
		for (int i=0 ; i<bytes.length ; i++) {
			if (i>0) formatter.format(" "); 
			byte theByte = bytes[i];
			formatter.format("%02x", theByte);
		}
		String hex = formatter.toString();
		formatter.close(); 
		return hex ; 
	}
	
	public static String the(List<Integer> numbers) { 
		return the(intArray(numbers)); 
	}

	public static String the(int[] array) {
		StringBuilder sb = new StringBuilder(); 
		build(sb, array);
		return sb.toString();
	}

	public static String the(int[] array, int length) {
		StringBuilder sb = new StringBuilder(); 
		build(sb, array, length);
		return sb.toString();
	}

	public static void build(StringBuilder sb, int[] array) { 
		build(sb, array, array.length); 
	}

	public static void build(StringBuilder sb, int[] array, int length) {
		for (int i=0 ; i<length ; i++) { 
			if (sb.length()>0) sb.append(' '); 
			sb.append(array[i]); 
		}
	}

	public static String the(long[] array) {
		StringBuilder sb = new StringBuilder(); 
		build(sb, array); 
		return sb.toString();
	}

	public static void build(StringBuilder sb, long[] array) { 
		build(sb, array, array.length); 
	}

	public static void build(StringBuilder sb, long[] array, int length) {
		for (int i=0 ; i<length ; i++) { 
			if (sb.length()>0) sb.append(' '); 
			sb.append(array[i]); 
		}
	}
	
	public static String decimalTwoPlaces(double[] array) {
		StringBuilder sb = new StringBuilder(); 
		build_decimalTwoPlaces(sb, array);
		return sb.toString();
	}

	public static void build_decimalTwoPlaces(StringBuilder sb, double[] array) { 
		build_decimalTwoPlaces(sb, array, array.length); 
	}

	public static void build_decimalTwoPlaces(StringBuilder sb, double[] array, int length) {
		for (int i=0 ; i<length ; i++) { 
			if (sb.length()>0) sb.append(' '); 
			sb.append(decimalTwoPlaces(array[i])); 
		}
	}

	public static String the(double[] array) {
		StringBuilder sb = new StringBuilder(); 
		build(sb, array);
		return sb.toString();
	}

	public static void build(StringBuilder sb, double[] array) { 
		build(sb, array, array.length); 
	}

	public static void build(StringBuilder sb, double[] array, int length) {
		for (int i=0 ; i<length ; i++) { 
			if (sb.length()>0) sb.append(' '); 
			sb.append(array[i]); 
		}
	}

	public static String percents(double[] array) {
		StringBuilder sb = new StringBuilder(); 
		buildPercents(sb, array);
		return sb.toString();
	}

	public static void buildPercents(StringBuilder sb, double[] array) {
		for (int i=0 ; i<array.length ; i++) { 
			if (sb.length()>0) sb.append(' '); 
			sb.append(percent(array[i])); 
		}
	}

	public static String the(float[] array) {
		StringBuilder sb = new StringBuilder(); 
		build(sb, array);
		return sb.toString();
	}

	public static void build(StringBuilder sb, float[] array) { 
		build(sb, array, array.length); 
	}

	public static void build(StringBuilder sb, float[] array, int length) {
		for (int i=0 ; i<length ; i++) { 
			if (sb.length()>0) sb.append(' '); 
			sb.append(array[i]); 
		}
	}

	public static String quartiles5(ArrayList<Integer> numbers) { 
		int[] array = intArray(numbers); 
		return quartiles5(array); 
	}

	public static int[] intArray(List<Integer> numbers) {
		int[] array = new int[numbers.size()]; 
		for (int i=0 ; i<numbers.size() ; i++) array[i] = numbers.get(i);
		return array;
	}
	
	/**
	 * The quartiles, including minimum and maximum, of the array. 
	 * @param numbers Must not be null or zero-length. 
	 */
	public static String quartiles5(int [] numbers) { 
		int[] quartiles = MediansInt.getQuartilesMinMax(numbers); 
		return the(quartiles) + " ("+numbers.length+")"; 
	}
	
	/**
	 * Returns the time interval, in appropriate units, for the given number of milliseconds.
	 */
	public static String timeInterval(long millis) { 
		if (millis<1000) return ""+millis+"ms" ; 
		long seconds = millis / 1000 ; 
		if (seconds<60) return ""+seconds+"s" ; 
		long minutes = seconds / 60 ; 
		if (minutes<60) return ""+minutes+"m" ; 
		long hours = minutes / 60 ; 
		if (hours<24) return ""+hours+"h" ; 
		long days = hours / 24 ; 
		if (days<14) return ""+days+"d"; 
		long weeks = days / 7 ; 
		return ""+weeks+"w" ; 
	}
	
	/**
	 * Returns the time interval, in appropriate units, for the given number of milliseconds. 
	 * <p>
	 * Avoids returning "1" of a time unit, will report it as overflow in smaller unit. 
	 */
	public static String timeIntervalOneless(long millis) { 
		if (millis<2000) return ""+millis+"ms" ; 
		long seconds = millis / 1000 ; 
		if (seconds<120) return ""+seconds+"s" ; 
		long minutes = seconds / 60 ; 
		if (minutes<120) return ""+minutes+"m" ; 
		long hours = minutes / 60 ; 
		if (hours<48) return ""+hours+"h" ; 
		long days = hours / 24 ; 
		if (days<14) return ""+days+"d"; 
		long weeks = days / 7 ; 
		return ""+weeks+"w" ; 
	}
	
	/**
	 * Returns the time interval, in appropriate units, since the given time. 
	 * If the given time <code>then</code> is zero, <em>unknown</em> is returned. 
	 */
	public static String timeSince(long then) { 
		if (then==0) return "<unknown>" ; 
		return timeInterval(System.currentTimeMillis()-then); 
	}
	
	public static final SimpleDateFormat timeFormat_HHmm = new SimpleDateFormat("HH:mm"); // Eg, 21:30
	public static final SimpleDateFormat timeFormat_EE = new SimpleDateFormat("EE"); // Eg, Tue
	public static final SimpleDateFormat timeFormat_dMMM = new SimpleDateFormat("d MMM"); // Eg, 5 Jul
	public static final SimpleDateFormat timeFormat_yyyy = new SimpleDateFormat("yyyy"); // Eg, 2015
	
	/**
	 * Returns a short string showing the given time. 
	 * <p>
	 * If it's in the last few hours, it will be 24-hours clock time. 
	 * A little longer, it will be a day of the week. 
	 * Longer than that, it will be a date. 
	 */
	public static String timeOf(long then) { 
		if (then==0) return "<unknown>" ; 
		long millis = System.currentTimeMillis() - then ; 
		if (millis<0) return "<future>" ; 
		long hours = millis / (60 * 60 * 1000) ; 
		if (hours<12) return timeFormat_HHmm.format(then); 
		long days = hours / 24 ; 
		if (days<5) return timeFormat_EE.format(then); 
		if (days<180) return timeFormat_dMMM.format(then); 
		return timeFormat_yyyy.format(then); 
	}
	
	public static String substrAfter(String fullString, String prefix) { 
		if (!fullString.startsWith(prefix)) return null ; 
		return fullString.substring(prefix.length()); 
	}
	
	/**
	 * True if the string is non-null and not empty. 
	 */
	public static boolean hasContent(String str) { 
		if (str==null) return false ; 
		return str.length() > 0 ; 
	}

//	private String spaceString = makeSpaceString(40); 
	private static String bigStringOfSpaces = " " ; 
	public static String makeSpaceString(int size) { 
		while (size>bigStringOfSpaces.length()) bigStringOfSpaces = bigStringOfSpaces + bigStringOfSpaces ; 
		return bigStringOfSpaces.substring(0, size); 
	}

	public static String getFileBasename(File file) { 
		return getFileBasename(file.getName());
	}

	/**
	 * Returns the string with the last dot and following suffix removed. 
	 * <p>
	 * If there's no dot, the string is returned unchanged. 
	 */
	public static String getFileBasename(String filename) {
		int dotIndex = filename.lastIndexOf('.'); 
		if (dotIndex>=0) { 
			return filename.substring(0, dotIndex); 
		} else { 
			return filename ; 
		}
	}

	public static String clip(String str, int len) { 
		if (str==null) return "<NULL>" ; 
		if (str.length()<=len) return str ; 
		return str.substring(0, len-2)+".." ; 
	}

	public static String clip(String str) {
		return clip(str, 10); 
	}
	
	public static String inWidth(String in, int length, boolean leftJustified, String padding) { 
		int contentMaxLength = length-padding.length();
		String clipped = clip(in, contentMaxLength); 
		int clippedLength = clipped.length();
		if (clippedLength==contentMaxLength) { 
			return clipped + padding ; 
		} else { 
			String extraPadding = makeSpaceString(contentMaxLength-clippedLength); 
			if (leftJustified) { 
				return clipped + extraPadding + padding ; 
			} else { 
				return extraPadding + padding + clipped ; 
			}
		}
	}
	
	/**
	 * Creates lines from the list of words, with the given maximum line length. 
	 */
	public static String [] toLineArray(Collection<String> words, int lineLength) { 
		ArrayList<String> lineList = new ArrayList<String>(); 
		StringBuffer nextLine = new StringBuffer(); 
		for (String word : words) { 
			if (nextLine.length()+word.length()+1<=lineLength) { 
				if (nextLine.length()>0) nextLine.append(' '); 
				nextLine.append(word); 
			} else { 
				lineList.add(nextLine.toString()); 
				nextLine.setLength(0);
				nextLine.append(word); 
			}
		}
		lineList.add(nextLine.toString()); 
		return toArray(lineList); 
	}
	
	static class ClassTest { 
		
		private void testNumberMethod(int number, String expected) { 
			String actual = number(number); 
			if (!actual.equals(expected)) throw new RuntimeException("Expected '"+expected+"', got '"+actual+"' for number "+number); 
		}
		
		private void testNumberMethod() { 
			testNumberMethod(0, "zero");
			testNumberMethod(1, "one");
			testNumberMethod(12, "twelve");
			testNumberMethod(987, "nine hundred and eighty seven");
			//testNumberMethod(42, "The answer...");
		}
		
		ClassTest() { 
			testNumberMethod();
			//////  Passed! Bye bye.  
			System.out.println("Passed test suite "+this.getClass().getCanonicalName()); 
		}
	}

	public static void main_DEL(String [] args) { 
		new ClassTest(); 
	}

	public static String toWIC(String word) {
		return word.substring(0,1).toUpperCase() + word.substring(1).toLowerCase() ;
	}

	public static boolean isAlphabetic(String str) { 
		int length = str.length(); 
		for (int i=0 ; i<length ; i++) { 
			if (!Character.isAlphabetic(str.charAt(i))) return false ; 
		}
		return true ; 
	}
	
	public static boolean isNumeric(String str) { 
		int length = str.length(); 
		for (int i=0 ; i<length ; i++) { 
			if (!Character.isDigit(str.charAt(i))) return false ; 
		}
		return true ; 
	}
	
	/**
	 * Returns <code>true</code> if there are at least the given number of uppercase letters, and no lower case letters. 
	 * <p>
	 * This might return true when you're not intending it. For example, reference numbers or generated-random strings. 
	 */
	public static boolean isUpperCase_mixed(String str, int minUpperCount) { 
		int upperCount = 0 ; 
		for (int i=0 ; i<str.length() ; i++) { 
			char ch = str.charAt(i); 
			if (Character.isLowerCase(ch)) return false ; 
			if (Character.isUpperCase(ch)) upperCount ++ ; 
		}
		return upperCount >= minUpperCount ; 
	}

	/**
	 * Trims all non-alphanumeric characters from the beginning and end of the string. 
	 * <p>
	 * Non-alphabetics embedded within the string are not affected. 
	 */
	public static String trimPunctuation(String in) { 
		int beginIndex = 0 ; 
		int endIndex = in.length()-1 ; 
		while (!Character.isLetterOrDigit(in.charAt(beginIndex))) beginIndex++ ; 
		while (!Character.isLetterOrDigit(in.charAt(endIndex))) endIndex-- ; 
		endIndex++ ; 
		return in.substring(beginIndex, endIndex); 
	}

	/**
	 * Returns the length of the longest string in the given array. 
	 * @param words Assumed non-null, but elements may be null. 
	 * @return <code>Integer.MIN_VALUE</code> if there are no non-null elements. (Including an empty array.)
	 * @throws NullPointerException if the argument is null. 
	 */
	public static int getMaxStringLength(String [] words) { 
		if (words==null) throw new NullPointerException(); 
		int max = Integer.MIN_VALUE ; 
		for (String word : words) if (word!=null && word.length()>max) max = word.length(); 
		return max ; 
	}

	/**
	 * Returns the given string, wrapped in single quotes. 
	 * If it's null, returns string <code>NULL</code> unquoted. 
	 * @param str
	 */
	public static String quoted(String str) {
		if (str==null) return "NULL" ; 
		return "'"+str+"'" ; 
		
	}
	
}
