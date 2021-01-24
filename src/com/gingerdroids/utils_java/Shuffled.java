package com.gingerdroids.utils_java;

public class Shuffled { 
	
	public final int count ; 
	
	private final int [] numbers ; 
	
	private int cursor ; 

	public Shuffled(int count) { 
		this.count = count ; 
		this.numbers = new int[count] ; 
		for (int i=0 ; i<count ; i++) numbers[i] = i ; 
		reset(); 
	}
	
	/**
	 * Reset to the beginning. 
	 * <p>
	 * The indices will not necessarily appear in the same order, 
	 * but they will all be returned before {@link #isFinished()} returns <code>true</code>. 
	 */
	public void reset() { 
		this.cursor = 0 ; 
	}
	
	public boolean isFinished() { 
		return cursor >= count ; 
	}
	
	public int nextIndex() { 
		if (isFinished()) throw new RuntimeException("All "+count+" indices have been returned."); 
		int remaining = count - cursor ; 
		int swapOffset = Util.random.nextInt(remaining); 
		int swapIndex = cursor + swapOffset ; 
		int tmp = numbers[cursor] ; 
		numbers[cursor] = numbers[swapIndex] ; 
		numbers[swapIndex] = tmp ; 
		int resultIndex = numbers[cursor] ; 
		cursor ++ ; 
		return resultIndex ; 
	}
	
	public static class Array<ElemType> { 
		
		private final Shuffled indices ; 
		
		private final ElemType [] array ; 

		public Array(ElemType [] array) { 
			this.indices = new Shuffled(array.length); 
			this.array = array ; 
		}

		/**
		 * Reset to the beginning. 
		 * <p>
		 * The items will not necessarily appear in the same order, 
		 * but they will all be returned before {@link #isFinished()} returns <code>true</code>. 
		 */
		public void reset() { 
			indices.reset(); 
		}
		
		public boolean isFinished() { 
			return indices.isFinished(); 
		}
		
		public ElemType nextItem() { 
			return array[indices.nextIndex()] ; 
		}
	}

}
