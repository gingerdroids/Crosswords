package com.gingerdroids.crossword;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import com.gingerdroids.crossword.StandardBuilder;
import com.gingerdroids.crossword.Grid;
import com.gingerdroids.crossword.SortedGridList;
import com.gingerdroids.utils_java.DynamicMultiThreader;
import com.gingerdroids.utils_java.Str;
import com.gingerdroids.utils_java.Util;

public class Builder3Flesh { 
	
	/**
	 * Length of single letter words. Given a constant so I can track where it's used. 
	 */
	private static final int singleLetterLength = 1 ;

	private final int gridsKeptCount; 
	
	public Builder3Flesh(int gridsKeptCount) { 
		this.gridsKeptCount = gridsKeptCount ; 
	}
	
	public Grid [] addFlesh(BuildInfo buildInfo, QualityMeasureFactory qualityMeasureFactory, Grid[] inGrids) { 
		int totalWordCount = buildInfo.words.length ; 
		//////  Build the lists of grids. 
		SortedGridList [] listsByWordCount = new SortedGridList[totalWordCount+1] ; // Grids with one word, two words, etc. 
		for (int currentWordCount=0 ; currentWordCount<=totalWordCount ; currentWordCount++) { 
			QualityMeasure qualityMeasure = qualityMeasureFactory.makeQualityMeasure(totalWordCount, currentWordCount);  
			listsByWordCount[currentWordCount] = new SortedGridList(qualityMeasure, gridsKeptCount); 
		}
		for (Grid grid : inGrids) { 
			int gridWordCount = grid.placedWordList.size(); 
			listsByWordCount[gridWordCount].addGrid(grid, false); 
		}
		//////  Build grids with more words 
		/* Maintain diversity in the list of grids by only allowing two child-grids from each grid into the next iteration. */
		Grid [] sortedResults = null ; 
		for (int fromWordCount=0 ; fromWordCount<totalWordCount ; fromWordCount++) { 
			SortedGridList fromList = listsByWordCount[fromWordCount] ; 
			int toWordCount = fromWordCount+1;
			SortedGridList toList = listsByWordCount[toWordCount] ; 
			Grid[] fromGrids = fromList.getSortedGrids();
			for (Grid fromGrid : fromGrids) { 
				addOneFleshWord(buildInfo, toList.qualityMeasure, fromGrid); 
			}
			/* At this point, the new grids are in fields of their 'fromGrid'. */
			for (Grid fromGrid : fromGrids) { 
				if (fromGrid.next1!=null) toList.addGrid(fromGrid.next1, false); 
				if (fromGrid.next2!=null) toList.addGrid(fromGrid.next2, false); 
			}
			listsByWordCount[fromWordCount] = null ; 
			int toListCount = toList.getCurrentCount();
			sortedResults = toList.getSortedGrids();
			if (toListCount>0) Builder.printProgress("Flesh", sortedResults);
		}
		//////  Bye bye
		return sortedResults ; 
	}

	private static void addOneFleshWord(BuildInfo buildInfo, QualityMeasure qualityMeasure, Grid grid) {
		List<String> pendingWords = grid.getPendingWords(); 
		int pendingWordCount = pendingWords.size(); 
		int highestPendingIndex = pendingWordCount - 1 ; 
		int lowestPendingIndex ; 
		{
			/* Heuristics implemented here...
			 * Singleton words are added after everything else. 
			 * No words more than two letters shorter than the longest. (To force long words to be placed.)
			 * Four words is enough to give the algorithm some options. 
			 */
			final int desiredOptionCount = 4 ; 
			final int maxShortening = 2 ; 
			String longestWord = pendingWords.get(highestPendingIndex); 
			int maxLength = longestWord.length(); 
			int minAcceptableLength = Math.max(maxLength-maxShortening, 0); 
			if (maxLength>singleLetterLength) { 
				lowestPendingIndex = highestPendingIndex - desiredOptionCount + 1 ; 
				if (lowestPendingIndex<0) lowestPendingIndex = 0 ; 
				int tentativeLength = pendingWords.get(lowestPendingIndex).length(); 
				if (tentativeLength>=minAcceptableLength) { 
					/* Found enough words of acceptable length. Skip to first of this length. */
					lowestPendingIndex -- ; 
					while (lowestPendingIndex>=0 && pendingWords.get(lowestPendingIndex).length()>=tentativeLength) lowestPendingIndex -- ; 
					lowestPendingIndex ++ ; 
				} else { 
					/* Tentative word is too short. Force longer words to be placed. */
					lowestPendingIndex ++ ; 
					while (pendingWords.get(lowestPendingIndex).length()<minAcceptableLength) lowestPendingIndex ++ ; 
				}
			} else { 
				/* Singleton words. */
				lowestPendingIndex = 0 ; 
			}
		}
		int liveCount = highestPendingIndex - lowestPendingIndex + 1 ; 
		if (Util.ffalse) { 
			/* With parallelization (not deterministic). Quick measurement on 4 processor machine: less than 2x speed-up. */
			DynamicMultiThreader multiThreader = new DynamicMultiThreader(liveCount) {
				@Override
				protected void processItem(int itemNumber) {
					int pendingIndex = highestPendingIndex - itemNumber ; 
					String pendingWord = pendingWords.get(pendingIndex);
					addOneFleshWord(buildInfo, qualityMeasure, grid, pendingWord); 
				}
			};
			multiThreader.startAll(); 
		} else { 
			/* Single-threaded. Deterministic (at least this section is). */
			for (int i=0 ; i<liveCount ; i++) { 
				int pendingIndex = highestPendingIndex - i ; 
				String pendingWord = pendingWords.get(pendingIndex);
				addOneFleshWord(buildInfo, qualityMeasure, grid, pendingWord); 
			}
		}
		/*
		for (int pendingIndex=highestPendingIndex ; pendingIndex>=lowestPendingIndex ; pendingIndex--) { // Longest word first, not all words. 
			String pendingWord = pendingWords.get(pendingIndex);
			addOneFleshWord(outList, buildInfo, grid, pendingWord); 
		}
		 */
	}

	private static void addOneFleshWord(BuildInfo buildInfo, QualityMeasure qualityMeasure, Grid grid, String word) { 
		int rowCount = grid.rowCount ; 
		int columnCount = grid.columnCount; 
		int wordLength = word.length(); 
		//////  Do each row 
		for (int row=0 ; row<rowCount ; row++) { 
			for (int column=0 ; column<=columnCount-wordLength ; column++) { 
				if (grid.isWordPlaceable(word, row, column, true)) { 
					Grid newGrid = grid.copy(); 
					newGrid.placeWord(word, row, column, true); 
					grid.addChild(newGrid, qualityMeasure);
//					outList.addGrid(newGrid, false); 
				}
			}
		}
		//////  Do each column 
		for (int column=0 ; column<=columnCount ; column++) { 
			for (int row=0 ; row<rowCount-wordLength ; row++) { 
				if (grid.isWordPlaceable(word, row, column, false)) { 
					Grid newGrid = grid.copy(); 
					newGrid.placeWord(word, row, column, false); 
					grid.addChild(newGrid, qualityMeasure);
//					outList.addGrid(newGrid, false); 
				}
			}
		}
	}
	
	static class FreeCells { 
		FreeCells(boolean wtf){}
		HashMap<Character, CellSet> freeAcrossCellsByLetterX = new HashMap<Character,CellSet>(); // Locs in grid available for crossing-words
		HashMap<Character, CellSet> freeDownCellsByLetterX = new HashMap<Character,CellSet>(); 
	}
	
	@SuppressWarnings("serial")
	public static class CellSet extends HashSet<Cell> {}

}
