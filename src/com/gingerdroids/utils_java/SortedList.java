package com.gingerdroids.utils_java;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Collections;
import java.util.Random;
import java.util.Set;

/**
 * Keeps the smallest few of a stream of objects, and provides access to them sorted smallest-first. 
 * It only keeps the smallest ones found so far, so it is space-efficient. 
 * <p>
 * Each time a new object is kept, a linear insertion algorithm is used among those kept so far. 
 * So, it's most efficient when only a small fraction of the stream are kept.  
 * <p>
 * The algorithm performs well if the objects are already sorted, but performs worst-case if the objects are reverse sorted. 
 */
public class SortedList { 
	/* DEV NOTE: It's tempting to make this a generic, but you can't create an array of generic types. See keptBags field. */

	public final int maxKept ; 

	private int count ; 

	/**
	 * The bags corresponding to the measures in {@link #keptMeasures}. 
	 */
	private final Object [] keptBags ; 

	/**
	 * The smallest measures found so far. The smallest measure is in index 0. 
	 */
	private final double [] keptMeasures ; 

	protected SortedList(int maxKept) { 
		this.maxKept = maxKept ; 
		this.keptBags = new Object[maxKept] ; 
		this.keptMeasures = new double[maxKept] ; 
		this.count = 0 ; 
		/* Here we know (trivially): keptMeasures is sorted. keptBags corresponds to keptMeasures. */
	} 

	/**
	 * Add a new item to the list, if it's lower than the threshold. 
	 * @param newBag
	 * @param newMeasure
	 * @return
	 */
	protected synchronized boolean add(Object newBag, double newMeasure) { 
		/* Precondition: keptMeasures is sorted. keptBags corresponds to keptMeasures. */
		int indexBelow = count - 1 ; 
		while (indexBelow>=0 && keptMeasures[indexBelow]>newMeasure) indexBelow -- ; 
		/* Here we know: measures at indexBelow and before are less than 'newMeasure'. Measures above indexBelow are greater than 'newMeasure'. */
		int newIndex = indexBelow + 1 ; 
		if (newIndex>=maxKept) { 
			/* Here we know: the newBag and newMeasure are not in the least maxKept so far. They should not be inserted. */
			return false ; 
		}
		/* Here we know: the newBag and newMeasure should be inserted at newIndex. */
		int newCount = count + 1 ; 
		if (newCount>maxKept) newCount = maxKept ; 
		for (int sourceIndex=newCount-2 ; sourceIndex>=newIndex ; sourceIndex--) { 
			keptBags[sourceIndex+1] = keptBags[sourceIndex] ; 
			keptMeasures[sourceIndex+1] = keptMeasures[sourceIndex] ; 
		}
		keptBags[newIndex] = newBag ; 
		keptMeasures[newIndex] = newMeasure ; 
		this.count = newCount ; 
		return true ; 
		/* Postcondition: keptMeasures is sorted. keptBags corresponds to keptMeasures. */
	}

	/**
	 * The current threshold - any measure larger than this will not be kept. 
	 */
	public double getCurrentThreshold() { 
		if (count>=maxKept) { 
			return keptMeasures[count-1] ; 
		} else { 
			return Double.POSITIVE_INFINITY ; 
		}
	}

	protected Object [] getSortedObjects() { 
		Object [] array = new Object[count]; 
		System.arraycopy(keptBags, 0, array, 0, count); 
		return array ; 
	}

	public int getCurrentCount() { 
		return count ; 
	}

	public static class Strings extends SortedList { 

		public Strings(int maxKept) { 
			super(maxKept);
		}

		public void addString(String str, double measure) { 
			super.add(str, measure); 
		}

		/**
		 * Returns a newly created array of {@link Grid}. 
		 */
		public String[] getSortedStrings() { 
			Object[] sortedObjects = super.getSortedObjects(); 
			int length = sortedObjects.length;
			String [] sortedStrings = new String[length] ; 
			for (int i=0 ; i<length ; i++) { 
				sortedStrings[i] = (String) sortedObjects[i] ; 
			}
			return sortedStrings ; 
		}
	}

	public static class ClassTest { 

		private final Random random = new Random(458645245521L); 

		class TestMaxKept { 

			final int length = 10 ; 
			Object [] rawBags = new Object[length] ; 
			double [] rawMeasures = new double[length] ; 
			final int trialCount = 50 ;
			Object [][] shuffledBags = new Object[trialCount][] ; 
			double [][] shuffledMeasures= new double[trialCount][] ; 

			TestMaxKept() { 
				//////  Build suites of shuffled bags & measures. 
				for (int i=0 ; i<length ; i++) { 
					rawBags[i] = new Integer(i); 
					rawMeasures[i] = i ; 
				}
				for (int trialIndex=0 ; trialIndex<trialCount ; trialIndex++) { 
					makeShuffledPairs(trialIndex); 
				}
				//////  Test maxKept values  
				trialMaxKeptValue(0); 
				trialMaxKeptValue(1); 
				trialMaxKeptValue(2); 
				trialMaxKeptValue(3); 
				trialMaxKeptValue(length/2); 
				trialMaxKeptValue(length-1); 
				trialMaxKeptValue(length); 
				trialMaxKeptValue(length+1); 
				trialMaxKeptValue(2*length+5); 
				//////  Bye bye 
				//System.out.println("Passed tests "+this.getClass().getCanonicalName()); 
			}

			private void trialMaxKeptValue(final int maxKept) { 
				//				Log.d("DBG", "Starting trial for maxKept == "+maxKept); 
				final int expectedLength = Math.min(maxKept, length);
				for (int trialIndex=0 ; trialIndex<trialCount ; trialIndex++) { 
					SortedList sorter = new SortedList(maxKept);
					for (int i=0 ; i<length ; i++) { 
						sorter.add(shuffledBags[trialIndex][i], shuffledMeasures[trialIndex][i]); 
					}
					Object[] sortedBags = sorter.getSortedObjects(); 
					if (sortedBags.length!=expectedLength) throw new RuntimeException("Expected length "+expectedLength+", actual length "+sortedBags.length); 
					for (int i=0 ; i<expectedLength ; i++) { 
						if (sortedBags[i]!=rawBags[i]) throw new RuntimeException("Wrong at index "+i+", result array was: "+Str.the(sortedBags)); 
					}
				}
				//				Log.d("DBG", "Finished trial for maxKept == "+maxKept); 
			}

			private void makeShuffledPairs(int trialIndex) { 
				int [] shuffledIndices = makeShuffledIndices(length); 
				//				Log.d("DBG", "Shuffling is: "+Str.the(shuffledIndices)); 
				Object[] bags = new Object[length] ; 
				double[] measures = new double[length] ; 
				for (int i=0 ; i<length ; i++) { 
					bags[shuffledIndices[i]] = rawBags[i] ; 
					measures[shuffledIndices[i]] = rawMeasures[i] ; 
				}
				shuffledBags[trialIndex] = bags ; 
				shuffledMeasures[trialIndex] = measures ; 
			}

		}

		int [] makeShuffledIndices(int length) { 
			int [] indices = new int[length] ; 
			for (int i=0 ; i<length ; i++) indices[i] = i ; 
			for (int i=length-1 ; i>0 ; i--) { 
				int j = random.nextInt(i+1); 
				if (j>i) throw new RuntimeException("WTF!?"); 
				int tmp = indices[i] ; 
				indices[i] = indices[j] ; 
				indices[j] = tmp ; 
			}
			return indices ; 
		}

		abstract class TestDoubleStream { 

			final int length ; 
			Double [] bags ; 
			double [] measures ; 

			TestDoubleStream(int length, int maxKept, String testName) { 
				final boolean showData = false ; 
				this.length = length ; 
				this.bags = new Double[length] ; 
				this.measures = new double[length] ; 
				int[] indices = new int[length] ; 
				//////  Build the data 
				for (int i=0 ; i<length ; i++) indices[i] = i ; 
				shuffleIndices(indices); 
				for (int i=0 ; i<length ; i++) { 
					int shuffledIndex = indices[i] ; 
					double value = getMeasure(i);
					measures[shuffledIndex] = value ; 
					bags[shuffledIndex] = (double) value ; 
				}
				/*
				if (showData) Log.d("DEV", testName); 
				if (showData) Log.d("DEV", "Shuffled indices "+Str.the(indices)+" ; measures "+Str.the(measures)); 
				 */
				/* Here we assume: all distinct bags are at least 1.0 apart, so we can add less-than-one to them without altering their order. 
				 * This is useful to test that the sort is stable (ie, the order of equal-measured bags is left the same). */ 
				double epsilon = 0.5 / length ; 
				for (int i=0 ; i<length ; i++) bags[i] += i * epsilon ; 
				//////  Run the sorter
				SortedList sorter = new SortedList(maxKept);
				for (int i=0 ; i<length ; i++) { 
					sorter.add(bags[i], measures[i]); 
				}
				Object[] sortedBags = sorter.getSortedObjects(); 
				/*
				if (showData) Log.d("DEV", "Results "+Str.the(sortedBags)); 
				if (showData) Log.d("DEV", "..... "); 
				 */
				//////  Check the results 
				double prevValue = (Double) sortedBags[0] ; 
				for (int i=1 ; i<maxKept ; i++) { 
					double value = (Double) sortedBags[i] ; 
					if (value<prevValue) throw new RuntimeException(); 
					prevValue = value ; 
				}
				//System.out.println("Passed tests "+testName); 
			}

			abstract double getMeasure(int i); // All distinct values at least one apart. 
			abstract void shuffleIndices(int [] indices); 
		}

		class TestOutOfOrder { 
			public TestOutOfOrder() {
				SortedList sortedList = new SortedList(8); 
				sortedList.add("A", 5);
				if (sortedList.getCurrentCount()!=1) throw new RuntimeException(); 
				if (sortedList.getCurrentThreshold()!=Double.POSITIVE_INFINITY) throw new RuntimeException("getCurrentThreshold() returns "+sortedList.getCurrentThreshold()); 
				sortedList.add("B", 6);
				if (sortedList.getCurrentCount()!=2) throw new RuntimeException(); 
				if (sortedList.getCurrentThreshold()!=Double.POSITIVE_INFINITY) throw new RuntimeException("getCurrentThreshold() returns "+sortedList.getCurrentThreshold()); 
				sortedList.add("C", 4);
				if (sortedList.getCurrentCount()!=3) throw new RuntimeException(); 
				if (sortedList.getCurrentThreshold()!=Double.POSITIVE_INFINITY) throw new RuntimeException("getCurrentThreshold() returns "+sortedList.getCurrentThreshold()); 
				//System.out.println("Passed tests "+this.getClass().getCanonicalName()); 
			}
		}


		ClassTest() { 
			//////  Test maxKept
			new TestMaxKept(); 
			//////  Test short lists
			new TestOutOfOrder(); 
			//////  Test various streams
			final int length = 1000 ;
			final int maxKept = 20 ;
			if (false) new TestDoubleStream(length, maxKept, this.getClass().getCanonicalName()+" Should fail") { // Testing the test code. 
				@Override void shuffleIndices(int[] indices) { shuffle(indices); }
				@Override double getMeasure(int i) { return i * 0.0001 ; } // Distinct measures need separation 
			};
			new TestDoubleStream(length, maxKept, this.getClass().getCanonicalName()+" Ascending") { 
				@Override void shuffleIndices(int[] indices) {}
				@Override double getMeasure(int i) { return i ; } 
			};
			new TestDoubleStream(length, maxKept, this.getClass().getCanonicalName()+" Descending") { 
				@Override void shuffleIndices(int[] indices) { reverse(indices); }
				@Override double getMeasure(int i) { return i ; } 
			};
			new TestDoubleStream(length, maxKept, this.getClass().getCanonicalName()+" Shuffled") { 
				@Override void shuffleIndices(int[] indices) { shuffle(indices); }
				@Override double getMeasure(int i) { return i ; } 
			};
			new TestDoubleStream(length, maxKept, this.getClass().getCanonicalName()+" Uniform") { 
				@Override void shuffleIndices(int[] indices) { shuffle(indices); }
				@Override double getMeasure(int i) { return 5 ; } 
			};
			new TestDoubleStream(length, maxKept, this.getClass().getCanonicalName()+" Fewer") { 
				@Override void shuffleIndices(int[] indices) { shuffle(indices); }
				@Override double getMeasure(int i) { return i<maxKept-1 ? 4.0 : 6.0 ; } 
			};
			new TestDoubleStream(length, maxKept, this.getClass().getCanonicalName()+" Exact") { 
				@Override void shuffleIndices(int[] indices) { shuffle(indices); }
				@Override double getMeasure(int i) { return i<maxKept ? 4.0 : 6.0 ; } 
			};
			new TestDoubleStream(length, maxKept, this.getClass().getCanonicalName()+" More") { 
				@Override void shuffleIndices(int[] indices) { shuffle(indices); }
				@Override double getMeasure(int i) { return i<maxKept+1 ? 4.0 : 6.0 ; } 
			};
			//////  Passed! Bye bye.  
			System.out.println("Passed test suite "+this.getClass().getCanonicalName()); 
		}

		private void reverse(int[] indices) { 
			int length = indices.length ;
			for (int i=0 ; i<=length/2 ; i++) { 
				int j = length - i - 1 ; 
				int tmp = indices[i] ; 
				indices[i] = indices[j] ; 
				indices[j] = tmp ; 
			}
		}

		int [] shuffle(int[] indices) { 
			int length = indices.length ;
			for (int i=0 ; i<length ; i++) indices[i] = i ; 
			for (int i=length-1 ; i>0 ; i--) { 
				int j = random.nextInt(i+1); 
				if (j>i) throw new RuntimeException("WTF!?"); 
				int tmp = indices[i] ; 
				indices[i] = indices[j] ; 
				indices[j] = tmp ; 
			}
			return indices ; 
		}
	}

	public static void main_DEL(String [] args) { 
		new ClassTest(); 
	}

}
