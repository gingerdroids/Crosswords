package com.gingerdroids.crossword;

import java.util.ArrayList;
import java.util.Arrays;

import com.gingerdroids.crossword.BuildInfo.WordCell;
import com.gingerdroids.crossword.Grid.PlacedWord;
import com.gingerdroids.utils_java.Str;
import com.gingerdroids.utils_java.Util;

/**
 * Creates an initial list of grids by selecting one word and placing it at every possible position. 
 */
public class Builder1Init { 
	
	/**
	 * Highest ASCII value for a letter, so far as we're concerned.
	 */ 
	private static final int lettersLength = 128 ; // 'char' values under 128 won't sign-extend negative on cast to int. 
	
	private final Builder builder ; 
	public final int rowCount ; 
	public final int columnCount ; 

	ArrayList<Grid> gridList = new ArrayList<Grid>(); 
	
	private final String [] words ; 
	
	private Grid[] grids;
	
	Builder1Init(Builder builder, String [] words)  { 
		this.builder = builder ; 
		this.rowCount = builder.rowCount ; 
		this.columnCount = builder.columnCount ; 
		this.words = words ; 
	}

	Builder1Init build() { 
		//////  Select most-crossable word 
		String firstWord ; 
		{
			int [] letterCounts = new int[lettersLength] ; // How often each letter occurs, over all the words. 
			Arrays.fill(letterCounts, 0);
			for (String word : words) { 
				for (int i=0 ; i<word.length() ; i++) { 
					char ch = word.charAt(i); 
					if (ch>=lettersLength) continue ; 
					letterCounts[ch] ++ ; 
				}
			}
			String bestWord = null ; 
			int bestCrossability = Integer.MIN_VALUE ; 
			for (String word : words) { 
				int crossability = getCrossability(letterCounts, word); 
				if (bestWord==null || crossability>bestCrossability) { 
					bestWord = word ; 
					bestCrossability = crossability ; 
				}
			}
			firstWord = bestWord ; 
		}
		//////  Place word in every possible position
		Grid emptyGrid = new Grid(builder); 
		int wordLength = firstWord.length() ; 
		for (int row=0 ; row<rowCount ; row++) { 
			for (int column=0 ; column<=columnCount-wordLength ; column++) { 
				Grid grid = new Grid(emptyGrid); 
				grid.placeWord(firstWord, row, column, true); 
				gridList.add(grid); 
			}
		}
		if (rowCount!=columnCount) { 
			for (int row=0 ; row<=rowCount-wordLength ; row++) { 
				for (int column=0 ; column<columnCount ; column++) { 
					Grid grid = new Grid(emptyGrid); 
					grid.placeWord(firstWord, row, column, false); 
					gridList.add(grid); 
				}
			}
		}
		//////  Bye bye 
		return this ; 
	}
	
	private int getCrossability(int [] letterCounts, String word) { 
		int result = 0 ; 
		for (int i=0 ; i<word.length() ; i++) { 
			char ch = word.charAt(i); 
			if (ch>=lettersLength) continue ; 
			result += letterCounts[ch] ; 
		}
		return result ; 
	}
	
	Grid [] getGrids() { 
		if (grids==null) this.grids = Grid.toArray(gridList); 
		return grids ; 
	}
	
}
