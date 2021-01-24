package com.gingerdroids.crossword;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.gingerdroids.crossword.BuildInfo.StringList;
import com.gingerdroids.crossword.Builder3Flesh.CellSet;
import com.gingerdroids.crossword.apps.PdfClueless;
import com.gingerdroids.crossword.Cell;
import com.gingerdroids.utils_java.InstanceCounter;
import com.gingerdroids.utils_java.Str;

/**
 * Grid of cells.
 * Need not be rectangular. 
 */
public class Grid { 
	
//	private InstanceCounter instanceCounter = new InstanceCounter();
	
	public static final char space = ' ' ; 
	
	private final Builder builder ; 
	
	private QualityMeasure currentQualityMeasure ; 
	
	private double quality ; 
	
	private Integer longestPendingLength ; 
	
	private Integer longestGapLength ; 

	public final int rowCount ; 
	public final int columnCount ; 
	public final Cell[][] rows ; // First index is row. 
	public final Cell[][] columns ; // First index is column. 
	public final boolean[][] rowHardLeftEdges ; 
	public final boolean[][] columnHardTopEdges ; 
	public final boolean[][] rowWithinWordLeftwards ; 
	public final boolean[][] columnWithinWordUpwards ; 
	
	/**
	 * List of words from clues not yet placed in the grid. 
	 * Words that can never be placed are excluded. (We may know there is no gap large enough.)
	 * The list is sorted shortest first. 
	 */
	private List<String> pendingWords ; 

	/**
	 * All words placed in the grid, both those the human must guess, and those that are given to the human. 
	 */
	public final ArrayList<PlacedWord> placedWordList = new ArrayList<Grid.PlacedWord>(); 
	
	/**
	 * All words placed in the grid, both those the human must guess, and those that are given to the human. 
	 */
	public final Set<String> placedWordSet = new HashSet<String>(); 

	/**
	 * Map from words (in canonical form) to their {@link PuzzleWord} object, for words the human must guess. 
	 */
	public HashMap<String, PuzzleWord> mapWordToPuzzleWord;
	
	/**
	 * Whether we are still building the puzzle cells, or just filling in around them 
	 */
	boolean isPuzzleBuilding = true ; 
	
	/**
	 * Utility for "fleshing" stage of placing words in grid. Should be null elsewhere. 
	 * <p>
	 * These cells can be crossed by a down-word. 
	 * For efficiency reasons, not all crossable cells should be in this list ; there are some cells we have tried to cross with our available words, and failed. 
	 */
	HashMap<Character, CellSet> freeAcrossCellsByLetter = new HashMap<Character,CellSet>(); // Locs in grid available for crossing-words
	
	/**
	 * Utility for "fleshing" stage of placing words in grid. Should be null elsewhere. 
	 * <p>
	 * These cells can be crossed by an across-word. 
	 * For efficiency reasons, not all crossable cells should be in this list ; there are some cells we have tried to cross with our available words, and failed. 
	 */
	HashMap<Character, CellSet> freeDownCellsByLetter = new HashMap<Character,CellSet>(); 
	
	/**
	 * A compressed version of the grid, used to prevent duplicates during the grid-building algorithm. 
	 * <p>
	 * If the grid is altered - eg, placing a word - this should be set to null. 
	 */
	private String signature ; 
	
	/**
	 * A grid derived from this one by adding a word. It is the best such grid found so far. 
	 * <p>
	 * The {@link #quality} field should be set. 
	 * 
	 * @see Grid#addChild(Grid, QualityMeasure)
	 */
	protected Grid next1 ; 
	
	/**
	 * A grid derived from this one by adding a word. It is the second best such grid found so far. 
	 * <p>
	 * The {@link #quality} field should be set. 
	 * 
	 * @see Grid#addChild(Grid, QualityMeasure)
	 */
	protected Grid next2 ; 

	private void resetInnerArrays(int rowCount, int columnCount) { 
		for (int i=0 ; i<rowCount ; i++) { 
			for (int j=0 ; j<columnCount ; j++) { 
				Cell cell = rows[i][j] ; 
				if (cell!=null) { 
					cell.reset(); 
				} else { 
					cell = new Cell(i, j); 
				}
				rows[i][j] = cell ; 
				columns[j][i] = cell ; 
			}
		}
		for (int i=0 ; i<rowCount ; i++) { 
			Arrays.fill(rowHardLeftEdges[i], false); 
			rowHardLeftEdges[i][0] = true ; 
			rowHardLeftEdges[i][columnCount] = true ; 
			Arrays.fill(rowWithinWordLeftwards[i], false); 
		}
		for (int j=0 ; j<columnCount ; j++) { 
			fillIntersticeColumn(j); 
		}
		fillIntersticeColumn(columnCount);
	}

	/**
	 * Constructs an empty grid. 
	 */
	public Grid(Builder builder) { 
		this.builder = builder ; 
		//////  Lengths 
		this.rowCount = builder.rowCount ; 
		this.columnCount = builder.columnCount ; 
		//////  Create outer arrays 
		this.rows = new Cell[rowCount][] ; 
		this.rowHardLeftEdges = new boolean[rowCount+1][] ; 
		this.rowWithinWordLeftwards = new boolean[rowCount+1][] ; 
		this.columns = new Cell[columnCount][] ; 
		this.columnHardTopEdges = new boolean[columnCount+1][] ; 
		this.columnWithinWordUpwards = new boolean[columnCount+1][] ; 
		//////  Create inner arrays 
		for (int i=0 ; i<rowCount ; i++) { 
			rows[i] = new Cell[columnCount] ; 
			rowHardLeftEdges[i] = new boolean[columnCount+1]; 
			rowWithinWordLeftwards[i] = new boolean[columnCount+1]; 
		}
		for (int j=0 ; j<columnCount ; j++) { 
			columns[j] = new Cell[rowCount] ; 
			createIntersticeColumn(j); 
		}
		createIntersticeColumn(columnCount); 
		//////  Fill inner arrays 
		for (int i=0 ; i<rowCount ; i++) { 
			for (int j=0 ; j<columnCount ; j++) { 
				Cell cell = new Cell(i, j); 
				rows[i][j] = cell ; 
				columns[j][i] = cell ; 
			}
		}
		for (int i=0 ; i<rowCount ; i++) { 
			Arrays.fill(rowHardLeftEdges[i], false); 
			rowHardLeftEdges[i][0] = true ; 
			rowHardLeftEdges[i][columnCount] = true ; 
			Arrays.fill(rowWithinWordLeftwards[i], false); 
		}
		for (int j=0 ; j<columnCount ; j++) { 
			fillIntersticeColumn(j); 
		}
		fillIntersticeColumn(columnCount); 
	}
	
	/**
	 * Copy-constructor. 
	 */
	Grid(Grid old) { 
		/*
		 * (October 2020)
		 * I profiled the app on a typical crossword. 
		 * It spends nearly half its time in the Grid copy-constructor. 
		 * Replacing the call to the copy-constructor with something more efficient would make the code more complicated 
		 * (at a data-structure level, not algorithm) and the last thing this code needs is more complicated stuff. 
		 */ 
		this.builder = old.builder ; 
		//////  Lengths 
		this.rowCount = old.rowCount ; 
		this.columnCount = old.columnCount ; 
		//////  Create outer arrays 
		this.rows = new Cell[rowCount][] ; 
		this.rowHardLeftEdges = new boolean[rowCount+1][] ; 
		this.rowWithinWordLeftwards = new boolean[rowCount+1][] ; 
		this.columns = new Cell[columnCount][] ; 
		this.columnHardTopEdges = new boolean[columnCount+1][] ; 
		this.columnWithinWordUpwards = new boolean[columnCount+1][] ; 
		//////  Copy inner arrays 
		for (int i=0 ; i<rowCount ; i++) { 
			rows[i] = new Cell[columnCount] ; 
			for (int j=0 ; j<columnCount ; j++) rows[i][j] = new Cell(old.rows[i][j]); 
			rowHardLeftEdges[i] = Arrays.copyOf(old.rowHardLeftEdges[i], columnCount+1); 
			rowWithinWordLeftwards[i] = Arrays.copyOf(old.rowWithinWordLeftwards[i], columnCount+1); 
		}
		for (int j=0 ; j<columnCount ; j++) { 
			columns[j] = new Cell[rowCount] ; 
			for (int i=0 ; i<rowCount ; i++) columns[j][i] = rows[i][j] ; 
			copyIntersticeColumn(old, j); 
		}
		copyIntersticeColumn(old, columnCount); 
		//////  Copy free cells
		{ 
			if (old.freeAcrossCellsByLetter!=null) { 
				this.freeAcrossCellsByLetter = new HashMap<Character, Builder3Flesh.CellSet>(); 
				Set<Entry<Character, CellSet>> entrySet = old.freeAcrossCellsByLetter.entrySet();
				for (Entry<Character, CellSet> entry : entrySet) { 
					CellSet cellSet = new CellSet(); 
					for (Cell oldCell : entry.getValue()) { 
						cellSet.add(rows[oldCell.row][oldCell.column]); 
					}
					this.freeAcrossCellsByLetter.put(entry.getKey(), cellSet); 
				}
			}
			if (old.freeDownCellsByLetter!=null) { 
				this.freeDownCellsByLetter = new HashMap<Character, Builder3Flesh.CellSet>(); 
				Set<Entry<Character, CellSet>> entrySet = old.freeDownCellsByLetter.entrySet();
				for (Entry<Character, CellSet> entry : entrySet) { 
					CellSet cellSet = new CellSet(); 
					for (Cell oldCell : entry.getValue()) { 
						cellSet.add(rows[oldCell.row][oldCell.column]); 
					}
					this.freeDownCellsByLetter.put(entry.getKey(), cellSet); 
				}
			}
		}
		//////  Copy pending words
		if (old.pendingWords!=null) this.pendingWords = new ArrayList<String>(old.pendingWords); 
		//////  Copy other fields
		this.quality = old.quality ; 
		for (PlacedWord oldPlacedWord : old.placedWordList) { 
			PlacedWord newPlacedWord = oldPlacedWord ;  //  WAS new PlacedWord(oldPlacedWord); 
			placedWordList.add(newPlacedWord); 
		}
		placedWordSet.addAll(old.placedWordSet); 
		this.signature = old.signature ; 
		this.isPuzzleBuilding = old.isPuzzleBuilding ; 
	}
	
	/**
	 * Cloning method, convenience method for the cloning constructor {@link #Grid(Grid)}. 
	 * Makes a deep copy. 
	 */
	Grid copy() { 
		return new Grid(this); 
	}
	
	boolean hasLetter(int i, int j) { 
		return rows[i][j].hasLetter(); 
	}
	
	private void copyIntersticeColumn(Grid old, int j) { 
		columnHardTopEdges[j] = Arrays.copyOf(old.columnHardTopEdges[j], rowCount+1); 
		columnWithinWordUpwards[j] = Arrays.copyOf(old.columnWithinWordUpwards[j], rowCount+1); 
	}

	/**
	 * Constructor-assist method. 
	 */
	private void createIntersticeColumn(int j) {
		columnHardTopEdges[j] = new boolean[rowCount+1]; 
		columnWithinWordUpwards[j] = new boolean[rowCount+1]; 
	} 

	/**
	 * Constructor-assist method. 
	 */
	private void fillIntersticeColumn(int j) {
		Arrays.fill(columnHardTopEdges[j], false); 
		columnHardTopEdges[j][0] = true ; 
		columnHardTopEdges[j][rowCount] = true ; 
		Arrays.fill(columnWithinWordUpwards[j], false);
	} 
	
	boolean isWordPlaceable(String word, int row, int column, boolean isAcross) { 
		int wordLength = word.length();
		int i0 = row ; 
		int j0 = column ; 
		int iDelta = isAcross ? 0 : 1 ; 
		int jDelta = isAcross ? 1 : 0 ; 
		//////  Do checks 
		if (0>i0 || i0>=rowCount) return false ; 
		if (0>j0 || j0>=columnCount) return false ; 
		if (isAcross) { 
			if (j0+wordLength>columnCount) return false ; 
		} else { 
			if (i0+wordLength>rowCount) return false ; 
		}
		for (int x=0 ; x<wordLength ; x++) { 
			char wordCh = word.charAt(x); 
			int i = i0+x*iDelta;
			int j = j0+x*jDelta;
			Cell cell = rows[i][j]; 
			char cellCh = cell.ch ; 
			if (x>0) { 
				if (isAcross) { 
					if (rowHardLeftEdges[i][j]) return false ; 
					if (rowWithinWordLeftwards[i][j]) return false ; 
				} else { 
					if (columnHardTopEdges[j][i]) return false ; 
					if (columnWithinWordUpwards[j][i]) return false ; 
				} 
			}
			if (cellCh!=space) { 
				if (cellCh!=wordCh) return false ; 
				if (isAcross ? cell.isInAcross : cell.isInDown) return false ; 
			}
		}
		return true ; 
	}
	
	/**
	 * Places a word in the grid, checking that it doesn't clash with existing words. 
	 * It is assumed to already be capitalized. 
	 * 
	 * @param row Row of first letter
	 * @param column Column of first letter
	 */
	PlacedWord placeWord(String word, int row, int column, boolean isAcross)  { 
		if (placedWordSet.contains(word)) System.err.println("Placing duplicate word '"+word+"' at "+row+","+column+" at "+Str.currentStack()); 
		this.signature = null ; 
//		System.out.println("Placing:  "+word);
		int wordLength = word.length();
		int iDelta = isAcross ? 0 : 1 ; 
		int jDelta = isAcross ? 1 : 0 ; 
		//////  Do checks 
		if (0>row || row>=rowCount) throw new RuntimeException("Row "+row+" is outside grid."); 
		if (0>column || column>=columnCount) throw new RuntimeException("Column "+column+" is outside grid."); 
		if (isAcross) { 
			if (column+wordLength>columnCount) throw new RuntimeException("End row "+(column+wordLength)+" is outside grid: '"+word+"' at "+row+","+column+(isAcross?"A":"D")); 
		} else { 
			if (row+wordLength>rowCount) throw new RuntimeException("End column "+(row+wordLength)+" is outside grid: '"+word+"' at "+row+","+column+(isAcross?"A":"D"));  
		}
		for (int x=0 ; x<wordLength ; x++) { 
			char wordCh = word.charAt(x); 
			int i = row+x*iDelta;
			int j = column+x*jDelta;
			Cell cell = rows[i][j]; 
			char cellCh = cell.ch ; 
			if (x>0) { 
				if (isAcross) { 
					if (rowHardLeftEdges[i][j]) throw new RuntimeException("Overlaps with existing word."); 
					if (rowWithinWordLeftwards[i][j]) throw new RuntimeException("Overlaps with existing word."); 
				} else { 
					if (columnHardTopEdges[j][i]) throw new RuntimeException("Overlaps with existing word."); 
					if (columnWithinWordUpwards[j][i]) throw new RuntimeException("Overlaps with existing word."); 
				} 
			}
			if (cellCh!=space) { 
				if (cellCh!=wordCh) throw new RuntimeException("Word '"+word+"'["+x+"] '"+wordCh+"' clashes with grid letter '"+cellCh+"'"); 
			}
		}
		//////  Add word 
		for (int x=0 ; x<wordLength ; x++) { 
			char wordCh = word.charAt(x); 
			int i = row+x*iDelta;
			int j = column+x*jDelta;
			Cell cell = rows[i][j]; 
			cell.setChar(wordCh, isAcross); 
			if (x>0) { 
				if (isAcross) { 
					rowWithinWordLeftwards[i][j] = true ; 
				} else { 
					columnWithinWordUpwards[j][i] = true ; 
				} 
			}
		} 
		if (isAcross) { 
			rowHardLeftEdges[row][column] = true ; 
			rowHardLeftEdges[row][column+wordLength] = true ; 
		} else { 
			columnHardTopEdges[column][row] = true ; 
			columnHardTopEdges[column][row+wordLength] = true ; 
		} 
		PlacedWord placedWord = new PlacedWord(word, row, column, isAcross, isPuzzleBuilding);
		placedWordList.add(placedWord); 
		placedWordSet.add(word); 
		if (pendingWords!=null) pendingWords.remove(word); 
//		this.lastWord = word ; 
		return placedWord ; 
	}
	
	public void placeWord(String word, Loc loc)  { 
		placeWord(word, loc.row, loc.column, loc.isAcross); 
	}

	Object getSignature() { 
		if (signature==null) { 
			StringBuffer sb = new StringBuffer(); 
			for (Cell[] row : rows) { 
				for (Cell cell : row) { 
					char ch = cell.ch;
					sb.append(ch); 
					if (ch!=Cell.space) { 
						if (cell.isInAcross) 
							if (cell.isInDown) sb.append('+'); 
							else sb.append('-'); 
						else sb.append('|'); 
					}
				}
			}
			this.signature = sb.toString(); 
		}
		return signature ; 
	}

	/**
	 * Sets the {@link #pendingWords} field with all unused words. 
	 */
	public void setPendingWords() { 
		/*
		 * (October 2020) I think this could be simpler. But it works and doesn't chew up a lot of runtime. 
		 */
		Grid.StringSet [] unusedWordsByLength ; 
//		PdfClueless.profiler.enterModule();
		StringList[] wordsByLength = builder.buildInfoAtStart.wordsByLength;
		int longestWordLength = wordsByLength.length - 1 ; // First element is 0-length "words". 
		Set<String> usedWords = new HashSet<String>() ; 
		for (PlacedWord placedWord : placedWordList) { 
			usedWords.add(placedWord.word); 
		}
		unusedWordsByLength = new Grid.StringSet[longestWordLength+1] ; 
		for (int i=1 ; i<=longestWordLength ; i++) { 
			Grid.StringSet wordSet = new Grid.StringSet(wordsByLength[i]);
			wordSet.removeAll(usedWords); 
			unusedWordsByLength[i] = wordSet; 
		}
		this.pendingWords = new ArrayList<String>(); 
		for (int i=1 ; i<=longestWordLength ; i++) { 
			pendingWords.addAll(unusedWordsByLength[i]); 
		}
	}
	
	public List<String> getPendingWords() { 
		if (pendingWords==null) setPendingWords();
		return pendingWords ; 
	}
	
	int getLongestGapLength() { 
		if (longestGapLength==null) { 
			int longestSoFar = 0 ; 
			for (Cell [] row : rows) { 
				int longestHere = getLongestGapLength(row); 
				if (longestHere>longestSoFar) longestSoFar = longestHere ; 
			}
			for (Cell [] column : columns) { 
				int longestHere = getLongestGapLength(column); 
				if (longestHere>longestSoFar) longestSoFar = longestHere ; 
			}
			longestGapLength = longestSoFar ; 
		}
		return longestGapLength ; 
	}
	
	private static int getLongestGapLength(Cell [] line) { 
		int tmp = 0 ; 
		int lineLength = line.length;
		while (tmp<lineLength && line[tmp].hasLetter()) tmp ++ ; 
		if (tmp==lineLength) return 0 ; // There is no gap on this line. 
		int longestSoFar = 0 ; 
		int gapFirst = tmp ; 
		while (gapFirst<lineLength) { 
			/* Here we know: gapFirst is the start of a gap. */
			//////  Find end of gap 
			tmp = gapFirst + 1 ; 
			while (tmp<lineLength && !line[tmp].hasLetter()) tmp ++ ;  
			/* Here we know: tmp is the fist index past the end of the gap (either a letter, or past the end of the line). */
			//////  Accumulate statistics
			int gapLength = tmp - gapFirst ; 
			if (gapLength>longestSoFar) longestSoFar = gapLength ; 
			//////  Advance for next iteration
			while (tmp<lineLength && line[tmp].hasLetter()) tmp ++ ; 
			gapFirst = tmp ; 
		}
		return longestSoFar ; 
	}
	
	int getLongestPendingLength() { 
		if (longestPendingLength==null) { 
			if (pendingWords==null) setPendingWords();
			if (pendingWords.isEmpty()) { 
				this.longestPendingLength = 0 ; 
			} else { 
				this.longestPendingLength = pendingWords.get(pendingWords.size()-1).length(); 
			}
		}
		return longestPendingLength ; 
	}
	
	void destroyPendingWords() { 
		this.pendingWords = null ; 
	}
	
	/**
	 * Returns the word at the given grid cell. 
	 * It doesn't have to be the first letter -- any cell in the word will do. 
	 * If there is no such word, <code>null</code> is returned. 
	 * <p>
	 * This does a dumb search. It is not efficient. 
	 */
	public PlacedWord getWordAt(int row, int column, boolean isAcross) { 
		for (PlacedWord placedWord : placedWordList) { 
			if (placedWord.isAcross!=isAcross) continue ; 
			int rowStart = placedWord.row ; 
			int columnStart = placedWord.column ; 
			int wordLength = placedWord.word.length() ; 
			if (isAcross) { 
				if (rowStart==row && columnStart<=column && column<columnStart+wordLength) return placedWord ; 
			} else { 
				if (columnStart==column && rowStart<=row && row<rowStart+wordLength) return placedWord ; 
			}
		}
		return null ; // No such word found. 
	}
	
	public static class PlacedWord { 
		/* Must be static, because is copied from one grid to another by the copy-constructor. */
		public final String word ; 
		public final int row ; 
		public final int column ; 
		public final boolean isAcross ; 
		public final boolean isPuzzleWord ; 
		PlacedWord(String word, int row, int column, boolean isAcross, boolean isPuzzleWord) { 
			this.word = word ; 
			this.row = row ; 
			this.column = column ; 
			this.isAcross = isAcross ; 
			this.isPuzzleWord = isPuzzleWord ; 
		}
	}
	
	static Grid [] toArray(Collection<Grid> gridList) { 
		int count = gridList.size();
		Grid [] array = new Grid[count] ; 
		gridList.toArray(array); 
		return array ; 
	}
	
	@SuppressWarnings("serial")
	public static class StringSet extends HashSet<String> {
		StringSet(Collection<String> collection) { 
			super(collection); 
		}
	}
	
	/**
	 * Perhaps records the child-grid as one of the best two child-grids of this grid. 
	 * @param qualityMeasure 
	 * 
	 * @see Grid#next1
	 * @see Grid#next2
	 */
	void addChild(Grid child, QualityMeasure qualityMeasure) { 
		child.setQuality(qualityMeasure);
		double childQuality = child.getQuality(); 
		if (next2!=null) { 
			/* Currently have a best & second best child grids. */
			if (childQuality>next2.quality) { 
				if (childQuality>next1.quality) { 
					this.next2 = next1 ; 
					this.next1 = child ; 
				} else { 
					this.next2 = child ; 
				}
			}
		} else if (next1!=null) { 
			/* Currently have only one child grid. */
			this.next2 = child ; 
		} else { 
			/* Currently have no child grid. */ 
			this.next1 = child ; 
		}
	}

	void setQuality(QualityMeasure qualityMeasure) { 
		if (currentQualityMeasure!=qualityMeasure) { 
			this.quality = qualityMeasure.computeQuality(this); 
			this.currentQualityMeasure = qualityMeasure ; 
		}
	}

	public double getQuality() {
		return quality;
	}

	public static void notePuzzleWordsDone(Grid[] grids) {
		for (Grid grid : grids) { 
			grid.notePuzzleWordsDone();
		}
	}

	public void notePuzzleWordsDone() {
		this.isPuzzleBuilding = false ; 
		for (Cell[] row : this.rows) { 
			for (Cell cell : row) { 
				if (cell.hasLetter()) cell.isPuzzleCell = true ; 
			}
		}
	}

	public void writeRow_sysout(int row) { 
		for (int column=0 ; column<columnCount ; column++) System.out.print(rows[row][column].ch);
	}

}
