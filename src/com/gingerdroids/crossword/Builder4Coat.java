package com.gingerdroids.crossword;

import java.util.HashSet;
import java.util.Set;

import com.gingerdroids.crossword.Grid.PlacedWord;
import com.gingerdroids.crossword.WordBank.IndexedWord;

public class Builder4Coat { 
	
//	final static double lengthsFactor = 0 ; 
//	final static double pendingFactor = 0 ; 
//	final static double fullSpanFactor = 1.0 ; // Small, because adding a word shouldn't be penalized. 
//	final static double symSpanFactor = 1.0 ; // Small, because adding a word shouldn't be penalized. 
//	final static double crossingsFactor = 1 ; 
//	final static double surroundedFactor = 1 ;
//	final static double needinessFactor = 5 ; 
//	static final double virginFactor = 0.1 ; 

	private final int optimalCoatWordLength = 5 ;

	private final WordBank wordBank;
	
	public Builder4Coat(WordBank wordBank) {
		this.wordBank = wordBank ; 
	}
	
	public Grid [] buildCoat(Grid [] inGrids) { 
		for (Grid inGrid : inGrids) { 
			buildCoat(inGrid); 
		}
		SortedGridList coatedGrids = new SortedGridList(Builder.qualityMeasure_final, Builder.candidateCountDuringCoat); 
		for (Grid grid : inGrids) coatedGrids.addGrid(grid, false); 
		return coatedGrids.getSortedGrids(); 
	}

	/**
	 * Fully coats the given grid. 
	 * The existing instance is modified (rather than creating a new instance). 
	 */
	public void buildCoat(Grid grid) { 
		int puzzleWordCount = grid.placedWordList.size(); 
		//////  Compute crossing-neediness of each puzzle word 
		NeedinessList needinessList = new NeedinessList(Builder.qualityMeasure_final, grid); 
		//////  Iteratively add coat-words 
//		Set<String> uncrossableWords = new HashSet<String>(); 
		while (needinessList.hasWordsRemaining()) { 
			//// Add a coat-word, hopefully to neediest word. 
			boolean didAddAnywhere = false ; 
			while (needinessList.hasWordsRemaining()) { 
				PlacedWord targetWord = needinessList.getNeediestWord(); 
//				if (uncrossableWords.contains(targetWord.word)) continue ; 
				boolean didAddCrossingTarget = addCoatWord(grid, targetWord); 
				if (didAddCrossingTarget) { 
					needinessList.updateNeediestWord(Builder.qualityMeasure_final, grid, targetWord);
					didAddAnywhere = true ; 
					break ; 
				}
				needinessList.removeNeediestWord(); 
			}
			//// If no word added, stop trying. 
			if (!didAddAnywhere) break ; 
		}
	}

	/**
	 * Attempts to add a coat word crossing the target word. 
	 * @param grid
	 * @param placedWord
	 * @return
	 */
	private boolean addCoatWord(Grid grid, PlacedWord placedWord) { 
		int startRow = placedWord.row ; 
		int startColumn = placedWord.column ; 
		boolean isOldAcross = placedWord.isAcross;
		boolean isNewAcross = !isOldAcross;
		int row = startRow ; 
		int column = startColumn ; 
		String oldWord = placedWord.word ; 
		int oldWordLength = oldWord.length(); 
		for (int i=0 ; i<oldWordLength ; i++) { // For each letter in the existing word... 
			//////  Extract info about this letter in the existing word 
			if (isOldAcross) column = startColumn+i ; else row = startRow+i ; 
			Cell cell = grid.rows[row][column];
			if (isOldAcross && cell.isInDown) continue ; 
			if (!isOldAcross && cell.isInAcross) continue ; 
			//////  Extract constraints on new word 
			int maxBefore ; 
			int maxAfter ; 
			if (isOldAcross) { 
				maxBefore = calculateCoatSpaceUpwards(grid, row, column);
				maxAfter = calculateCoatSpaceDownwards(grid, row, column);
			} else { 
				maxBefore = calculateCoatSpaceLeftwards(grid, row, column);
				maxAfter = calculateCoatSpaceRightwards(grid, row, column);
			}
			int maxNewLength = maxBefore + maxAfter + 1 ; 
			//////  Look for a word from the word-bank which fits. 
			boolean isAdded = false ; 
			for (int tryNewLength=optimalCoatWordLength ; tryNewLength<=maxNewLength && !isAdded ; tryNewLength++) { 
				isAdded = addCoatWord(grid, isNewAcross, row, column, cell, maxBefore, maxAfter, isAdded, tryNewLength);
			}
			for (int tryNewLength=optimalCoatWordLength-1 ; tryNewLength>=2 && !isAdded ; tryNewLength--) { 
				isAdded = addCoatWord(grid, isNewAcross, row, column, cell, maxBefore, maxAfter, isAdded, tryNewLength);
			}
			if (isAdded) return true ; 
		}
		return false ; 
	}

	public boolean addCoatWord(Grid grid, boolean isNewAcross, int row, int column, Cell cell, int maxBefore, int maxAfter, boolean isAdded, int tryNewLength) {
		IndexedWord[] indexedWords = wordBank.getWords(cell.ch, tryNewLength); // Fetch words containing this letter, with the desired length. 
		for (IndexedWord indexedWord : indexedWords) { // For each candidate-word of this length... 
			if (grid.placedWordSet.contains(indexedWord.word)) continue ; 
			int letterIndex = indexedWord.index ; 
			int lettersBefore = letterIndex ; 
			int lettersAfter = tryNewLength - letterIndex - 1 ; 
			if (lettersBefore<=maxBefore && lettersAfter<=maxAfter) { 
				/* Here we know: There is space for the word. But does it clash with existing letters in the grid? */
				int newRow = row ; 
				int newColumn = column ; 
				if (isNewAcross) newColumn -= letterIndex ; else newRow -= letterIndex ; 
				if (grid.isWordPlaceable(indexedWord.word, newRow, newColumn, isNewAcross)) { 
					/* Here we know: This candidate word can be placed in the grid. */
					grid.placeWord(indexedWord.word, newRow, newColumn, isNewAcross); 
					isAdded = true ; 
					break ; 
				}
			}
			if (isAdded) break ; 
		}
		return isAdded;
	}
	
//	private void breakME() {
//		// TODO Auto-generated method stub
//		
//	}

	/**
	 * Compute how many cells to the right of the given cell can be covered by an across-word. 
	 */
	private int calculateCoatSpaceRightwards(Grid grid, int row, int startColumn) { 
		int column = startColumn + 1 ; 
		while (column<grid.columnCount && !grid.rows[row][column].isInAcross) column ++ ; 
		return column - startColumn - 1 ; 
	}
	
	/**
	 * Compute how many cells to the left of the given cell can be covered by an across-word. 
	 */
	private int calculateCoatSpaceLeftwards(Grid grid, int row, int startColumn) { 
		int column = startColumn - 1 ; 
		while (column>=0 && !grid.rows[row][column].isInAcross) column -- ; 
		return startColumn - column - 1 ; 
	}
	
	/**
	 * Compute how many cells below the given cell can be covered by a down-word. 
	 */
	private int calculateCoatSpaceDownwards(Grid grid, int startRow, int column) { 
		int row = startRow + 1 ; 
		while (row<grid.rowCount && !grid.rows[row][column].isInDown) row ++ ; 
		return row - startRow - 1 ; 
	}
	
	/**
	 * Compute how many cells above the given cell can be covered by a down-word. 
	 */
	private int calculateCoatSpaceUpwards(Grid grid, int startRow, int column) { 
		int row = startRow - 1 ; 
		while (row>=0 && !grid.rows[row][column].isInDown) row -- ; 
		return startRow - row - 1 ; 
	}

}
