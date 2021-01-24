package com.gingerdroids.utils_java;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Finds medians in an array. 
 * <p>
 * Does not make especially efficient use of already sorted arrays. 
 */
public class MediansDouble { 
	
	public final double[] soughtMedians ; 
	
	/**
	 * Finds the given "medians". 
	 * That is, if the array were sorted ascending, which of <code>numbers</code> would be at each of the <code>soughtIndices</code>. 
	 * @param numbers Must not be zero-length. 
	 * @param soughtIndices Must not be zero-length. 
	 */
	private MediansDouble(double[] numbers, int[] soughtIndices) { 
		int soughtLength = soughtIndices.length ; 
		//////  Shuffle 'numbers' so the sought-values are in their correct place. 
		semiSort(numbers, 0, numbers.length-1, soughtIndices); 
		//////  Extract the sought-medians
		this.soughtMedians = new double[soughtLength] ; 
		for (int i=0 ; i<soughtLength ; i++) { 
			soughtMedians[i] = numbers[soughtIndices[i]] ; 
		}
	}
	
	private void semiSort(double[] numbers, int loIndex, int hiIndex, int[] soughtIndices) { 
		/* Note: loIndex and hiIndex are inclusive bounds, both inside the range. */
		int length = hiIndex - loIndex + 1 ; 
		if (length<=1) return ; 
		if (!containsSoughtIndex(loIndex, hiIndex, soughtIndices)) return ; 
		//////  Sort into three buckets: below threshold, equal to threshold, above threshold. 
		/* The 'numbers' array is in four sections: sorted-less-than-threshold, sorted-equal-threshold, unsorted, sorted-greater-than-threshold. */
		/* There's redundancy among these section-boundary variables. It was too hard to think about, otherwise. */
		final int loSortedLess = loIndex ; 
		int hiSortedLess = loIndex - 1 ; 
		int loSortedEqual = loIndex ; 
		int hiSortedEqual = loIndex - 1 ; 
		int loUnsorted = loIndex ; 
		int hiUnsorted = hiIndex ; 
		int loSortedGreater = hiIndex + 1 ; 
		final int hiSortedGreater = hiIndex ; 
		double threshold = getRoughMedian(numbers, loIndex, hiIndex); 
		/* Here we know: 'threshold' either is a value in 'numbers', or lies between two values. */ 
		//// Shrink unsorted-section, moving into sorted-sections. 
		while (loUnsorted<=hiUnsorted) { 
			double currentValue = numbers[loUnsorted] ; 
			if (currentValue<threshold) { 
				// Append currentValue to sorted-less, and shift sorted-equal up. 
				hiSortedLess ++ ; 
				loSortedEqual ++ ; 
				hiSortedEqual ++ ; 
				loUnsorted ++ ; 
				numbers[hiSortedLess] = currentValue ; 
			} else if (currentValue>threshold) { 
				// Prepend currentValue to sorted-greater, and copy down the value we're overwriting. 
				hiUnsorted -- ; 
				loSortedGreater -- ; 
				numbers[loUnsorted] = numbers[loSortedGreater] ; 
				numbers[loSortedGreater] = currentValue ; 
			} else { 
				/* Assume: untouchedValue==threshold. Hopefully, there are no infinities, NANs, etc in the array. */
				// Extend the sorted-equal region. 
				// Note: it will be filled with 'threshold' values after the loop finishes. 
				hiSortedEqual ++ ; 
				loUnsorted ++ ; 
			}
		}
		//// Fill the equal-to-threshold section. 
		for (int i=loSortedEqual ; i<=hiSortedEqual ; i++) { 
			numbers[i] = threshold ; 
		}
		//////  Make recursive calls. 
		// Bottom-of-recursion is detected at the start of the method. 
		semiSort(numbers, loSortedLess, hiSortedLess, soughtIndices);
		semiSort(numbers, loSortedGreater, hiSortedGreater, soughtIndices);
	}
	
	private boolean containsSoughtIndex(int loIndex, int hiIndex, int[] soughtIndices) { 
		for (int i=0 ; i<soughtIndices.length ; i++) { 
			int soughtIndex = soughtIndices[i] ; 
			if (loIndex<=soughtIndex) return soughtIndex<=hiIndex ; 
		}
		return false ; 
	}

	private static double getRoughMedian(double [] numbers, int loIndex, int hiIndex) { 
		double lowGuess = numbers[loIndex] ; 
		double highGuess = numbers[loIndex+1] ; 
		if (highGuess<lowGuess) { 
			double tmp = lowGuess ; 
			lowGuess = highGuess ; 
			highGuess = tmp ; 
		}
		for (int i=loIndex+2 ; i<hiIndex ; i++) { 
			double number = numbers[i] ; 
			if (number*2>lowGuess+highGuess) { 
				highGuess = number ; 
			} else { 
				lowGuess = number ; 
			}
		}
		return (lowGuess+highGuess) * 0.5 ; 
	}
	
	public static double getMin(double[] numbers) { 
		if (numbers==null) throw new NullPointerException(); 
		if (numbers.length==0) throw new IllegalArgumentException("Empty array"); 
		double min = numbers[0] ; 
		for (double x : numbers) if (x<min) min = x ; 
		return min ; 
	}
	
	public static double getMax(double[] numbers) { 
		if (numbers==null) throw new NullPointerException(); 
		if (numbers.length==0) throw new IllegalArgumentException("Empty array"); 
		double max = numbers[0] ; 
		for (double x : numbers) if (x>max) max = x ; 
		return max ; 
	}
	
	public static double [] getRange(double[] numbers) { 
		if (numbers==null) throw new NullPointerException(); 
		if (numbers.length==0) throw new IllegalArgumentException("Empty array"); 
		double min = numbers[0];
		double max = min ; 
		for (double x : numbers) { 
			if (x>max) max = x ; 
			else if (x<min) min = x ; 
		}
		return new double[] {min, max} ; 
	}
	
//	public static double getMin_OLD(double[] numbers) { 
//		if (numbers==null) throw new NullPointerException(); 
//		if (numbers.length==0) throw new IllegalArgumentException("Empty array"); 
//		int[] soughtIndices = new int[]{0} ; 
//		MediansDouble medians = new MediansDouble(numbers, numbers.length, soughtIndices); 
//		return medians.soughtMedians[0] ; 
//	}
//	
//	public static double getMax_OLD(double[] numbers) { 
//		if (numbers==null) throw new NullPointerException(); 
//		if (numbers.length==0) throw new IllegalArgumentException("Empty array"); 
//		int length = numbers.length ; 
//		int[] soughtIndices = new int[]{length-1} ; 
//		MediansDouble medians = new MediansDouble(numbers, numbers.length, soughtIndices); 
//		return medians.soughtMedians[0] ; 
//	}
	
	public static double getMedian(double[] numbers) { 
		if (numbers==null) throw new NullPointerException(); 
		if (numbers.length==0) throw new IllegalArgumentException("Empty array"); 
		int length = numbers.length ; 
		int[] soughtIndices = new int[]{length/2} ; 
		MediansDouble medians = new MediansDouble(numbers, soughtIndices); 
		return medians.soughtMedians[0] ; 
	}
	
	public static double getMedian(double [] numbers, double fraction) { 
		double[] medians = getMedians(numbers, new double[]{fraction}); 
		return medians[0] ; 
	}
	
	public static double [] getMedians(double [] numbers, double [] fractions) { 
		if (numbers==null) throw new NullPointerException(); 
		if (numbers.length==0) throw new IllegalArgumentException("Empty array"); 
		int numbersLength = numbers.length ; 
		int fractionsCount = fractions.length ; 
		int[] soughtIndices = new int[fractionsCount] ;
		for (int i=0 ; i<fractionsCount ; i++) { 
			soughtIndices[i] = (int) (numbersLength * fractions[i]) ; 
		}
		try { 
			MediansDouble medians = new MediansDouble(numbers, soughtIndices); 
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
	public static double[] getQuartilesMinMax(double[] numbers) { 
		if (numbers==null) throw new NullPointerException(); 
		if (numbers.length==0) throw new IllegalArgumentException("Empty array"); 
		int length = numbers.length ; 
		int[] soughtIndices = new int[5] ;
		soughtIndices[0] = 0 ; 
		soughtIndices[4] = length-1 ; 
		for (int i=1 ; i<4 ; i++) soughtIndices[i] = (i*length) / 4 ; 
//		for (int i=0 ; i<5 ; i++) soughtIndices[i] = (i*length) / 4 ; 
		MediansDouble medians = new MediansDouble(numbers, soughtIndices); 
		return medians.soughtMedians ; 
	}
	
//	private static class MinTest { 
//		MinTest(double[] numbers, double desiredResult) { 
//			double actualResult = getMin(numbers); 
//			if (actualResult!=desiredResult) throw new RuntimeException(); 
//		}
//	}
//	
//	private static class MaxTest { 
//		MaxTest(double[] numbers, double desiredResult) { 
//			double actualResult = getMax(numbers); 
//			if (actualResult!=desiredResult) throw new RuntimeException(); 
//		}
//	}
	
	private static class MinMaxTest { 
		MinMaxTest(double[] numbers, double desiredMin, double desiredMax) { 
			double actualMin = getMin(numbers); 
			if (actualMin!=desiredMin) throw new RuntimeException("Expected min "+desiredMin+", actual "+actualMin); 
			double actualMax = getMax(numbers); 
			if (actualMax!=desiredMax) throw new RuntimeException("Expected max "+desiredMax+", actual "+actualMax); 
			double [] actualRange = getRange(numbers); 
			if (actualRange[0]!=desiredMin||actualRange[1]!=desiredMax) throw new RuntimeException("Expected min,max "+desiredMax+","+desiredMax+", actual "+actualRange[0]+","+actualRange[1]); 
		}
	}
	
	private static class MedianTest { 
		MedianTest(double[] numbers, double desiredResult) { 
			double actualResult = getMedian(numbers); 
			if (actualResult!=desiredResult) throw new RuntimeException(); 
		}
	}
	
	private static class QuartilesMinMaxTest { 
		QuartilesMinMaxTest(double[] numbers, double[] desiredResults) { 
			if (desiredResults.length!=5) throw new RuntimeException("Error in test: desired array has length "+desiredResults.length); 
			//Log.v("TEST", "TESTING  numbers "+StringMethods.the(numbers)+", desired "+StringMethods.the(desiredResults)); 
			double[] actualResults = getQuartilesMinMax(numbers);
			if (!Arrays.equals(actualResults, desiredResults)) throw new RuntimeException("Actual "+Str.the(actualResults)+", desired "+Str.the(desiredResults)); 
		}
	}
	
	public static class ClassTest { 
		public ClassTest() { 
			///////  Tests copied from MediansInt 
			new MinMaxTest(new double[]{5}, 5, 5); 
			new MinMaxTest(new double[]{5, 5}, 5, 5); 
			new MinMaxTest(new double[]{5, 5, 5}, 5, 5); 
			new MinMaxTest(new double[]{6, 5, 7}, 5, 7); 
			new MinMaxTest(new double[]{6, 6, 9, 5, 8, 6, 9, 5, 7}, 5, 9); 
			new MinMaxTest(new double[]{1, 2, 2, 2}, 1, 2); 
			new MinMaxTest(new double[]{2, 2, 2, 1}, 1, 2); 
			new MinMaxTest(new double[]{3, 2, 2, 2}, 2, 3); 
			new MinMaxTest(new double[]{2, 2, 2, 3}, 2, 3); 
//			new MaxTest(new double[]{5}, 5); 
//			new MaxTest(new double[]{5, 5}, 5); 
//			new MaxTest(new double[]{5, 5, 5}, 5); 
//			new MaxTest(new double[]{6, 8, 7}, 8); 
//			new MaxTest(new double[]{6, 6, 9, 5, 8, 6, 9, 5, 7}, 9); 
			new MedianTest(new double[]{5}, 5); 
			new MedianTest(new double[]{5, 5}, 5); 
			new MedianTest(new double[]{5, 5, 5}, 5); 
			new MedianTest(new double[]{6, 8, 7}, 7); 
			new MedianTest(new double[]{6, 6, 9, 5, 8, 6, 9, 5, 7}, 6); 
			new MedianTest(new double[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16}, 8); 
			new MedianTest(new double[]{16, 15, 14, 9, 8, 7, 6, 5, 4, 13, 12, 11, 10, 3, 2, 1, 0}, 8); 
			new MedianTest(new double[]{16, 15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0}, 8); 
			new MedianTest(new double[]{3, 8, 9, 14, 4, 10, 11, 2, 16, 12, 13, 5, 6, 7, 15, 0, 1}, 8); 
			new QuartilesMinMaxTest(new double[]{0, 1, 2, 3, 4}, new double[]{0, 1, 2, 3, 4}); 
			new QuartilesMinMaxTest(new double[]{0, 0, 1, 1, 2, 3, 3, 4, 4}, new double[]{0, 1, 2, 3, 4}); 
			new QuartilesMinMaxTest(new double[]{5}, new double[]{5, 5, 5, 5, 5}); // Singleton data set 
			new QuartilesMinMaxTest(new double[]{5, 5, 5, 5}, new double[]{5, 5, 5, 5, 5}); // Uniform data set
			new QuartilesMinMaxTest(new double[]{5, 6}, new double[]{5, 5, 6, 6, 6}); 
			new QuartilesMinMaxTest(new double[]{6, 5}, new double[]{5, 5, 6, 6, 6}); 
			new QuartilesMinMaxTest(new double[]{10, 11, 12, 13, 14, 13, 12, 11, 10}, new double[]{10, 11, 12, 13, 14}); 
			new QuartilesMinMaxTest(new double[]{2, 2, 2, 2, 2, 2, 3, 3, 4, 4, 4}, new double[]{2, 2, 2, 4, 4}); 
			new QuartilesMinMaxTest(new double[]{2, 4, 2, 3, 2, 4, 2, 3, 2, 4, 2}, new double[]{2, 2, 2, 4, 4}); 
			new QuartilesMinMaxTest(new double[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16}, new double[]{0, 4, 8, 12, 16}); 
			new QuartilesMinMaxTest(new double[]{16, 15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0}, new double[]{0, 4, 8, 12, 16}); 
			new QuartilesMinMaxTest(new double[]{3, 8, 9, 14, 4, 10, 11, 2, 16, 12, 13, 5, 6, 7, 15, 0, 1}, new double[]{0, 4, 8, 12, 16}); 
			new QuartilesMinMaxTest(new double[]{16, 15, 14, 9, 8, 7, 6, 5, 4, 13, 12, 11, 10, 3, 2, 1, 0}, new double[]{0, 4, 8, 12, 16}); 
			//////  Passed! Bye bye.  
			System.out.println("Passed test suite "+this.getClass().getCanonicalName()); 
		}
	}
	
	private static double [] makePrecisionChallengingArray(double centre, double delta) { 
		double min = centre - delta ; 
		double max = centre + delta ; 
		ArrayList<Double> list = new ArrayList<Double>(); 
		list.add(min); 
		buildList(list, min, max); 
		list.add(max); 
		return Util.toArray_double(list); 
	}

	private static void buildList(ArrayList<Double> list, double min, double max) { 
		if (min<=0) throw new RuntimeException("'min' must be positive"); 
		if (min>=max) throw new RuntimeException("'min' must be less than 'max'"); 
		double mid = (min+max) * 0.5 ; 
		boolean wantRecursion = min<mid && mid<max ;  
		if (wantRecursion) buildList(list, min, mid);
		list.add(mid); 
		if (wantRecursion) buildList(list, mid, max);
	}
	
	public static void main(String [] args) { 
		double [] xx = makePrecisionChallengingArray(1.0, 1e-15); 
		for (double x : xx) System.out.println(x); 
	}

}
