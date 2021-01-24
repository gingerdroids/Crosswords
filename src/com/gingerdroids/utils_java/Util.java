package com.gingerdroids.utils_java;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class Util {

	/**
	 * Always true, but the compiler's flow analysis doesn't know that. 
	 */
	public static final boolean ttrue = "".length() == 0;
	
	/**
	 * Always false, but the compiler's flow analysis doesn't know that. 
	 */
	public static final boolean ffalse = ! ttrue;
	
	public static final long  millisPerHour = 60 * 60 * 1000 ;

	/**
	 * Number of milliseconds in a day. 
	 * Often used to subtract a number of days from a <code>Date</code> or <code>Calendar</code>. 
	 * Beware: Some years have leap-seconds! 
	 */
	public static final long  millisPerDay = 24 * millisPerHour ;
	
	public static final String [] emptyStringArray = new String[]{};
	public static final Void [] emptyVoidArray = new Void[]{};
	
	/**
	 * Does nothing. Sometimes useful for placing breakpoints. 
	 */
	public static void noop(){}
	
	/**
	 * The <em>Golden Ratio</em>, sometimes useful for laying out displays. 
	 */
	public static final double GOLDEN_RATIO = 1.61803398875;
	
	/**
	 * Reciprocal of the <em>Golden Ratio</em>. 
	 */
	public static final double GOLDEN_RATIO_INV = 1.0 / GOLDEN_RATIO;
	
	/**
	 * Random number generator for general use throughout the code. 
	 * <p>
	 * In dev-code where you want replicable runs, you might want to set the seed to a constant early in <code>main()</code>.
	 * <pre>
		Util.random.setSeed(987); // Replicable runs desired here. 
	 * </pre> 
	 */
	public static final Random random = new Random(milliTime()); 

	/**
	 * Elapsed time in milliseconds since arbitrary epoch. 
	 * The units aren't exactly milliseconds, but are close. 
	 * This is intended to be a fast call. 
	 * <p>
	 * Based on {@link System#nanoTime()}. 
	 */
	public static long milliTime() { 
		return System.nanoTime() >> 20 ;  
	}
	
	public static final double sqr(double x) { 
		return x * x ; 
	}

	/**
	 * Returns the number of digits in the number. 
	 * <p>
	 * Decimal representation. 
	 * @param number Zero or positive. Not negative. 
	 */
	public static int countDigits(int number) { 
		if (number<0) throw new IllegalArgumentException("Negative maximum of "+number+": urk."); 
		if (number==0) return 1 ; 
		int count = 1 ; 
		while (number>=10) { 
			number /= 10 ; 
			count += 1 ; 
		}
		return count ;
	}
	
	/**
	 * Returns an array for the <code>Collection</code>. 
	 * Does not play nicely with nulls. 
	 */
	public static int [] toArray_int(double [] numbers) { 
		int length = numbers.length ; 
		int [] array = new int[length] ; 
		for (int i=0 ; i<length ; i++) array[i] = (int) Math.round(numbers[i]); 
		return array ; 
	}
	
	/**
	 * Returns an array for the <code>Collection</code>. 
	 * Does not play nicely with nulls. 
	 */
	public static int [] toArray_int(Collection<Integer> collection) { 
		int [] array = new int[collection.size()] ; 
		int i = 0 ; 
		for (Integer value : collection) { 
			array[i++] = value ; 
		}
		return array ; 
	}

	/**
	 * Returns an array for the <code>Collection</code>. 
	 * Does not play nicely with nulls. 
	 */
	public static long [] toArray_long(Collection<Long> collection) { 
		long [] array = new long[collection.size()] ; 
		int i = 0 ; 
		for (Long value : collection) { 
			array[i++] = value ; 
		}
		return array ; 
	}

	/**
	 * Returns an array for the <code>Collection</code>. 
	 * Does not play nicely with nulls. 
	 */
	public static double[] toArray_double(Collection<Double> collection) { 
		double[] array = new double[collection.size()] ; 
		int i = 0 ; 
		for (Double value : collection) { 
			array[i++] = value ; 
		}
		return array ; 
	}

	public static ArrayList<Integer> toListOfInteger(int [] ints) { 
		ArrayList<Integer> list = new ArrayList<Integer>(ints.length); 
		for (int i=0 ; i<ints.length ; i++) list.add(ints[i]); 
		return list ; 
	}

	public static List<Long> toListOfLong(long [] longs) { 
		List<Long> list = new ArrayList<Long>(longs.length); 
		for (int i=0 ; i<longs.length ; i++) list.add(longs[i]); 
		return list ; 
	}

	public static Set<Long> toSetOfLong(long [] longs) { 
		Set<Long> set = new HashSet<Long>(longs.length); 
		for (int i=0 ; i<longs.length ; i++) set.add(longs[i]); 
		return set ; 
	}

	public String getInstanceString() {  
		if (instanceCounter==null) return InstanceCounter.outerClass.getSimpleName() + "?" + InstanceCounter.count ; // Can occur, eg if called in super-class constructor. 
		return instanceCounter.instanceString ; 
	}

	/**
	 * Returns the folder corresponding to the <code>System</code> property "<code>user.dir</code>". 
	 * <p>
	 * @see System#getProperty(String)
	 */
	public static File getCWD() { 
		File userDir = new File(System.getProperty("user.dir"));
		return userDir;
	}

	private final InstanceCounter instanceCounter = new InstanceCounter();

	protected static class InstanceCounter { 
		private static final Object lock_InstanceCounter = new Object(); 
		private static final Class<?> outerClass = InstanceCounter.class.getEnclosingClass(); 
		private static int count = 0 ; 
		private final int instanceNumber ; 
		private final String instanceString ; 
		private InstanceCounter() { 
			synchronized (lock_InstanceCounter) { 
				this.instanceNumber = count ; 
				this.instanceString = outerClass.getSimpleName() + "-" + instanceNumber ; 
				count ++ ; 
			}
		}
	}

	/**
	 * Returns a subdirectory of the home folder, or the home folder itself. 
	 * Will create the subdirectory, if necessary. 
	 * 
	 * @param subdirPath Null implies return the home folder. 
	 */
	public static File getHomeSubdirectory(String subdirPath) {
		String homeDirPath = System.getProperty("user.home"); 
		File homeDir = new File(homeDirPath); 
		if (subdirPath==null) return homeDir ; 
		String [] names = subdirPath.split("/"); 
		File dir = homeDir ; 
		for (String name : names) { 
			dir = new File(dir, name); 
		}
		return dir ; 
	}

	/**
	 * Returns a subdirectory of the Desktop folder, or the Desktop folder itself. 
	 * Will create the subdirectory, if necessary. 
	 * 
	 * @param subdirPath Null implies return the <code>Desktop</code> folder. 
	 */
	public static File getDesktopSubdirectory(String subdirPath) {
		String homeDirPath = System.getProperty("user.home"); 
		File homeDir = new File(homeDirPath); 
		File desktopDir = new File(homeDir, "Desktop"); 
		if (subdirPath==null) return desktopDir ; 
		String [] names = subdirPath.split("/"); 
		File dir = desktopDir ; 
		for (String name : names) { 
			dir = new File(dir, name); 
		}
		return dir ; 
	}
	
	/**
	 * Returns how many booleans are <code>true</code> in the array. 
	 */
	public static int countTrue(boolean[] booleans) { 
		int count = 0 ; 
		for (boolean b : booleans) if (b) count ++ ; 
		return count ; 
	}
	
	private static final void checkTrue(boolean isAsserted, String messageIfFalse) { 
		if (isAsserted) return ; 
		if (messageIfFalse==null) throw new RuntimeException(); 
		throw new RuntimeException(messageIfFalse); 
	}
	
	public static final void checkTrue(boolean isAsserted) { 
		checkTrue(isAsserted, null); 
	}
	
	public static int [] makeShuffledIndices(int length) { 
		int [] indices = new int[length] ; 
		for (int i=0 ; i<length ; i++) indices[i] = i ; 
		for (int i=length-1 ; i>0 ; i--) { 
			int j = random.nextInt(i+1); 
			if (j>i) throw new RuntimeException("WTF!?"); 
			indices[i] = indices[j] ; 
			indices[j] = i ; 
		}
		return indices ; 
	}
	
	public static int [] shuffle(int [] array) { 
		int length = array.length ; 
		for (int i=length-1 ; i>0 ; i--) { 
			int j = random.nextInt(i+1); 
			int tmp = array[i] ; 
			array[i] = array[j] ; 
			array[j] = tmp ; 
		}
		return array ; 
	}
	
	/**
	 * Does nothing, except writes a reminder to standard-error that  something needs fixing near the calling line. 
	 * Useful when you've commented out / altered functionality for debugging purposes, and don't want to forget about it. 
	 */
	public static Object cruft() { 
		System.err.println("Near "+Str.currentLine(1)+"\t"+"Fix or uncomment"); 
		/* Can't just call fixHere(String) because the currentLine would return this method, not the caller .*/
		return null ; // Returning something makes it easier to run in the outer layer of a class, by initialising a static variable. 
	}

	/**
	 * Does nothing, except writes a reminder to standard-error that something needs fixing near the calling line. 
	 * Useful when you've commented out / altered functionality for debugging purposes, and don't want to forget about it. 
	 */
	public static Object cruft(String message) { 
		System.err.println("Near "+Str.currentLine(1)+"\t"+message); 
		return null ; // Returning something makes it easier to run in the outer layer of a class, by initialising a static variable. 
	}
	
	/**
	 * Writes a text file containing the given line, if the file doesn't already exist. 
	 * If the file already exists, this method does nothing. 
	 */
	public static File writeFile(File dir, String filename, String text) { 
		return writeFile(dir, filename, new String[] {text});
	}
	
	/**
	 * Writes a text file containing the given lines, if the file doesn't already exist. 
	 * If the file already exists, this method does nothing. 
	 */
	public static File writeFile(File dir, String filename, String [] lines) { 
		File file = new File(dir, filename); 
		if (!file.exists()) { 
			PrintWriter writer;
			try {
				writer = new PrintWriter(file);
				for (String line : lines) { 
					if (line!=null) writer.println(line); 
				}
				writer.close(); 
			} catch (FileNotFoundException e) {
				throw new RuntimeException(e); 
			} 
		}
		return file ; 
	}
	
	public static File ensureDirExists(File dir) { 
		if (dir.exists()) { 
			if (!dir.isDirectory()) throw new RuntimeException("Expected directory at "+dir.getAbsolutePath()); 
			/* Ideally, would throw IOException, but that stops it being used to initialize static finals. */
		} else { 
			dir.mkdirs(); 
		}
		return dir ; 
	}

	public static File ensureDir(File dir, String readme_filename, String text) { 
		dir.mkdirs(); 
		return writeFile(dir, readme_filename, new String[] {text});
	}
	
	public static File ensureDir(File dir, String filename, String [] lines) { 
		dir.mkdirs(); 
		return writeFile(dir, filename, lines);
	}
	
}
