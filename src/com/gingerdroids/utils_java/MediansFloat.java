package com.gingerdroids.utils_java;

import java.util.Arrays;

/**
 * Finds medians in an array. 
 * <p>
 * Does not make especial efficient use of already sorted arrays. 
 */
public class MediansFloat { 
	
	public final float[] soughtMedians ; 
	
	/**
	 * Finds the given "medians". 
	 * That is, if the array were sorted, which of <code>numbers</code> would be at each of the <code>soughtIndices</code>.
	 * <p>
	 * WARNING: This shuffles the content of the <code>numbers</code> argument. 
	 *  
	 * @param numbers Must not be zero-length. 
	 * @param soughtIndices Must not be zero-length. 
	 */
	private MediansFloat(float[] numbers, int[] soughtIndices) { 
		int soughtLength = soughtIndices.length ; 
		//////  Shuffle 'numbers' so the sought-values are in their correct place. 
		semiSort(numbers, 0, numbers.length-1, soughtIndices); 
		//////  Extract the sought-medians
		this.soughtMedians = new float[soughtLength] ; 
		for (int i=0 ; i<soughtLength ; i++) { 
			soughtMedians[i] = numbers[soughtIndices[i]] ; 
		}
	}
	
	private void semiSort(float[] numbers, int loIndex, int hiIndex, int[] soughtIndices) { 
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
		float threshold = getRoughMedian(numbers, loIndex, hiIndex); 
		/* Here we know: 'threshold' either is a value in 'numbers', or lies between two values. */ 
		//// Shrink unsorted-section, moving into sorted-sections. 
		while (loUnsorted<=hiUnsorted) { 
			float currentValue = numbers[loUnsorted] ; 
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
		
	private static float getRoughMedian(float [] numbers, int loIndex, int hiIndex) { 
		float lowGuess = numbers[loIndex] ; 
		float highGuess = numbers[loIndex+1] ; 
		if (highGuess<lowGuess) { 
			float tmp = lowGuess ; 
			lowGuess = highGuess ; 
			highGuess = tmp ; 
		}
		for (int i=loIndex+2 ; i<hiIndex ; i++) { 
			float number = numbers[i] ; 
			if (number*2>lowGuess+highGuess) { 
				highGuess = number ; 
			} else { 
				lowGuess = number ; 
			}
		}
		return (lowGuess+highGuess) * 0.5f ; 
	}
	
	public static float getMin(float[] numbers) { 
		if (numbers==null) throw new NullPointerException(); 
		if (numbers.length==0) throw new IllegalArgumentException("Empty array"); 
		float min = numbers[0] ; 
		for (float x : numbers) if (x<min) min = x ; 
		return min ; 
	}
	
	public static float getMax(float[] numbers) { 
		if (numbers==null) throw new NullPointerException(); 
		if (numbers.length==0) throw new IllegalArgumentException("Empty array"); 
		float max = numbers[0] ; 
		for (float x : numbers) if (x>max) max = x ; 
		return max ; 
	}
	
	public static float [] getRange(float[] numbers) { 
		if (numbers==null) throw new NullPointerException(); 
		if (numbers.length==0) throw new IllegalArgumentException("Empty array"); 
		float min = numbers[0];
		float max = min ; 
		for (float x : numbers) { 
			if (x>max) max = x ; 
			else if (x<min) min = x ; 
		}
		return new float[] {min, max} ; 
	}
	
	public static float getMedian(float[] numbers) { 
		if (numbers==null) throw new NullPointerException(); 
		if (numbers.length==0) throw new IllegalArgumentException("Empty array"); 
		int length = numbers.length ; 
		int[] soughtIndices = new int[]{length/2} ; 
		MediansFloat medians = new MediansFloat(numbers, soughtIndices); 
		return medians.soughtMedians[0] ; 
	}
	
	public static float getMedian(float [] numbers, double fraction) { 
		float[] medians = getMedians(numbers, new double[]{fraction}); 
		return medians[0] ; 
	}
	
	public static float [] getMedians(float [] numbers, double [] fractions) { 
		if (numbers==null) throw new NullPointerException(); 
		if (numbers.length==0) throw new IllegalArgumentException("Empty array"); 
		int numbersLength = numbers.length ; 
		int fractionsCount = fractions.length ; 
		int[] soughtIndices = new int[fractionsCount] ;
		for (int i=0 ; i<fractionsCount ; i++) { 
			soughtIndices[i] = (int) (numbersLength * fractions[i]) ; 
		}
		try { 
			MediansFloat medians = new MediansFloat(numbers, soughtIndices); 
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
	public static float[] getQuartilesMinMax(float[] numbers) { 
		if (numbers==null) throw new NullPointerException(); 
		if (numbers.length==0) throw new IllegalArgumentException("Empty array"); 
		int length = numbers.length ; 
		int[] soughtIndices = new int[5] ;
		soughtIndices[0] = 0 ; 
		soughtIndices[4] = length-1 ; 
		for (int i=1 ; i<4 ; i++) soughtIndices[i] = (i*length) / 4 ; 
//		for (int i=0 ; i<5 ; i++) soughtIndices[i] = (i*length) / 4 ; 
		MediansFloat medians = new MediansFloat(numbers, soughtIndices); 
		return medians.soughtMedians ; 
	}
	
	private static class MinMaxTest { 
		MinMaxTest(float[] numbers, float desiredMin, float desiredMax) { 
			float actualMin = getMin(numbers); 
			if (actualMin!=desiredMin) throw new RuntimeException("Expected min "+desiredMin+", actual "+actualMin); 
			float actualMax = getMax(numbers); 
			if (actualMax!=desiredMax) throw new RuntimeException("Expected max "+desiredMax+", actual "+actualMax); 
			float [] actualRange = getRange(numbers); 
			if (actualRange[0]!=desiredMin||actualRange[1]!=desiredMax) throw new RuntimeException("Expected min,max "+desiredMax+","+desiredMax+", actual "+actualRange[0]+","+actualRange[1]); 
		}
	}
	
//	private static class MinTest { 
//		MinTest(float[] numbers, float desiredResult) { 
//			float actualResult = getMin(numbers); 
//			if (actualResult!=desiredResult) throw new RuntimeException("Expected "+desiredResult+", actual "+actualResult); 
//		}
//	}
//	
//	private static class MaxTest { 
//		MaxTest(float[] numbers, float desiredResult) { 
//			float actualResult = getMax(numbers); 
//			if (actualResult!=desiredResult) throw new RuntimeException("Expected "+desiredResult+", actual "+actualResult); 
//		}
//	}
	
	private static class MedianTest { 
		MedianTest(float[] numbers, float desiredResult) { 
			float actualResult = getMedian(numbers); 
			if (actualResult!=desiredResult) throw new RuntimeException("Expected "+desiredResult+", actual "+actualResult); 
		}
	}
	
	private static class QuartilesMinMaxTest { 
		QuartilesMinMaxTest(float[] numbers, float[] desiredResults) { 
			if (desiredResults.length!=5) throw new RuntimeException("Error in test: desired array has length "+desiredResults.length); 
			//Log.v("TEST", "TESTING  numbers "+StringMethods.the(numbers)+", desired "+StringMethods.the(desiredResults)); 
			float[] actualResults = getQuartilesMinMax(numbers);
			if (!Arrays.equals(actualResults, desiredResults)) throw new RuntimeException("Actual "+Str.the(actualResults)+", desired "+Str.the(desiredResults)); 
		}
	}
	
	public static class ClassTest { 
		public ClassTest() { 
			new MinMaxTest(new float[]{5}, 5, 5); 
			new MinMaxTest(new float[]{5, 5}, 5, 5); 
			new MinMaxTest(new float[]{5, 5, 5}, 5, 5); 
			new MinMaxTest(new float[]{6, 5, 7}, 5, 7); 
			new MinMaxTest(new float[]{6, 6, 9, 5, 8, 6, 9, 5, 7}, 5, 9); 
			new MinMaxTest(new float[]{1, 2, 2, 2}, 1, 2); 
			new MinMaxTest(new float[]{2, 2, 2, 1}, 1, 2); 
			new MinMaxTest(new float[]{3, 2, 2, 2}, 2, 3); 
			new MinMaxTest(new float[]{2, 2, 2, 3}, 2, 3); 
//			new MaxTest(new float[]{5}, 5); 
//			new MaxTest(new float[]{5, 5}, 5); 
//			new MaxTest(new float[]{5, 5, 5}, 5); 
//			new MaxTest(new float[]{6, 8, 7}, 8); 
//			new MaxTest(new float[]{6, 6, 9, 5, 8, 6, 9, 5, 7}, 9); 
			new MedianTest(new float[]{5}, 5); 
			new MedianTest(new float[]{5, 5}, 5); 
			new MedianTest(new float[]{5, 5, 5}, 5); 
			new MedianTest(new float[]{6, 8, 7}, 7); 
			new MedianTest(new float[]{6, 6, 9, 5, 8, 6, 9, 5, 7}, 6); 
			new MedianTest(new float[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16}, 8); 
			new MedianTest(new float[]{16, 15, 14, 9, 8, 7, 6, 5, 4, 13, 12, 11, 10, 3, 2, 1, 0}, 8); 
			new MedianTest(new float[]{16, 15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0}, 8); 
			new MedianTest(new float[]{3, 8, 9, 14, 4, 10, 11, 2, 16, 12, 13, 5, 6, 7, 15, 0, 1}, 8); 
			new QuartilesMinMaxTest(new float[]{0, 1, 2, 3, 4}, new float[]{0, 1, 2, 3, 4}); 
			new QuartilesMinMaxTest(new float[]{0, 0, 1, 1, 2, 3, 3, 4, 4}, new float[]{0, 1, 2, 3, 4}); 
			new QuartilesMinMaxTest(new float[]{5}, new float[]{5, 5, 5, 5, 5}); // Singleton data set 
			new QuartilesMinMaxTest(new float[]{5, 5, 5, 5}, new float[]{5, 5, 5, 5, 5}); // Uniform data set
			new QuartilesMinMaxTest(new float[]{5, 6}, new float[]{5, 5, 6, 6, 6}); 
			new QuartilesMinMaxTest(new float[]{6, 5}, new float[]{5, 5, 6, 6, 6}); 
			new QuartilesMinMaxTest(new float[]{10, 11, 12, 13, 14, 13, 12, 11, 10}, new float[]{10, 11, 12, 13, 14}); 
			new QuartilesMinMaxTest(new float[]{2, 2, 2, 2, 2, 2, 3, 3, 4, 4, 4}, new float[]{2, 2, 2, 4, 4}); 
			new QuartilesMinMaxTest(new float[]{2, 4, 2, 3, 2, 4, 2, 3, 2, 4, 2}, new float[]{2, 2, 2, 4, 4}); 
			new QuartilesMinMaxTest(new float[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16}, new float[]{0, 4, 8, 12, 16}); 
			new QuartilesMinMaxTest(new float[]{16, 15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0}, new float[]{0, 4, 8, 12, 16}); 
			new QuartilesMinMaxTest(new float[]{3, 8, 9, 14, 4, 10, 11, 2, 16, 12, 13, 5, 6, 7, 15, 0, 1}, new float[]{0, 4, 8, 12, 16}); 
			new QuartilesMinMaxTest(new float[]{16, 15, 14, 9, 8, 7, 6, 5, 4, 13, 12, 11, 10, 3, 2, 1, 0}, new float[]{0, 4, 8, 12, 16}); 
			//////  Passed! Bye bye.  
			System.out.println("Passed test suite "+this.getClass().getCanonicalName()); 
		}
	}

}
