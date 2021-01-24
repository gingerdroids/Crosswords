package com.gingerdroids.utils_java;

import java.io.File;
import java.util.Arrays;
import java.util.Random;

/**
 * Sorts an array of objects (<code>bags</code>) by a separate array of Strings (<code>measures</code>). 
 * Case is significant. 
 * It sorts into ascending order. 
 * <p>
 * The <code>results</code> array is always a new array. 
 * <p>
 * The algorithm used:<ul>
 * <li> Efficiently exploits already ordered subsequences. 
 * <li> Requires the allocation of eight arrays: four of the baggage type, four of doubles, each the length of the original data. 
 * <li> Is not a stable-sort! That is, bags with equal measure are not necessarily in the same order in the output as they were in the input. 
 * </ul>
 * <p>
 * A <code>null</code> in the <code>measures</code> - I haven't thought about that, and don't want to. 
 * <p>
 * There is test code at the end of the class. 
 */
public class SortByString { 
	
	public final Object [] results ; 
	
	public final String [] sortedMeasures ; 
	
	protected final int length ; 
	
	public SortByString(Object[] bags, String[] measures) { 
		this.length = bags.length; 
		if (bags.length!=measures.length) throw new IllegalArgumentException("Mismatched lengths: payload array "+bags.length+", measures array "+measures.length); 
		this.results = new Object[length]; 
		this.sortedMeasures = new String[length]; 
		Object [] bagsA = new Object[length] ; 
		Object [] bagsB = new Object[length] ; 
		Object [] bagsC = new Object[length] ; 
		Object [] bagsD = new Object[length] ; 
		String [] measuresA = new String[length] ; 
		String [] measuresB = new String[length] ; 
		String [] measuresC = new String[length] ; 
		String [] measuresD = new String[length] ; 
		int [] positionsA = new int[length] ; 
		int [] positionsB = new int[length] ; 
		int [] positionsC = new int[length] ; 
		int [] positionsD = new int[length] ; 
		System.arraycopy(bags, 0, bagsA, 0, length); 
		System.arraycopy(measures, 0, measuresA, 0, length); 
		for (int i=0 ; i<length ; i++) positionsA[i] = i ; 
		sort(length, 0, bagsA, bagsB, bagsC, bagsD, measuresA, measuresB, measuresC, measuresD, positionsA, positionsB, positionsC, positionsD); 
	}
	
	private void sort(int inLengthA, int inLengthB, Object[] inBagsA, Object[] inBagsB, Object[] outBagsC, Object[] outBagsD, String[] inMeasuresA, String[] inMeasuresB, String[] outMeasuresC, String[] outMeasuresD, int[] inPositionsA, int[] inPositionsB, int[] outPositionsC, int[] outPositionsD) { 
		while (true) { 
			//////  Copy bags and measures from A&B to C&D, making the sorted-runs longer. 
			int outLengthC = 0 ; 
			int outLengthD = 0 ; 
			int cursorA = 0 ; 
			int cursorB = 0 ; 
			boolean toC = true ; 
			while(outLengthC+outLengthD<length) { 
//				if (inLengthA==18) { 
//					System.out.println();
//				}
				boolean fromA ; 
				if (cursorA>=inLengthA) { 
					fromA = false ; 
				} else if (cursorB>=inLengthB) { 
					fromA = true ; 
				} else { 
					String nextMeasureA = inMeasuresA[cursorA];
					String nextMeasureB = inMeasuresB[cursorB];
					int nextPositionA = inPositionsA[cursorA];
					int nextPositionB = inPositionsB[cursorB];
					String lastC = outLengthC>0 ? outMeasuresC[outLengthC-1] : null ; // 'null' implies nothing in this out-list yet. 
					String lastD = outLengthD>0 ? outMeasuresD[outLengthD-1] : null ; 
					if (toC) { 
						/* We're hoping to add to array C. */
						if (lastC==null || lastC.compareTo(nextMeasureA)<=0) { 
							if (lastC==null || lastC.compareTo(nextMeasureB)<=0) { 
								/* Either A or B could be used, choose the least. */
								fromA = nextMeasureA.compareTo(nextMeasureB) <= 0 ;  
								if (nextMeasureA.equals(nextMeasureB)) fromA = nextPositionA < nextPositionB ; // Positions can't be equal. 
							} else { 
								/* A can be used, but not B. */
								fromA = true ; 
							}
						} else { 
							if (lastC==null || lastC.compareTo(nextMeasureB)<=0) { 
								/* B can be used, but not A. */
								fromA = false ; 
							} else { 
								/* Neither can be used. Append lesser to D. */
								fromA = nextMeasureA.compareTo(nextMeasureB) <= 0 ;  
								toC = false ; 
							}
						}
					} else { 
						/* We're hoping to add to array D. */
						if (lastD==null || lastD.compareTo(nextMeasureA)<=0) { 
							if (lastD==null || lastD.compareTo(nextMeasureB)<=0) { 
								/* Either A or B could be used, choose the least. */
								fromA = nextMeasureA.compareTo(nextMeasureB) <= 0 ; 
								if (nextMeasureA.equals(nextMeasureB)) fromA = nextPositionA < nextPositionB ; // Positions can't be equal. 
							} else { 
								/* A can be used, but not B. */
								fromA = true ; 
							}
						} else { 
							if (lastD==null || lastD.compareTo(nextMeasureB)<=0) { 
								/* B can be used, but not A. */
								fromA = false ; 
							} else { 
								/* Neither can be used. Append lesser to C. */
								fromA = nextMeasureA.compareTo(nextMeasureB) <= 0 ;  
								toC = true ; 
							}
						}
					}
				} 
				String tmpMeasure = fromA ? inMeasuresA[cursorA] : inMeasuresB[cursorB] ; 
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
			String [] tmpMeasures ; 
			tmpMeasures = inMeasuresA ; inMeasuresA = outMeasuresC ; outMeasuresC = tmpMeasures ; 
			tmpMeasures = inMeasuresB ; inMeasuresB = outMeasuresD ; outMeasuresD = tmpMeasures ; 
			int [] tmpPositions ; 
			tmpPositions = inPositionsA ; inPositionsA = outPositionsC ; outPositionsC = tmpPositions ; 
			tmpPositions = inPositionsB ; inPositionsB = outPositionsD ; outPositionsD = tmpPositions ; 
		}
	}

	void printOutMeasure(String label, String[] outMeasuresC, int outLengthC) {
		System.out.print(label+":\t"); 
		for (int i=0 ; i<outLengthC ; i++) System.out.print(" "+outMeasuresC[i]); 
		System.out.println();
	}

	private boolean canExtendRun(int outLength, String[] outMeasures, int[] outPositions, String tmpMeasure, int tmpPosition) {
		if (outLength==0) return true ; 
		if (outMeasures[outLength-1].compareTo(tmpMeasure)<0) return true ; 
		if (outMeasures[outLength-1].compareTo(tmpMeasure)>0) return false ; 
		if (outPositions[outLength-1]<tmpPosition) return true ; // Positions cannot be equal. 
		return false ; 
	}
	
	/**
	 * Subclass to sort strings, with a result object <code>sortedStrings</code> which is of a useful type. 
	 */
	public static class Strings extends SortByString { 
		
		public final String [] sortedStrings ; 

		public Strings(String[] in, String[] measures) {
			super(in, measures);
			this.sortedStrings = new String[results.length]; 
			for (int i=0 ; i<results.length ; i++) sortedStrings[i] = (String) results[i] ; 
		} 
		
	}
	
	/**
	 * Subclass to sort Files by the name (not path), with a result object <code>sortedFiles</code> which is of a useful type. 
	 */
	public static class FileNames extends SortByString { 
		
		public final File [] sortedFiles ; 

		public FileNames(File[] in) {
			super(in, toFilenames(in));
			this.sortedFiles = new File[results.length]; 
			for (int i=0 ; i<results.length ; i++) sortedFiles[i] = (File) results[i] ; 
		} 
		
	}
	
	public static String [] toFilenames(File[] files) { 
		int length = files.length;
		String [] names = new String[length] ; 
		for (int i=0 ; i<length ; i++) { 
			names[i] = files[i].getName(); 
		}
		return names ; 
	}
	
	/**
	 * Tests sorting - assumes there are no duplicates among the measures. 
	 */
	private static class NoDuplicatesTest { 
		private NoDuplicatesTest(String[] shuffledStrings, String[] shuffledMeasures, String[] expectedStrings) { 
			SortByString sorter = new SortByString(shuffledStrings, shuffledMeasures); 
			if (!Arrays.equals(expectedStrings, sorter.results)) throw new RuntimeException("Test failed"); 
		}
	}
	
	private static class MultiseedNoDuplicatesTest { 
		private MultiseedNoDuplicatesTest(String[] orderedStrings, String[] orderedMeasures, int[] seeds) { 
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
				String[] shuffledMeasures = new String[length]; 
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
				String[] orderedMeasures = new String[length] ; 
				for (int j=0 ; j<length ; j++) { 
					orderedStrings[j] = Str.number(j); 
					orderedMeasures[j] = Str.zeroPad_4(j); 
				}
				new MultiseedNoDuplicatesTest(orderedStrings, orderedMeasures, seeds); 
			}
			
		}
	}
	
	private static class StableSortingTest { 
		public StableSortingTest(int length) {
			if (length>=1000) throw new RuntimeException("oct19 Test relies on zero-padded numbers being in lexigraphical order."); 
			String [] measures = new String[length] ; 
			DoublePair [] pairs = new DoublePair[length] ; 
			for (int i=0 ; i<length ; i++) pairs[i] = new DoublePair(Util.random.nextInt(length/2),  Util.random.nextInt(length/2)); 
			for (int i=0 ; i<length ; i++) measures[i] = pairs[i].a ; 
			DoublePair[] sortedPairs = new SortPairs(pairs, measures).sortedPairs; 
			for (int i=1 ; i<length ; i++) { 
				DoublePair prev = sortedPairs[i-1] ; 
				DoublePair here = sortedPairs[i] ; 
				if (prev.a.compareTo(here.a)<=0) continue ; 
				throw new RuntimeException("BUG!"); 
			}
			for (int i=0 ; i<length ; i++) measures[i] = sortedPairs[i].b ; 
			sortedPairs = new SortPairs(sortedPairs, measures).sortedPairs; 
			for (int i=1 ; i<length ; i++) { 
				DoublePair prev = sortedPairs[i-1] ; 
				DoublePair here = sortedPairs[i] ; 
				if (prev.b.compareTo(here.b)<0) continue ; 
				if (prev.b.compareTo(here.b)==0 && prev.a.compareTo(here.a)<=0) continue ; 
				System.err.println("Prev is a,b:\t"+prev.a+",\t"+prev.b);
				System.err.println("Here is a,b:\t"+here.a+",\t"+here.b);
				throw new RuntimeException("BUG!"); 
			}
		}
		
		private static class DoublePair { 
			final String a ; 
			final String b ; 
			public DoublePair(int a, int b) {
				this.a = Str.zeroPad_4(a) ; 
				this.b = Str.zeroPad_4(b) ; 
			}
		}
		
		private class SortPairs extends SortByString{

			private final DoublePair[] sortedPairs;

			public SortPairs(DoublePair[] pairs, String[] measures) {
				super(pairs, measures);
				int length = pairs.length ; 
				this.sortedPairs = new DoublePair[length] ; 
				for (int i=0 ; i<length ; i++) sortedPairs[i] = (DoublePair) results[i] ; 
			}
			
		}
	}
	
	private static class DuplicatesTest { 
		DuplicatesTest(int blocksCount, int repeatCount) { 
			int length = repeatCount * blocksCount ; 
			//// Build ordered arrays 
			String [] orderedMeasures = new String[length] ; 
			double [] orderedBags = new double[length] ; 
			int index = 0 ; 
			for (int blocksIndex=0 ; blocksIndex<blocksCount ; blocksIndex++) { 
				int measure = blocksIndex ; 
				double bag = 1.0 / (blocksIndex+1) ; 
				for (int repeatIndex=0 ; repeatIndex<repeatCount ; repeatIndex++) { 
					orderedMeasures[index] = Str.zeroPad_4(measure) ; 
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
			String[] shuffledMeasures = new String[length]; 
			for (int i=0 ; i<length ; i++) { 
				shuffledBags[shuffleIndices[i]] = orderedBags[i] ; 
				shuffledMeasures[shuffleIndices[i]] = orderedMeasures[i] ; 
			}
			//// Sort 
			SortByString sorter = new SortByString(shuffledBags, shuffledMeasures); 
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
			new StableSortingTest(2); 
			new StableSortingTest(32); 
			new StableSortingTest(256); 
			new StableSortingTest(256); 
			//////  Passed! Bye bye.  
			System.out.println("Passed test suite "+this.getClass().getCanonicalName()); 
		}
	}
	
	public static void main_OLD(String [] args) { 
		new SortByString.ClassTest(); 
	}

}
