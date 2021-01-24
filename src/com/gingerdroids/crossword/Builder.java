package com.gingerdroids.crossword;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.gingerdroids.crossword.Grid.PlacedWord;
import com.gingerdroids.utils_java.Str;

public abstract class Builder {
	
	public static final int printCount = 8 ; 
	
	// Parameters for qualityMeasure_final
	static final double lengthsFactor_final = 0;
	static final double pendingFactor_final = 0;
	static final double fullSpanFactor_final = 0.0;
	static final double symSpanFactor_final = 0.0;
	static final double crossingsFactor_final = 1;
	static final double surroundedFactor_final = 0;
	static final double needinessFactor_final = 5;
	static final double virginFactor_final = 0.1;
	static final boolean isSurroundedBoth_final = false;
	static final double[] edgePenalties_final = new double [] {40, 20};
	
	/**
	 * How many grids are kept between the fleshing iterations (and as output of the fleshing stage). 
	 * <p>
	 * (Oct20) I'd like to see this larger than 256 - say 1024 or 2048 - but it hugely slows the code. 
	 */
	protected static final int candidateCountDuringFlesh = 512 ; 
	
	protected static final int candidateCountDuringCoat = 256 ;
	
	protected final QualityMeasureFactory qualityMeasureFactory ; 
	
	/**
	 * The quality-measure for choosing the best grid(s) at the end of fleshing and coating, the ones to be written out. 
	 * 
	 * @see UsualQualityMeasure#UsualQualityMeasure(double, double, double, double, double, double, boolean, double, double, double[])
	 * for a description of the parameter semantics. 
	 */
	protected static final UsualQualityMeasure qualityMeasure_final = new UsualQualityMeasure(lengthsFactor_final, pendingFactor_final, fullSpanFactor_final, symSpanFactor_final, crossingsFactor_final, surroundedFactor_final, isSurroundedBoth_final, needinessFactor_final, virginFactor_final, edgePenalties_final);

	public final CrosswordInput crosswordInput;
	public final BuildInfo buildInfoAtStart;
	protected int rowCount;
	protected int columnCount;
	protected Clue[] clues;
	protected String[] clueWords;
	protected final WordBank wordBank;

	public Builder(CrosswordInput crosswordInput, WordBank wordBank, QualityMeasureFactory qualityMeasureFactory) {
		this.crosswordInput = crosswordInput ; 
		this.wordBank = wordBank ; 
		this.qualityMeasureFactory = qualityMeasureFactory ; 
		this.rowCount = crosswordInput.rowCount; 
		this.columnCount = crosswordInput.columnCount; 
		this.clues = crosswordInput.clues; 
		int maxSideLength = Math.max(rowCount, columnCount); 
		this.clueWords = Clue.getUniqueWords(clues, maxSideLength); 
		buildInfoAtStart = new BuildInfo(clueWords, rowCount, columnCount) ; 
		printProgress("Clue words ("+clueWords.length+") read", null);
	}

	private static Cell getCellForIndex(Grid grid, PlacedWord placedWord, int chIndex) {
		return placedWord.isAcross ? grid.rows[placedWord.row][placedWord.column+chIndex] : grid.rows[placedWord.row+chIndex][placedWord.column];
	}

	/**
	 * Invokes all the work to build a crossword. 
	 */
	public Crossword buildCrossword() throws IOException { 
		Crossword[] crosswords = buildCrosswords(); 
		return crosswords[0] ; 
	}

	public Crossword[] buildCrosswords() throws IOException {
		Grid [] grids = buildGrids(printCount);
		int gridCount = grids.length;
		if (gridCount==0) throw new RuntimeException("Builder returned no grids."); 
		Crossword [] crosswords = new Crossword[gridCount] ; 
		for (int i=0 ; i<gridCount ; i++) crosswords[i] = numberWordsAndPhrases(clues, grids[i]);
		return crosswords;
	}

	public Grid[] buildGrids(int desiredGridCount) throws IOException {
		//////  Sanity check 
		int sideLength = Math.max(rowCount, columnCount); 
		for (String word : clueWords) { 
			if (word.length()>sideLength) throw new RuntimeException("Word '"+word+"' is longer than "+sideLength); 
		}
		/*
		 * Originally, there were four stages to adding words. 
		 * Stage 1 added four words (skeleton), forming a cross centred at the centre of the grid. (Builder1Spine)
		 * Stage 2 placed words (ribs) crossing these four skeleton words. (Builder2Ribs)
		 * Stage 3 placed words wherever possible (fleshing). 
		 * These placed all the puzzle words. (Except, very historically, the single-letter words were done after coating.)
		 * Stage 4 placed extra non-puzzle (coat) words from the word bank. 
		 * 
		 * I've since replaced stage 1 with placing a single word in may places. Stage 2 is omitted. Stages 3&4 remain. 
		 */
		Grid [] currentGrids ; 
		currentGrids = new Grid[]{new Grid(this)}; 
		//////  Add puzzle words to grid
		currentGrids = new Builder1Init(this, clueWords).build().getGrids(); 
		currentGrids = new Builder3Flesh(candidateCountDuringFlesh).addFlesh(buildInfoAtStart, qualityMeasureFactory, currentGrids);
		//////  Administrivia, shifting from adding puzzle words to coating. 
		currentGrids = SortedGridList.resort(currentGrids, qualityMeasure_final, desiredGridCount); 
		Grid.notePuzzleWordsDone(currentGrids);
		//////  Coat around outside of grids 
		if (wordBank!=null) { 
			Builder4Coat builderCoat = new Builder4Coat(wordBank); 
			for (Grid grid : currentGrids) { 
				builderCoat.buildCoat(grid);
			}
		} else { 
			System.out.println(); 
			System.out.println("There is no word bank to coat the puzzle words with words from a dictionary."); 
		}
		//////  Bye bye 
		return currentGrids ;
	}

	/**
	 * Prints progress so far to the console. 
	 * Currently (nov20) shows how many words in each grid, how many grids have been kept, 
	 * and quality measures of various grids.  
	 */
	protected static void printProgress(String stage, Grid[] grids) { 
		System.out.print(stage); 
		if (grids!=null) { 
			if (grids.length==0) throw new RuntimeException("Zero grids in array."); 
			System.out.print("\t "+grids[0].placedWordList.size()+" words");
			System.out.print(" ("+grids.length+"):  "); 
			for (int i=0 ; i<grids.length ; ) { 
				System.out.print(""+grids[i].getQuality()+"  "); 
				if (i==0) i=1 ; else i*=2 ; 
			}
		}
		System.out.println(); 
		}

	private Crossword numberWordsAndPhrases(Clue[] clues, Grid grid) {
			Crossword crossword;
			//////  Build puzzle word lists
			HashMap<String, PuzzleWord> mapWordToPuzzleWord = new HashMap<String, PuzzleWord>(); // Words the human must guess. 
			{
				ArrayList<PuzzleWord> wordList = new ArrayList<PuzzleWord>(); 
				for (PlacedWord placedWord : grid.placedWordList) { 
					if (placedWord.isPuzzleWord) { 
						PuzzleWord puzzleWord = new PuzzleWord(placedWord); 
						wordList.add(puzzleWord); 
						mapWordToPuzzleWord.put(placedWord.word, puzzleWord); 
					}
				}
			}
			grid.mapWordToPuzzleWord = mapWordToPuzzleWord ; 
			//////  Build list of phrases in puzzle, keyed by Clue. 
			Map<Clue,PuzzlePhrase> mapClueToPuzzlePhrase = new HashMap<Clue, PuzzlePhrase>(); 
			{
				for (Clue clue : clues) { 
					for (String word : clue.uniqueWords) { 
						if (mapWordToPuzzleWord.containsKey(word)) { 
							/* The body of this executes for all words in clues, which words are in a puzzle.
							 * Words which are in multiple clues will run through this multiple times (which we want). 
							 */
							PuzzlePhrase phrase = mapClueToPuzzlePhrase.get(clue); 
							if (phrase==null) { 
								phrase = new PuzzlePhrase(clue, mapWordToPuzzleWord); 
								mapClueToPuzzlePhrase.put(clue, phrase); 
	//								phraseList.add(phrase); 
							}
						}
					}
				}
			}
			//////  Sort the puzzle words, by position in grid - equivalent to sorting them by number. 
			int puzzleWordCount = mapWordToPuzzleWord.size();
			PuzzleWord [] puzzleWords_sorted = new PuzzleWord[puzzleWordCount] ; // Not yet sorted, but soon will be. 
			{
				mapWordToPuzzleWord.values().toArray(puzzleWords_sorted); 
				Arrays.sort(puzzleWords_sorted, new Comparator<PuzzleWord>() {
					public int compare(PuzzleWord left, PuzzleWord right) {
						if (left.row==right.row) {
							if (left.column==right.column) return 0 ; // Happens if Across word and Down word share a starting cell. 
							return left.column < right.column ? -1 : 1 ; 
						}
						return left.row < right.row ? -1 : 1 ; 
					}
				}); 
			}
			//////  Number the words and phrases
			{
				PuzzlePhrase.Numberer phraseNumberer = new PuzzlePhrase.Numberer(); 
				int nextWordNumber = 1 ; 
				for (int i=0 ; i<puzzleWordCount ; i++) { 
					PuzzleWord puzzleWord = puzzleWords_sorted[i];
					PuzzleWord prevWord = i>0 ? puzzleWords_sorted[i-1] : null ; 
					int wordNumber = puzzleWord.isSameCell(prevWord) ? prevWord.getWordNumber() : nextWordNumber++ ; 
					puzzleWord.setWordNumber(wordNumber); 
					grid.rows[puzzleWord.row][puzzleWord.column].wordNumber = wordNumber ; 
					for (PuzzlePhrase phrase : puzzleWord.phrases) { 
						phraseNumberer.noteUse(phrase); 
					}
				}
			}
			//////  Sort the puzzle phrases 
			PuzzlePhrase [] sortedPuzzlePhrases ; 
			{
				Collection<PuzzlePhrase> phraseList = mapClueToPuzzlePhrase.values(); 
				sortedPuzzlePhrases = new PuzzlePhrase[phraseList.size()] ; 
				phraseList.toArray(sortedPuzzlePhrases); 
				Arrays.sort(sortedPuzzlePhrases, new Comparator<PuzzlePhrase>() {
					public int compare(PuzzlePhrase left, PuzzlePhrase right) {
						return left.getPhraseNumber() - right.getPhraseNumber();
					}
				}); 
			}
			crossword = new Crossword(crosswordInput, grid, grid.placedWordList, puzzleWords_sorted, sortedPuzzlePhrases);
			return crossword;
		}

	private static void printMissingWords(Grid grid, String [] allWords) { 
		Set<String> placedWords = new HashSet<String>(); 
		for (PlacedWord placedWord : grid.placedWordList) { 
			placedWords.add(placedWord.word); 
		}
		Set<String> missingWords = new HashSet<String>(); 
		missingWords.addAll(Arrays.asList(allWords)); 
		missingWords.removeAll(placedWords); 
		for (String missingWord : missingWords) { 
			System.out.print(missingWord+" "); 
		}
		System.out.println(); 
	}

	private static void printGrids(Grid[] grids, int sideLength) throws IOException { 
		if (grids.length==0) { 
			System.out.println("No grids in array at "+Str.currentStack()); 
			return ; 
		}
		//////  Print words in first grid
		if (grids.length>0) {
			Grid grid0 = grids[0] ; 
			ArrayList<PlacedWord> placedWordList = grid0.placedWordList; 
			System.out.print(""+placedWordList.size()+": "); 
			for (PlacedWord placedWord : placedWordList) { 
				System.out.print(placedWord.word+" "); 
			}
			System.out.println(); 
			System.out.println(); 
		}
		new StdoutGridWriter(grids[0], true).writeGrid(); 
		//////  Print all the grids
		/*
		if (App.ffalse) {
			StdoutGridWriter.Queue queue = new StdoutGridWriter.Queue(sideLength, 4, 3); 
			queue.showQuality(); 
			for (Grid grid : grids) { 
				queue.addPerhapsPrint(grid); 
			}
			queue.print(); 
		}
		 */
		//////  Bye bye
		System.out.println("------------------------"); 
		System.out.println(); 
	}

	private static String [] stringToUniqueArray(String in) { 
		String[] rawWords = in.toUpperCase().split("[ -]");
		Set<String> depunctedSet = new HashSet<String>(); 
		for (String rawWord : rawWords) { 
			String trimmedWord = rawWord.trim();
			if (trimmedWord.length()==0) continue ; 
			String depunctedWord = trimmedWord.replaceAll("[^\\w\\d]", "");
			//System.out.println(rawWord+" --> "+depunctedWord); 
			depunctedSet.add(depunctedWord); 
		}
		//		System.out.print(""+depunctedSet.size()+" unique words: "); 
		//		for (String word : depunctedSet) { 
		//			System.out.print(word+" "); 
		//		}
		//		System.out.println(); 
		//		System.out.println(); 
		return Str.toArray(depunctedSet); 
	}

}