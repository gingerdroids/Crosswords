package com.gingerdroids.crossword;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import com.gingerdroids.crossword.Grid.PlacedWord;
import com.gingerdroids.utils_java.SortByString;
import com.gingerdroids.utils_java.SortByDouble;

/**
 * A clue for a single word to be displayed with the crossword. 
 * That is, an "Across" or "Down" entry. 
 * <p>
 * Only used for words the human must guess. Padding words do not have an instance of this class. 
 */
public class PuzzleWord { 
	
	public final String word ; 

//	/**
//	 * Clue for this word, as a standalone word - not a clue for a multi-word phrase. 
//	 * Can be null. 
//	 */
//	private Clue wordClue ; 

	private int wordNumber = Integer.MIN_VALUE ; 

	private String numberDirn;

	/**
	 * Phrases used in the crossword that this word occurs in. 
	 * <p>
	 * WARNING: This will not be correctly filled until all {@link PuzzlePhrase} objects have been built. 
	 */
	public final Set<PuzzlePhrase> phrases = new HashSet<PuzzlePhrase>() ; 

	public final int row ;
	public final int column ;
	public final boolean isAcross ;

	PuzzleWord(PlacedWord placedWord) { 
		this.word = placedWord.word ; 
		this.row = placedWord.row ; 
		this.column = placedWord.column ; 
		this.isAcross = placedWord.isAcross ; 
	}

	void addPhrase(PuzzlePhrase puzzlePhrase) { 
		phrases.add(puzzlePhrase); 
//		if (puzzlePhrase.orderedWords.length==1) this.wordClue = puzzlePhrase.clue ; 
	}

	void setWordNumber(int wordNumber) { 
		this.wordNumber = wordNumber ;
		String dirnStr = isAcross?"a":"d" ; 
		this.numberDirn = ""+wordNumber+dirnStr; 
	}

	public int getWordNumber() {
		return wordNumber;
	}

	public String getNumberDirn() { 
		return numberDirn ; 
	}

	public boolean isSameCell(PuzzleWord other) { 
		if (other==null) return false ; 
		if (this.row!=other.row) return false ;  
		if (this.column!=other.column) return false ;  
		return true ;
	}
	
	/**
	 * Utility - returns a measure for sorting loc-dirns. 
	 */
	double getLocationSortDouble() { 
		if (wordNumber<0) throw new IllegalStateException("Field 'wordNumber' has not been set."); 
		if (isAcross) return wordNumber ; 
		return wordNumber + 0.5 ; 
	}

	private static double[] computeLocationSortDoubles(PuzzleWord[] words) {
		int arrayLength = words.length;
		double [] measures = new double[arrayLength] ; 
		for (int i=0 ; i<arrayLength ; i++) measures[i] = words[i].getLocationSortDouble(); 
		return measures ; 
	}

	private static String[] extractStringArray(PuzzleWord[] puzzleWords) {
		int arrayLength = puzzleWords.length;
		String [] words = new String[arrayLength] ; 
		for (int i=0 ; i<arrayLength ; i++) words[i] = puzzleWords[i].word ; 
		return words ; 
	}

	private static double[] extractLengthsArray(PuzzleWord[] words) {
		int arrayLength = words.length;
		double [] measures = new double[arrayLength] ; 
		for (int i=0 ; i<arrayLength ; i++) measures[i] = words[i].word.length() ; 
		return measures ; 
	}
	
	public static class SortByLocation extends SortByDouble { 
		
		public final PuzzleWord [] sortedWords ; 
		
		public SortByLocation(PuzzleWord [] words) { 
			super(words, computeLocationSortDoubles(words)); 
			int arrayLength = words.length;
			this.sortedWords = new PuzzleWord[arrayLength] ; 
			for (int i=0 ; i<arrayLength ; i++) sortedWords[i] = (PuzzleWord) results[i] ; 
		}
	}
	
	public static class SortLexigraphically extends SortByString { 
		
		public final PuzzleWord [] sortedWords ; 
		
		public SortLexigraphically(PuzzleWord [] words) { 
			super(words, extractStringArray(words)); 
			int arrayLength = words.length;
			this.sortedWords = new PuzzleWord[arrayLength] ; 
			for (int i=0 ; i<arrayLength ; i++) sortedWords[i] = (PuzzleWord) results[i] ; 
		}
	}
	
	public static class SortByLength extends SortByDouble { 
		
		public final PuzzleWord [] sortedWords ; 
		
		public SortByLength(PuzzleWord [] words) { 
			super(words, extractLengthsArray(words)); 
			int arrayLength = words.length;
			this.sortedWords = new PuzzleWord[arrayLength] ; 
			for (int i=0 ; i<arrayLength ; i++) sortedWords[i] = (PuzzleWord) results[i] ; 
		}
	}
	
	/**
	 * Returns the given array, broken into several arrays, one for each word-length. 
	 * The order of words within the arrays is preserved. 
	 * <p>
	 * Lengths with no words have a <code>null</code> item, not an empty array. 
	 */
	public static PuzzleWord [][] groupIntoLengths(PuzzleWord [] puzzleWords) { 
		//////  Find greatest length
		int maxLength = 0 ; 
		for (PuzzleWord puzzleWord : puzzleWords) { 
			int length = puzzleWord.word.length(); 
			if (length>maxLength) maxLength = length ; 
		}
		//////  Group into lists
		int arrayLength = maxLength + 1 ; // There is a zero-length element. 
		PuzzleWordList [] groupLists = new PuzzleWordList[arrayLength] ; 
		for (int i=0 ; i<=maxLength ; i++) groupLists[i] = new PuzzleWordList(); 
		for (PuzzleWord puzzleWord : puzzleWords) { 
			int length = puzzleWord.word.length(); 
			groupLists[length].add(puzzleWord); 
		}
		//////  Extract arrays 
		PuzzleWord [][] groups = new PuzzleWord[arrayLength][] ; 
		for (int wordLength=0 ; wordLength<arrayLength ; wordLength++) { 
			PuzzleWordList groupList = groupLists[wordLength] ; 
			int groupSize = groupList.size();
			if (groupSize>0) { 
				PuzzleWord [] group = new PuzzleWord[groupSize]; 
				groupList.toArray(group); 
				groups[wordLength] = group ; 
			} else { 
				groups[wordLength] = null ; 
			}
		}
		//////  Bye bye 
		return groups ; 
	}
	
	private static class PuzzleWordList extends ArrayList<PuzzleWord> {}
	
//	public static class SortByLength_longestFirst extends StableSortByDouble { 
//		
//		public final PuzzleWord [] sortedWords ; 
//		
//		public SortByLength_longestFirst(PuzzleWord [] words) { 
//			super(words, negate(extractLengthsArray(words))); 
//			int arrayLength = words.length;
//			this.sortedWords = new PuzzleWord[arrayLength] ; 
//			for (int i=0 ; i<arrayLength ; i++) sortedWords[i] = (PuzzleWord) results[i] ; 
//		}
//
//		private static double[] negate(double[] doubles) { 
//			for (int i=0 ; i<doubles.length ; i++) doubles[i] = -doubles[i] ; 
//			return doubles;
//		}
//	}
}
