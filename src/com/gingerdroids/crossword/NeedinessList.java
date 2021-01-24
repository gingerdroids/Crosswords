package com.gingerdroids.crossword;

import java.util.ArrayList;

import com.gingerdroids.crossword.Grid.PlacedWord;
import com.gingerdroids.utils_java.SortByDouble;
import com.gingerdroids.utils_java.Str;

public class NeedinessList { 
	
	/**
	 * The placed-words from the initial-grid, sorted (roughly) with the least needy first. 
	 * <p>
	 * They may not be perfectly sorted, since the grid changes, which changes the neediness. 
	 * It's possible for a word to be crossed without being re-measured. 
	 * <p>
	 * The placed-words from the initial-grid should remain the placed-puzzle-words throughout all calls.  
	 */
	final private PlacedWord [] sortedWords ; 

	/**
	 * The neediness-measures corresponding to {@link #sortedWords}. 
	 * Note that these measures can go out of date. 
	 */
	final private double [] sortedNeedinesses ;

	/**
	 * How many words are remaining. 
	 * Initially, this is the total number of puzzle words, the length of the {@link #sortedWords} array. 
	 * But words can be removed by {@link #removeNeediestWord()}. 
	 */
	private int count ; 
	
	public NeedinessList(UsualQualityMeasure qualityMeasure, Grid initialGrid) { 
		ArrayList<PlacedWord> placedWordList = initialGrid.placedWordList;
		this.count = placedWordList.size();
		PlacedWord [] unsortedWords = new PlacedWord[count]; 
		placedWordList.toArray(unsortedWords); 
		double [] unsortedNeedinesses = new double[count] ; 
		for (int i=0 ; i<count ; i++) unsortedNeedinesses[i] = qualityMeasure.computeCrossingNeediness(initialGrid, unsortedWords[i]); 
		SortedWords sorter = new SortedWords(unsortedWords, unsortedNeedinesses); 
		this.sortedWords = sorter.sortedWords ; 
		this.sortedNeedinesses = sorter.sortedMeasures ; 
	}
	
	private class SortedWords extends SortByDouble { 

		public final PlacedWord [] sortedWords ; 

		public SortedWords(PlacedWord[] in, double[] measures) {
			super(in, measures);
			this.sortedWords = new PlacedWord[results.length]; 
			for (int i=0 ; i<results.length ; i++) sortedWords[i] = (PlacedWord) results[i] ; 
		} 
	}
	
	PlacedWord getNeediestWord() { 
		return sortedWords[count-1] ; 
	}
	
//	/**
//	 * Retrieve a word. 
//	 * Zero retrieves neediest word, one second neediest, etc. 
//	 * @param n
//	 */
//	PlacedWord getNthNeediestWord(int n) { 
//		return sortedWords[lengthZ-n-1] ; 
//	}
	
	/**
	 * The given word has has an updated (reduced) neediness, and will be bubbled down the sorted-list. 
	 * 
	 * @param updatedWord The word which has been updated by the caller. It should be the last word in the list. 
	 * @param grid The grid as it is now, which has probably had coat-words added since the instances initial-grid. 
	 */
	void updateNeediestWord(UsualQualityMeasure qualityMeasure, Grid grid, PlacedWord updatedWord) { 
		if (updatedWord!=sortedWords[count-1]) throw new RuntimeException(); 
		PlacedWord bubblingWord = sortedWords[count-1] ; 
		double bubblingNeediness = qualityMeasure.computeCrossingNeediness(grid, bubblingWord); 
		sortedNeedinesses[count-1] = bubblingNeediness ; // Because the neediest-words measure has changed. 
		int bubblingIndex = count-1 ; 
		while (bubblingIndex>0) { 
			// In each iteration, bubble the word down one, or exit the loop. 
			if (bubblingNeediness>sortedNeedinesses[bubblingIndex-1]) { 
				/* Here we know: the bubblingWord is needier than the one immediately below it. */
				break ; 
			}
			sortedWords[bubblingIndex] = sortedWords[bubblingIndex-1] ; 
			sortedNeedinesses[bubblingIndex] = sortedNeedinesses[bubblingIndex-1] ; 
			bubblingIndex -- ; 
		}
		sortedWords[bubblingIndex] = bubblingWord ; 
		sortedNeedinesses[bubblingIndex] = bubblingNeediness ; 
//		while (true) { 
//			if (bubblingIndex==count-1) break ; 
//		}
	}
	
	/**
	 * Removes this word from the list.
	 * Currently (jan19), this is called when we've tried to cross a word, but no crossings could be found. 
	 */
	void removeNeediestWord() { 
		this.count -- ; 
		sortedWords[count] = null ; // Really ensure it isn't used by accident! 
	}
	
	boolean hasWordsRemaining() { 
		return count > 0 ; 
	}
}
