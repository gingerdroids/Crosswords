package com.gingerdroids.utils_java;

import java.util.Random;

/**
 * Keeps the largest few, sorted, of a stream of numbers. 
 */
public class LargestN_int { 

	public final int maxKept ; 

	private int count ; 

	/**
	 * The largest values found so far. The largest is in index 0. 
	 */
	private final int [] largest ; 

	public LargestN_int(int maxKept) { 
		this.maxKept = maxKept ; 
		this.largest = new int[maxKept] ; 
		this.count = 0 ; 
		/* Here we know (trivially): 'largest' is sorted. */
	} 

	/**
	 * The next number in the stream. 
	 */
	public synchronized boolean add(int number) { 
		/* Precondition: 'largest' is sorted, descending. */ 
		int indexBelow = count - 1 ; 
		while (indexBelow>=0 && largest[indexBelow]<number) indexBelow -- ; 
		/* Here we know: measures at indexBelow and before are greater-equal than 'number'. 
		 * Measures above indexBelow are less than 'number'. */
		int newIndex = indexBelow + 1 ; 
		if (newIndex>=maxKept) { 
			/* Here we know: the number is not in the least maxKept so far. It should not be inserted. */
			return false ; 
		}
		/* Here we know: the number should be inserted at newIndex. */
		int newCount = count + 1 ; 
		if (newCount>maxKept) newCount = maxKept ; 
		for (int sourceIndex=newCount-2 ; sourceIndex>=newIndex ; sourceIndex--) { 
			largest[sourceIndex+1] = largest[sourceIndex] ; 
		}
		largest[newIndex] = number ; 
		this.count = newCount ; 
		return true ; 
		/* Postcondition: 'largest' is sorted. */
	}

	/**
	 * The current threshold - any measure smaller than this will not be kept. 
	 */
	public int getCurrentThreshold() { 
		if (count>=maxKept) { 
			return largest[count-1] ; 
		} else { 
			return Integer.MIN_VALUE ; 
		}
	}

	public int [] getLargest() { 
		int [] array = new int[count]; 
		System.arraycopy(largest, 0, array, 0, count); 
		return array ; 
	}
	
	public int getCurrentCount() { 
		return count ; 
	}

	public static class ClassTest { 

		private final Random random = new Random(458645245521L); 

		class TestMaxKept { 

			final int length = 10 ; 
			int [] rawMeasures = new int[length] ; 
			final int trialCount = 50 ;
			int [][] shuffledMeasures= new int[trialCount][] ; 

			TestMaxKept() { 
				//////  Build suites of shuffled bags & measures. 
				for (int i=0 ; i<length ; i++) { 
					rawMeasures[i] = i ; 
				}
				for (int trialIndex=0 ; trialIndex<trialCount ; trialIndex++) { 
					shuffle(trialIndex); 
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
			}

			private void trialMaxKeptValue(final int maxKept) { 
				final int expectedLength = Math.min(maxKept, length);
				for (int trialIndex=0 ; trialIndex<trialCount ; trialIndex++) { 
					LargestN_int sorter = new LargestN_int(maxKept);
					for (int i=0 ; i<length ; i++) { 
						sorter.add(shuffledMeasures[trialIndex][i]); 
					}
					int[] sorted = sorter.getLargest(); 
					if (sorted.length!=expectedLength) throw new RuntimeException("Expected length "+expectedLength+", actual length "+sorted.length); 
					for (int i=0 ; i<expectedLength ; i++) { 
						if (sorted[i]!=rawMeasures[length-i-1]) throw new RuntimeException("Wrong at index "+i+", result array was: "+Str.the(sorted)); 
					}
				}
			}

			private void shuffle(int trialIndex) { 
				int [] shuffledIndices = makeShuffledIndices(length); 
				int[] measures = new int[length] ; 
				for (int i=0 ; i<length ; i++) { 
					measures[shuffledIndices[i]] = rawMeasures[i] ; 
				}
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

		abstract class TestIntStream { 

			final int length ; 
			int [] measures ; 

			TestIntStream(int length, int maxKept, String testName) { 
				this.length = length ; 
				this.measures = new int[length] ; 
				int[] indices = new int[length] ; 
				//////  Build the data 
				for (int i=0 ; i<length ; i++) indices[i] = i ; 
				shuffleIndices(indices); 
				for (int i=0 ; i<length ; i++) { 
					int shuffledIndex = indices[i] ; 
					int value = getMeasure(i);
					measures[shuffledIndex] = value ; 
				}
				//////  Run the sorter
				LargestN_int sorter = new LargestN_int(maxKept);
				for (int i=0 ; i<length ; i++) { 
					sorter.add(measures[i]); 
				}
				//////  Check the results 
				int [] sorted = sorter.getLargest(); 
				int prevValue = sorted[0] ; 
				for (int i=1 ; i<maxKept ; i++) { 
					int value = sorted[i] ; 
					if (value>prevValue) throw new RuntimeException("Expected descending (or equal), wrong at index "+i+" in result "+Str.the(sorted)); 
					prevValue = value ; 
				}
			}

			abstract int getMeasure(int i); 
			abstract void shuffleIndices(int [] indices); 
		}

		class TestOutOfOrder { 
			public TestOutOfOrder() {
				LargestN_int sortedList = new LargestN_int(8); 
				sortedList.add(5);
				if (sortedList.getCurrentCount()!=1) throw new RuntimeException(); 
				if (sortedList.getCurrentThreshold()!=Integer.MIN_VALUE) throw new RuntimeException("getCurrentThreshold() returns "+sortedList.getCurrentThreshold()); 
				sortedList.add(6);
				if (sortedList.getCurrentCount()!=2) throw new RuntimeException(); 
				if (sortedList.getCurrentThreshold()!=Integer.MIN_VALUE) throw new RuntimeException("getCurrentThreshold() returns "+sortedList.getCurrentThreshold()); 
				sortedList.add(4);
				if (sortedList.getCurrentCount()!=3) throw new RuntimeException(); 
				if (sortedList.getCurrentThreshold()!=Integer.MIN_VALUE) throw new RuntimeException("getCurrentThreshold() returns "+sortedList.getCurrentThreshold()); 
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
			new TestIntStream(length, maxKept, this.getClass().getCanonicalName()+" Ascending") { 
				@Override void shuffleIndices(int[] indices) {}
				@Override int getMeasure(int i) { return i ; } 
			};
			new TestIntStream(length, maxKept, this.getClass().getCanonicalName()+" Descending") { 
				@Override void shuffleIndices(int[] indices) { reverse(indices); }
				@Override int getMeasure(int i) { return i ; } 
			};
			new TestIntStream(length, maxKept, this.getClass().getCanonicalName()+" Shuffled") { 
				@Override void shuffleIndices(int[] indices) { shuffle(indices); }
				@Override int getMeasure(int i) { return i ; } 
			};
			new TestIntStream(length, maxKept, this.getClass().getCanonicalName()+" Uniform") { 
				@Override void shuffleIndices(int[] indices) { shuffle(indices); }
				@Override int getMeasure(int i) { return 5 ; } 
			};
			new TestIntStream(length, maxKept, this.getClass().getCanonicalName()+" Fewer") { 
				@Override void shuffleIndices(int[] indices) { shuffle(indices); }
				@Override int getMeasure(int i) { return i<maxKept-1 ? 4 : 6 ; } 
			};
			new TestIntStream(length, maxKept, this.getClass().getCanonicalName()+" Exact") { 
				@Override void shuffleIndices(int[] indices) { shuffle(indices); }
				@Override int getMeasure(int i) { return i<maxKept ? 4 : 6 ; } 
			};
			new TestIntStream(length, maxKept, this.getClass().getCanonicalName()+" More") { 
				@Override void shuffleIndices(int[] indices) { shuffle(indices); }
				@Override int getMeasure(int i) { return i<maxKept+1 ? 4 : 6 ; } 
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

//	public static void main(String [] args) { 
//		new ClassTest(); 
//	}

}
