package com.gingerdroids.crossword;

import java.util.HashMap;

import com.gingerdroids.utils_java.SortByDouble;
import com.gingerdroids.utils_java.StringMaker;

public class PuzzlePhrase { 
	
	public final Clue clue ; 
	
	/**
	 * The words in the phrase, corresponding to {@link Clue#orderedWords}. 
	 * Where a word does not appear in the crossword, it is null in this array. 
	 * <p>
	 * This is filled in the constructor. 
	 */
	public final PuzzleWord[] orderedWords ; 
	
	private int phraseNumber = Integer.MIN_VALUE ; 
	
	PuzzlePhrase(Clue clue, HashMap<String, PuzzleWord> puzzleWordMap) { 
		this.clue = clue ; 
		int orderedCount = clue.orderedWords.length;
		this.orderedWords = new PuzzleWord[orderedCount] ; 
		for (int i=0 ; i<orderedCount ; i++) { 
			String word = clue.orderedWords[i] ; 
			if (puzzleWordMap.containsKey(word)) { // Skip words which weren't placed in grid. 
				PuzzleWord puzzleWord = puzzleWordMap.get(word); 
				orderedWords[i] = puzzleWord ; 
				puzzleWord.addPhrase(this); 
			}
		}
	}
	
	static class Numberer { 
		
		private static int nextNumber = 1 ; 
		
		void noteUse(PuzzlePhrase phrase) { 
			if (phrase.phraseNumber < 0) { 
				phrase.phraseNumber = nextNumber ; 
				nextNumber ++ ; 
			}
		}
		
	}
	
	public int getPhraseNumber() { 
		return phraseNumber ; 
	}
	
	private String locDirns ; 
	
	public String getLocDirns() {
		if (locDirns==null) { 
			StringMaker<PuzzleWord> wordNumbers = new StringMaker<PuzzleWord>(orderedWords, " ") { 
				@Override
				public String getString(PuzzleWord puzzleWord) { 
					return puzzleWord.getNumberDirn(); 
				}
			};
			this.locDirns = wordNumbers.string;
		}
		return locDirns ; 
	}

	private static double[] computeLocationSortDoubles(PuzzlePhrase[] phrases) {
		int arrayLength = phrases.length;
		double [] measures = new double[arrayLength] ; 
		for (int i=0 ; i<arrayLength ; i++) measures[i] = phrases[i].orderedWords[0].getLocationSortDouble(); 
		return measures ; 
	}
	
	public static class SortByFirstLocation extends SortByDouble { 
		
		public final PuzzlePhrase [] sortedPhrases ; 
		
		public SortByFirstLocation(PuzzlePhrase [] phrases) { 
			super(phrases, computeLocationSortDoubles(phrases)); 
			int arrayLength = phrases.length;
			this.sortedPhrases = new PuzzlePhrase[arrayLength] ; 
			for (int i=0 ; i<arrayLength ; i++) sortedPhrases[i] = (PuzzlePhrase) results[i] ; 
		}
	}
	
}
