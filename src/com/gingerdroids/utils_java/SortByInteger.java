package com.gingerdroids.utils_java;

import java.util.Arrays;
import java.util.Random;

/**
 * Sorts an array of objects (<code>bags</code>) by a separate array of ints (<code>measures</code>). 
 * It sorts into ascending order. 
 * <p>
 * The <code>results</code> array is always a new array. 
 * <p>
 * The algorithm used:<ul>
 * <li> Is a stable-sort! That is, bags with equal measure are in the same order in the output as they were in the input. jan19 - This is not yet coded. 
 * <li> Efficiently exploits already ordered subsequences. 
 * <li> Requires the allocation of twelve arrays: four of the baggage type, four of doubles, four of ints (original poistion), each the length of the original data. 
 * </ul>
 * <p>
 * A <code>NaN</code> in the <code>measures</code> - I haven't thought about that, and don't want to. 
 * <p>
 * There is test code at the end of the class. 
 */
public class SortByInteger { 
	
	public final Object [] results ; 
	
	public final int [] sortedMeasures ; 
	
	protected final int length ; 
	
	public SortByInteger(Object[] bags, int[] measures) { 
		this.length = bags.length; 
		if (bags.length!=measures.length) throw new IllegalArgumentException("Mismatched lengths: payload array "+bags.length+", measures array "+measures.length); 
		this.results = new Object[length]; 
		this.sortedMeasures = new int[length]; 
		Object [] bagsA = new Object[length] ; 
		Object [] bagsB = new Object[length] ; 
		Object [] bagsC = new Object[length] ; 
		Object [] bagsD = new Object[length] ; 
		int [] measuresA = new int[length] ; 
		int [] measuresB = new int[length] ; 
		int [] measuresC = new int[length] ; 
		int [] measuresD = new int[length] ; 
		int [] positionsA = new int[length] ; 
		int [] positionsB = new int[length] ; 
		int [] positionsC = new int[length] ; 
		int [] positionsD = new int[length] ; 
		System.arraycopy(bags, 0, bagsA, 0, length); 
		System.arraycopy(measures, 0, measuresA, 0, length); 
		for (int i=0 ; i<length ; i++) positionsA[i] = i ; 
//		printArray(length, measuresA, positionsA); System.out.println(); 
		sort(length, 0, bagsA, bagsB, bagsC, bagsD, measuresA, measuresB, measuresC, measuresD, positionsA, positionsB, positionsC, positionsD); 
//		printArray(length, measuresA, positionsA); System.out.println(); 
	}
	
	public SortByInteger(Object[] bags, IntGetter measure) { 
		this(bags, measureArray(bags, measure));
	} 
	
	private void sort(int inLengthA, int inLengthB, Object[] inBagsA, Object[] inBagsB, Object[] outBagsC, Object[] outBagsD, int[] inMeasuresA, int[] inMeasuresB, int[] outMeasuresC, int[] outMeasuresD, int[] inPositionsA, int[] inPositionsB, int[] outPositionsC, int[] outPositionsD) { 
		while (true) { 
			//////  Copy bags and measures from A&B to C&D, making the sorted-runs longer. 
			int outLengthC = 0 ; 
			int outLengthD = 0 ; 
			int cursorA = 0 ; 
			int cursorB = 0 ; 
			boolean toC = true ; 
			while(outLengthC+outLengthD<length) { 
				boolean fromA ; 
				if (cursorA>=inLengthA) { 
					fromA = false ; 
				} else if (cursorB>=inLengthB) { 
					fromA = true ; 
				} else { 
					int nextMeasureA = inMeasuresA[cursorA];
					int nextMeasureB = inMeasuresB[cursorB];
					int nextPositionA = inPositionsA[cursorA];
					int nextPositionB = inPositionsB[cursorB];
					int lastC = outLengthC>0 ? outMeasuresC[outLengthC-1] : Integer.MIN_VALUE ; 
					int lastD = outLengthD>0 ? outMeasuresD[outLengthD-1] : Integer.MIN_VALUE ; 
					if (toC) { 
						/* We're hoping to add to array C. */
						if (lastC<=nextMeasureA) { 
							if (lastC<=nextMeasureB) { 
								/* Either A or B could be used, choose the least. */
								fromA = nextMeasureA <= nextMeasureB ;  
								if (nextMeasureA==nextMeasureB) fromA = nextPositionA < nextPositionB ; // Positions can't be equal. 
							} else { 
								/* A can be used, but not B. */
								fromA = true ; 
							}
						} else { 
							if (lastC<=nextMeasureB) { 
								/* B can be used, but not A. */
								fromA = false ; 
							} else { 
								/* Neither can be used. Append lesser to D. */
								fromA = nextMeasureA <= nextMeasureB ;  
								toC = false ; 
							}
						}
					} else { 
						/* We're hoping to add to array D. */
						if (lastD<=nextMeasureA) { 
							if (lastD<=nextMeasureB) { 
								/* Either A or B could be used, choose the least. */
								fromA = nextMeasureA <= nextMeasureB ; 
								if (nextMeasureA==nextMeasureB) fromA = nextPositionA < nextPositionB ; // Positions can't be equal. 
							} else { 
								/* A can be used, but not B. */
								fromA = true ; 
							}
						} else { 
							if (lastD<=nextMeasureB) { 
								/* B can be used, but not A. */
								fromA = false ; 
							} else { 
								/* Neither can be used. Append lesser to C. */
								fromA = nextMeasureA <= nextMeasureB ;  
								toC = true ; 
							}
						}
					}
				} 
				int tmpMeasure = fromA ? inMeasuresA[cursorA] : inMeasuresB[cursorB] ; 
				int tmpPosition = fromA ? inPositionsA[cursorA] : inPositionsB[cursorB] ; 
				Object tmpBag = fromA ? inBagsA[cursorA] : inBagsB[cursorB] ; 
				if (fromA) cursorA ++ ; else cursorB ++ ; 
				if (toC) { 
					if (canExtendRun(outLengthC, outMeasuresC, outPositionsC, tmpMeasure, tmpPosition)) { 
						outMeasuresC[outLengthC] = tmpMeasure ; 
						outPositionsC[outLengthC] = tmpPosition ; 
						outBagsC[outLengthC] = tmpBag ; 
						outLengthC ++ ; 
					} else { 
						toC = false ; 
						outMeasuresD[outLengthD] = tmpMeasure ; 
						outPositionsD[outLengthD] = tmpPosition ; 
						outBagsD[outLengthD] = tmpBag ; 
						outLengthD ++ ; 
					}
				} else { 
					if (canExtendRun(outLengthD, outMeasuresD, outPositionsD, tmpMeasure, tmpPosition)) { 
						outMeasuresD[outLengthD] = tmpMeasure ; 
						outPositionsD[outLengthD] = tmpPosition ; 
						outBagsD[outLengthD] = tmpBag ; 
						outLengthD ++ ; 
					} else { 
						toC = true ; 
						outMeasuresC[outLengthC] = tmpMeasure ; 
						outPositionsC[outLengthC] = tmpPosition ; 
						outBagsC[outLengthC] = tmpBag ; 
						outLengthC ++ ; 
					}
				}
			}
			//////  If finished, bye bye 
			if (outLengthC==length) { 
				System.arraycopy(outBagsC, 0, results, 0, length); 
				System.arraycopy(outMeasuresC, 0, sortedMeasures, 0, length); 
				break ; 
			} 
			//////  Swap the source and destination arrays, for the next iteration. 
			inLengthA = outLengthC ; 
			inLengthB = outLengthD ; 
			Object [] tmpBags ; 
			tmpBags = inBagsA ; inBagsA = outBagsC ; outBagsC = tmpBags ; 
			tmpBags = inBagsB ; inBagsB = outBagsD ; outBagsD = tmpBags ; 
			int [] tmpMeasures ; 
			tmpMeasures = inMeasuresA ; inMeasuresA = outMeasuresC ; outMeasuresC = tmpMeasures ; 
			tmpMeasures = inMeasuresB ; inMeasuresB = outMeasuresD ; outMeasuresD = tmpMeasures ; 
			int [] tmpPositions ; 
			tmpPositions = inPositionsA ; inPositionsA = outPositionsC ; outPositionsC = tmpPositions ; 
			tmpPositions = inPositionsB ; inPositionsB = outPositionsD ; outPositionsD = tmpPositions ; 
		}
	}
	
	void printArray(int count, int[] measures, int[] positions) { 
		System.out.print(count); 
		for (int i=0 ; i<count ; i++) { 
			System.out.print("\t"+(char)('a'+positions[i])+(int)measures[i]);
		}
		System.out.println(); 
	}

	public boolean canExtendRun(int outLength, int[] outMeasures, int[] outPositions, int tmpMeasure, int tmpPosition) {
		if (outLength==0) return true ; 
		if (outMeasures[outLength-1]<tmpMeasure) return true ; 
		if (outMeasures[outLength-1]>tmpMeasure) return false ; 
		if (outPositions[outLength-1]<tmpPosition) return true ; // Positions cannot be equal. 
		return false ; 
	}
	
	/**
	 * Subclass to sort strings, with a result object <code>sortedStrings</code> which is of a useful type. 
	 * <p>
	 * This is useful as a cut&paste template for client types. 
	 */
	public static class Strings extends SortByInteger { 
		
		public final String [] sortedStrings ; 

		public Strings(String[] in, int[] measures) {
			super(in, measures);
			this.sortedStrings = new String[results.length]; 
			for (int i=0 ; i<results.length ; i++) sortedStrings[i] = (String) results[i] ; 
		} 
	}
	
	public static int [] measureArray(Object[] bags, IntGetter measure) { 
		int count = bags.length ; 
		int [] array = new int[count] ; 
		for (int i=0 ; i<count ; i++) { 
			array[i] = measure.getInt(bags[i]); 
		}
		return array ; 
	}
	
	/**
	 * Tests sorting - assumes there are no duplicates among the measures. 
	 */
	private static class NoDuplicatesTest { 
		private NoDuplicatesTest(String[] shuffledStrings, int[] shuffledMeasures, String[] expectedStrings) { 
			SortByInteger.Strings sorter = new SortByInteger.Strings(shuffledStrings, shuffledMeasures); 
			if (!Arrays.equals(expectedStrings, sorter.sortedStrings)) throw new RuntimeException("Test failed"); 
		}
	}
	
	private static class MultiseedNoDuplicatesTest { 
		private MultiseedNoDuplicatesTest(String[] orderedStrings, int[] orderedMeasures, int[] seeds) { 
			int length = orderedStrings.length;
			for (int seed : seeds) { 
				//// Shuffle the strings and measures
				Random random = new Random(seed); 
				int [] shuffleIndices = new int[length] ; 
				for (int i=0 ; i<length ; i++) shuffleIndices[i] = i ; 
				for (int i=1 ; i<length ; i++) { 
					int j = random.nextInt(i+1); // 'j' is in the range 0..i, bounds inclusive. 
					int tmp = shuffleIndices[i]; 
					shuffleIndices[i] = shuffleIndices[j] ; 
					shuffleIndices[j] = tmp ; 
				}
				String[] shuffledStrings = new String[length]; 
				int[] shuffledMeasures = new int[length]; 
				for (int i=0 ; i<length ; i++) { 
					shuffledStrings[shuffleIndices[i]] = orderedStrings[i] ; 
					shuffledMeasures[shuffleIndices[i]] = orderedMeasures[i] ; 
				}
				//// Run the test with shuffled strings and measures
				new NoDuplicatesTest(shuffledStrings, shuffledMeasures, orderedStrings); 
			}
		}
	}
	
	private static class MultilengthMultiseedNoDuplicatesTest { 
		MultilengthMultiseedNoDuplicatesTest(int[] lengths, int[] seeds) { 
			for (int i=0 ; i<lengths.length ; i++) { 
				int length = lengths[i] ; 
				String[] orderedStrings = new String[length] ; 
				int[] orderedMeasures = new int[length] ; 
				for (int j=0 ; j<length ; j++) { 
					orderedStrings[j] = Str.number(j); 
					orderedMeasures[j] = j ; 
				}
				new MultiseedNoDuplicatesTest(orderedStrings, orderedMeasures, seeds); 
			}
			
		}
	}
	
	private static class StableSortingTest { 
		public StableSortingTest(int length) {
			int [] measures = new int[length] ; 
			IntegerPair [] pairs = new IntegerPair[length] ; 
			for (int i=0 ; i<length ; i++) pairs[i] = new IntegerPair(Util.random.nextInt(length/2),  Util.random.nextInt(length/2)); 
			for (int i=0 ; i<length ; i++) measures[i] = pairs[i].a ; 
			IntegerPair[] sortedPairs = new SortPairs(pairs, measures).sortedPairs; 
			for (int i=1 ; i<length ; i++) { 
				IntegerPair prev = sortedPairs[i-1] ; 
				IntegerPair here = sortedPairs[i] ; 
				if (prev.a<here.a) continue ; 
				if (prev.a==here.a) continue ; 
				throw new RuntimeException("BUG!"); 
			}
			for (int i=0 ; i<length ; i++) measures[i] = sortedPairs[i].b ; 
			sortedPairs = new SortPairs(sortedPairs, measures).sortedPairs; 
			for (int i=1 ; i<length ; i++) { 
				IntegerPair prev = sortedPairs[i-1] ; 
				IntegerPair here = sortedPairs[i] ; 
				if (prev.b<here.b) continue ; 
				if (prev.b==here.b && prev.a<=here.a) continue ; 
				throw new RuntimeException("BUG!"); 
			}
		}
		
		private static class IntegerPair { 
			final int a ; 
			final int b ; 
			public IntegerPair(int a, int b) {
				this.a = a ; 
				this.b = b ; 
			}
		}
		
		private class SortPairs extends SortByInteger {

			private final IntegerPair[] sortedPairs;

			public SortPairs(IntegerPair[] pairs, int[] measures) {
				super(pairs, measures);
				int length = pairs.length ; 
				this.sortedPairs = new IntegerPair[length] ; 
				for (int i=0 ; i<length ; i++) sortedPairs[i] = (IntegerPair) results[i] ; 
			}
			
		}
	}
	
	private static class DuplicatesTest { 
		DuplicatesTest(int blocksCount, int repeatCount) { 
			int length = repeatCount * blocksCount ; 
			//// Build ordered arrays 
			int [] orderedMeasures = new int[length] ; 
			double [] orderedBags = new double[length] ; 
			int index = 0 ; 
			for (int blocksIndex=0 ; blocksIndex<blocksCount ; blocksIndex++) { 
				int measure = blocksIndex ; 
				double bag = 1.0 / (blocksIndex+1) ; 
				for (int repeatIndex=0 ; repeatIndex<repeatCount ; repeatIndex++) { 
					orderedMeasures[index] = measure ; 
					orderedBags[index] = bag ; 
					index ++ ; 
				}
			}
			//// Shuffle the strings and measures 
			Random random = new Random(54643+blocksCount*1024+repeatCount); 
			int [] shuffleIndices = new int[length] ; 
			for (int i=0 ; i<length ; i++) shuffleIndices[i] = i ; 
			for (int i=1 ; i<length ; i++) { 
				int j = random.nextInt(i+1); // 'j' is in the range 0..i, bounds inclusive. 
				int tmp = shuffleIndices[i]; 
				shuffleIndices[i] = shuffleIndices[j] ; 
				shuffleIndices[j] = tmp ; 
			}
			Double[] shuffledBags = new Double[length]; 
			int[] shuffledMeasures = new int[length]; 
			for (int i=0 ; i<length ; i++) { 
				shuffledBags[shuffleIndices[i]] = orderedBags[i] ; 
				shuffledMeasures[shuffleIndices[i]] = orderedMeasures[i] ; 
			}
			//// Sort 
			SortByInteger sorter = new SortByInteger(shuffledBags, shuffledMeasures); 
			//// Check 
			Object [] sortedBagObjects = sorter.results ; 
			for (int i=1 ; i<length ; i++) { 
				double prev = (Double) sortedBagObjects[i-1] ; 
				double here = (Double) sortedBagObjects[i] ; 
				if (prev<here) throw new RuntimeException(); 
			}
			
		}
	}
	
	public static class ClassTest { 
		ClassTest() { 
			new MultilengthMultiseedNoDuplicatesTest(new int[]{0}, new int[]{8543, 45125}); 
			new MultilengthMultiseedNoDuplicatesTest(new int[]{1}, new int[]{8543, 45125}); 
			new MultilengthMultiseedNoDuplicatesTest(new int[]{2}, new int[]{8543, 45125, 4545, 785413}); 
			new MultilengthMultiseedNoDuplicatesTest(new int[]{3, 4, 5, 6, 7, 8, 9, 10}, new int[]{8543, 45125, 4545, 785413}); 
			new MultilengthMultiseedNoDuplicatesTest(new int[]{50, 100, 1000}, new int[]{474854, 43233}); 
			new DuplicatesTest(5, 4); 
			new DuplicatesTest(10, 10); 
			new DuplicatesTest(20, 20); 
			new StableSortingTest(8); 
			new StableSortingTest(16); 
			new StableSortingTest(64); 
			new StableSortingTest(256); 
			//////  Passed! Bye bye.  
			System.out.println("Passed test suite "+this.getClass().getCanonicalName()); 
		}
	}
	
	public static void main(String [] args) { 
		new SortByInteger.ClassTest(); 
	}

}
