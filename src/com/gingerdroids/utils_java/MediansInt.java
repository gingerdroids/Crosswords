package com.gingerdroids.utils_java;

import java.util.Arrays;

import com.gingerdroids.utils_java.MediansInt;

/**
 * Finds medians in an array. 
 * <p>
 * Does not make especial efficient use of already sorted arrays. 
 */
public class MediansInt { 
	
	public final int[] soughtMedians ; 
	
	/**
	 * Finds the given "medians". 
	 * That is, if the array were sorted, which of <code>numbers</code> would be at each of the <code>soughtIndices</code>. 
	 * @param numbers Must not be zero-length. 
	 * @param soughtIndices Must not be zero-length. 
	 */
	private MediansInt(int[] numbers, int length, int[] soughtIndices) { 
		int soughtLength = soughtIndices.length ; 
		this.soughtMedians = new int[soughtLength] ; 
		if (length==1) { 
			/* There's only one number in the list, so it's all the medians! */
			Arrays.fill(soughtMedians, numbers[0]); 
		} else if (soughtLength==1 && soughtIndices[0]==0) { 
			/* Seeking minimum, and no other. */
			int result = numbers[0] ; 
			for (int i=1 ; i<length ; i++) if (numbers[i]<result) result = numbers[i] ; 
			soughtMedians[0] = result ; 
		} else if (soughtLength==1 && soughtIndices[soughtLength-1]==length-1) { 
			/* Seeking maximum, and no other. */
			int result = numbers[0] ; 
			for (int i=1 ; i<length ; i++) if (numbers[i]>result) result = numbers[i] ; 
			soughtMedians[0] = result ; 
		} else { 
			/* Here we know: there are at least two numbers. */
			int n0 = numbers[0] ; 
			int n1 = numbers[1] ; 
			int index = 1 ; // Index of 'n1'  
			while (n0==n1 && index<length-1) n1 = numbers[++index] ; 
			if (n0==n1) { 
				/* Here we know: all the numbers are the same. */
				Arrays.fill(soughtMedians, n0); 
			} else { 
				/* Normal recursive case. */
				int threshold = (n0+n1) / 2 ; 
				/* Here we know: At least one number is less-equal 'threshold', and at least one is greater-than 'threshold'. */
				//// Split numbers so 'lowers' holds less-equal the threshold, and 'uppers' holds greater-than the threshold. 
				int[] lowers = new int[length-1] ; 
				int[] uppers = new int[length-1] ; 
				int lowerLength = 0 ; 
				int upperLength = 0 ; 
				for (int i=0 ; i<length ; i++) {
					int number = numbers[i] ; 
					if (number<=threshold) { 
						lowers[lowerLength++] = number ; 
					} else { 
						uppers[upperLength++] = number ; 
					}
				}
				//// Build the sought-indices arrays for 'lowers' and 'uppers'. 
				int lowerSoughtLength = 0 ; // How many of the sought-indices are in the 'lowers' part. 
				while (lowerSoughtLength<soughtIndices.length && soughtIndices[lowerSoughtLength]<lowerLength) lowerSoughtLength ++ ; 
				int [] lowerSoughtIndices = new int[lowerSoughtLength] ; 
				int [] upperSoughtIndices = new int[soughtLength-lowerSoughtLength] ; 
				System.arraycopy(soughtIndices, 0, lowerSoughtIndices, 0, lowerSoughtLength); 
				for (int i=0 ; i<upperSoughtIndices.length ; i++) { 
					upperSoughtIndices[i] = soughtIndices[lowerSoughtLength+i] - lowerLength ; 
				}
				//// Get the recursive soughtMedians
				MediansInt lowerMedians = new MediansInt(lowers, lowerLength, lowerSoughtIndices); 
				MediansInt upperMedians = new MediansInt(uppers, upperLength, upperSoughtIndices); 
				//// Merge the soughtMedians
				System.arraycopy(lowerMedians.soughtMedians, 0, soughtMedians, 0, lowerSoughtLength); 
				for (int i=0 ; i<upperSoughtIndices.length ; i++) { 
					soughtMedians[lowerSoughtLength+i] = upperMedians.soughtMedians[i] ; 
				}
				/*
					Log.v("TEST", "numbers "+Str.the(numbers, length)+", lowers "+Str.the(lowers, lowerLength)+", uppers "+Str.the(uppers, upperLength)); 
					Log.v("TEST", "    indices "+Str.the(soughtIndices)+", lower indices "+Str.the(lowerSoughtIndices)+", upper indices "+Str.the(upperSoughtIndices)); 
					Log.v("TEST", "    medians "+Str.the(soughtMedians)+", from lower "+Str.the(lowerMedians.soughtMedians)+", from upper "+Str.the(upperMedians.soughtMedians)); 
				 */
			}
		}
	}
	
	public static int getMin(int[] numbers) { 
		if (numbers==null) throw new NullPointerException(); 
		if (numbers.length==0) throw new IllegalArgumentException("Empty array"); 
		int[] soughtIndices = new int[]{0} ; 
		MediansInt medians = new MediansInt(numbers, numbers.length, soughtIndices); 
		return medians.soughtMedians[0] ; 
	}
	
	public static int getMax(int[] numbers) { 
		if (numbers==null) throw new NullPointerException(); 
		if (numbers.length==0) throw new IllegalArgumentException("Empty array"); 
		int length = numbers.length ; 
		int[] soughtIndices = new int[]{length-1} ; 
		MediansInt medians = new MediansInt(numbers, numbers.length, soughtIndices); 
		return medians.soughtMedians[0] ; 
	}
	
	public static int getMedian(int[] numbers) { 
		if (numbers==null) throw new NullPointerException(); 
		if (numbers.length==0) throw new IllegalArgumentException("Empty array"); 
		int length = numbers.length ; 
		int[] soughtIndices = new int[]{length/2} ; 
		MediansInt medians = new MediansInt(numbers, numbers.length, soughtIndices); 
		return medians.soughtMedians[0] ; 
	}
	
	public static int getMedian(int [] numbers, double fraction) { 
		int[] medians = getMedians(numbers, new double[]{fraction}); 
		return medians[0] ; 
	}
	
	public static int getMedian(int [] numbers, int soughtIndex) { 
		int[] medians = getMedians(numbers, new int[]{soughtIndex}); 
		return medians[0] ; 
	}
	
	public static int [] getMedians(int [] numbers, double [] fractions) { 
		if (numbers==null) throw new NullPointerException(); 
		if (numbers.length==0) throw new IllegalArgumentException("Empty array"); 
		int numbersLength = numbers.length ; 
		int fractionsCount = fractions.length ; 
		int[] soughtIndices = new int[fractionsCount] ;
		for (int i=0 ; i<fractionsCount ; i++) { 
			soughtIndices[i] = (int) (numbersLength * fractions[i]) ; 
		}
		return getMedians(numbers, soughtIndices);
	}

	public static int[] getMedians(int[] numbers, int[] soughtIndices) {
		try { 
			MediansInt medians = new MediansInt(numbers, numbers.length, soughtIndices); 
			return medians.soughtMedians ; 
		} catch (OutOfMemoryError e) { 
			e.printStackTrace(System.err); 
			throw new RuntimeException("Caught a "+e.getClass().getSimpleName()); 
		}
	}
	
	/**
	 * Returns an array of five numbers: the minimum, the quartiles, the maximum. 
	 * Where a median falls between two numbers, the one immediately below is returned. 
	 */
	public static int[] getQuartilesMinMax(int[] numbers) { 
		if (numbers==null) throw new NullPointerException(); 
		if (numbers.length==0) throw new IllegalArgumentException("Empty array"); 
		int length = numbers.length ; 
		int[] soughtIndices = new int[5] ;
		soughtIndices[0] = 0 ; 
		soughtIndices[4] = length-1 ; 
		for (int i=1 ; i<4 ; i++) soughtIndices[i] = (i*length) / 4 ; 
//		for (int i=0 ; i<5 ; i++) soughtIndices[i] = (i*length) / 4 ; 
		MediansInt medians = new MediansInt(numbers, numbers.length, soughtIndices); 
		return medians.soughtMedians ; 
	}
	
	private static class MinTest { 
		MinTest(int[] numbers, int desiredResult) { 
			int actualResult = getMin(numbers); 
			if (actualResult!=desiredResult) throw new RuntimeException(); 
		}
	}
	
	private static class MaxTest { 
		MaxTest(int[] numbers, int desiredResult) { 
			int actualResult = getMax(numbers); 
			if (actualResult!=desiredResult) throw new RuntimeException(); 
		}
	}
	
	private static class MedianTest { 
		MedianTest(int[] numbers, int desiredResult) { 
			int actualResult = getMedian(numbers); 
			if (actualResult!=desiredResult) throw new RuntimeException(); 
		}
	}
	
	private static class QuartilesMinMaxTest { 
		QuartilesMinMaxTest(int[] numbers, int[] desiredResults) { 
			if (desiredResults.length!=5) throw new RuntimeException("Error in test: desired array has length "+desiredResults.length); 
			//Log.v("TEST", "TESTING  numbers "+StringMethods.the(numbers)+", desired "+StringMethods.the(desiredResults)); 
			int[] actualResults = getQuartilesMinMax(numbers);
			if (!Arrays.equals(actualResults, desiredResults)) throw new RuntimeException("Actual "+Str.the(actualResults)+", desired "+Str.the(desiredResults)); 
		}
	}
	
	public static class ClassTest { 
		public ClassTest() { 
			new MinTest(new int[]{5}, 5); 
			new MinTest(new int[]{5, 5}, 5); 
			new MinTest(new int[]{5, 5, 5}, 5); 
			new MinTest(new int[]{6, 5, 7}, 5); 
			new MinTest(new int[]{6, 6, 9, 5, 8, 6, 9, 5, 7}, 5); 
			new MaxTest(new int[]{5}, 5); 
			new MaxTest(new int[]{5, 5}, 5); 
			new MaxTest(new int[]{5, 5, 5}, 5); 
			new MaxTest(new int[]{6, 8, 7}, 8); 
			new MaxTest(new int[]{6, 6, 9, 5, 8, 6, 9, 5, 7}, 9); 
			new MedianTest(new int[]{5}, 5); 
			new MedianTest(new int[]{5, 5}, 5); 
			new MedianTest(new int[]{5, 5, 5}, 5); 
			new MedianTest(new int[]{6, 8, 7}, 7); 
			new MedianTest(new int[]{6, 6, 9, 5, 8, 6, 9, 5, 7}, 6); 
			new MedianTest(new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16}, 8); 
			new MedianTest(new int[]{16, 15, 14, 9, 8, 7, 6, 5, 4, 13, 12, 11, 10, 3, 2, 1, 0}, 8); 
			new MedianTest(new int[]{16, 15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0}, 8); 
			new MedianTest(new int[]{3, 8, 9, 14, 4, 10, 11, 2, 16, 12, 13, 5, 6, 7, 15, 0, 1}, 8); 
			new QuartilesMinMaxTest(new int[]{0, 1, 2, 3, 4}, new int[]{0, 1, 2, 3, 4}); 
			new QuartilesMinMaxTest(new int[]{0, 0, 1, 1, 2, 3, 3, 4, 4}, new int[]{0, 1, 2, 3, 4}); 
			new QuartilesMinMaxTest(new int[]{5}, new int[]{5, 5, 5, 5, 5}); // Singleton data set 
			new QuartilesMinMaxTest(new int[]{5, 5, 5, 5}, new int[]{5, 5, 5, 5, 5}); // Uniform data set
			new QuartilesMinMaxTest(new int[]{5, 6}, new int[]{5, 5, 6, 6, 6}); 
			new QuartilesMinMaxTest(new int[]{6, 5}, new int[]{5, 5, 6, 6, 6}); 
			new QuartilesMinMaxTest(new int[]{10, 11, 12, 13, 14, 13, 12, 11, 10}, new int[]{10, 11, 12, 13, 14}); 
			new QuartilesMinMaxTest(new int[]{2, 2, 2, 2, 2, 2, 3, 3, 4, 4, 4}, new int[]{2, 2, 2, 4, 4}); 
			new QuartilesMinMaxTest(new int[]{2, 4, 2, 3, 2, 4, 2, 3, 2, 4, 2}, new int[]{2, 2, 2, 4, 4}); 
			new QuartilesMinMaxTest(new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16}, new int[]{0, 4, 8, 12, 16}); 
			new QuartilesMinMaxTest(new int[]{16, 15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0}, new int[]{0, 4, 8, 12, 16}); 
			new QuartilesMinMaxTest(new int[]{3, 8, 9, 14, 4, 10, 11, 2, 16, 12, 13, 5, 6, 7, 15, 0, 1}, new int[]{0, 4, 8, 12, 16}); 
			new QuartilesMinMaxTest(new int[]{16, 15, 14, 9, 8, 7, 6, 5, 4, 13, 12, 11, 10, 3, 2, 1, 0}, new int[]{0, 4, 8, 12, 16}); 
			//////  Passed! Bye bye.  
			System.out.println("Passed test suite "+this.getClass().getCanonicalName()); 
		}
	}

}
