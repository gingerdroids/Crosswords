package com.gingerdroids.crossword;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.gingerdroids.crossword.Grid.PlacedWord;

public class UsualQualityMeasure implements QualityMeasure {
	
	public static final char space = ' ' ; 
	
	final double lengthsFactor ; 
	final double pendingFactor ; 
	final double fullSpanFactor ; 
	final double symSpanFactor ; 
	final double crossingsFactor ; 
	final double surroundedFactor ; 
	final boolean isSurroundedBoth ; 
	final double needinessFactor ; 
	final double virginFactor ; // Must be positive, usually less-equal one. Small values discourage words with no crossing. 
	final double [] edgePenalties ; 
	
	/**
	 * Constructor. 
	 * 
	 * @param lengthsFactor     Favours adding words, especially long words. Number of word-letters in grid. (Crossing are counted twice.) 
	 * @param pendingFactor     Favours grids with (at least one) space for a long word. 
	 * @param crossingsFactor	Favours crossings. Number of crossings in the grid. 
	 * @param fullSpanFactor	Favours crossable letters with much space on either side. Note - Decreases as new words fill vacant space. 
	 * @param symSpanFactor 	Favours crossable letters with much space on both sides. Note - Decreases as new words fill vacant space. 
	 * @param surroundedFactor	Discourages words alongside each other. 
	 * @param isSurroundedBoth
	 * @param needinessFactor	Discourages words without crossings, or with few crossings. 
	 * @param virginFactor		Tweaks neediness-measure. Must be positive, usually less-equal one. Small values discourage words with no crossing. 
	 * @param edgePenalties		Penalty for a word parallel to an edge, and close to it. 
	 */
	public UsualQualityMeasure(double lengthsFactor, double pendingFactor, double crossingsFactor, double fullSpanFactor, double symSpanFactor, double surroundedFactor, boolean isSurroundedBoth, double needinessFactor, double virginFactor, double [] edgePenalties) { 
		this.lengthsFactor = lengthsFactor ; 
		this.pendingFactor = pendingFactor ; 
		this.crossingsFactor = crossingsFactor ; 
		this.fullSpanFactor = fullSpanFactor ; 
		this.symSpanFactor = symSpanFactor ; 
		this.surroundedFactor = surroundedFactor ; 
		this.isSurroundedBoth = isSurroundedBoth ; 
		this.needinessFactor = needinessFactor ; 
		this.virginFactor = virginFactor ; 
		this.edgePenalties = normalizeEdgePenalties(edgePenalties); 
	}
	
	private static double [] normalizeEdgePenalties(double [] in) { 
		if (in==null || in.length==0) return in ; 
		int outLength = 0 ; 
		for (int i=0 ; i<in.length ; i++) if (in[i]>0) outLength = i+1 ; 
		if (outLength==0) return null ; 
		double [] out = new double[outLength]; 
		for (int i=0 ; i<outLength ; i++) out[i] = Math.max(in[i], 0); 
		return out ; 
	}

	public double computeQuality(Grid grid) {
		/*
		 * As the grid fills and we have most of the required words in the grid, we care less about available space and more about appearance. 
		 */
		ArrayList<PlacedWord> placedWordList = grid.placedWordList ;
		Cell[][] rows = grid.rows ; 
		Cell[][] columns = grid.columns ; 
		final int rowCount = grid.rowCount ; 
		final int columnCount = grid.columnCount ; 
		//////  Compute word-lengths measure - sum of the lengths of the placed words
		double wordLengthsMeasure ; // Magnitude: number of filled cells
		if (lengthsFactor!=0) { 
			int sumLengths = 0 ; 
			for (PlacedWord placedWord : placedWordList) { 
				sumLengths += placedWord.word.length(); 
			}
			wordLengthsMeasure = sumLengths ; 
		} else { 
			wordLengthsMeasure = 0.0 ; // Does not contribute. 
		}
		//////  Compute pending word length measure 
		double pendingLengthsMeasure ; 
		if (pendingFactor!=0) { 
			int longestGapLength = grid.getLongestGapLength(); 
			int sum = 0 ; 
			List<String> pendingWords = grid.getPendingWords();
			for (int i=pendingWords.size()-1 ; i>=0 ; i--) { // From longest to shortest
				String pendingWord = pendingWords.get(i); 
				int length = pendingWord.length(); 
				if (length<=longestGapLength) break ; 
				sum += length ; 
			}
			pendingLengthsMeasure = - sum ; 
		} else { 
			pendingLengthsMeasure = 0.0 ; // Does not contribute. 
		}
		//////  Compute crossed cells measure - how many cells are in both an across-word and a down-word
		double crossedCellsMeasure ; // Magnitude: number of crossing cells
		if (crossingsFactor!=0) { 
			int crossingsCount = 0 ; 
			for (Cell[] row : rows) { 
				for (Cell cell : row) {
					if (cell.isInAcross && cell.isInDown) crossingsCount ++ ; 
				}
			}
			crossedCellsMeasure = crossingsCount ; 
		} else { 
			crossedCellsMeasure = 0.0 ; // Does not contribute. 
		}
		//////  Compute reachable spaces measure - how many gaps are there to place a word (with one crossing), weighted by size of the word (kind of).  
		double fullSpanMeasure ; // Magnitude: Decreasing fraction of unused cells
		double symSpanMeasure ; // Magnitude: Decreasing fraction of unused cells
		if (fullSpanFactor!=0 || symSpanFactor!=0) { 
			double [] spanMeasures = new double[2] ; // Index 0 is full span measure, index 1 is symmetric span measure. 
			Arrays.fill(spanMeasures, 0);
			for (Cell[] row : rows) computeSpaceMeasures(spanMeasures, grid, row); 
			for (Cell[] column : columns) computeSpaceMeasures(spanMeasures, grid, column); 
			fullSpanMeasure = spanMeasures[0] ; 
			symSpanMeasure = spanMeasures[1] ; 
		} else { 
			fullSpanMeasure = 0.0 ; // Does not contribute. 
			symSpanMeasure = 0.0 ; // Does not contribute. 
		}
		//////  Compute surrounded cells measure - how many cells are too difficult to cross (ie, are filled but not crossed, and have a parallel words on one/both sides. 
		double surroundedCellMeasure ; // Magnitude: Increasing fraction of filled cells
		if (surroundedFactor!=0) { 
			int surroundedCount = 0 ; 
			for (int i=1 ; i<rowCount-1 ; i++) { 
				for (int j=0 ; j<columnCount ; j++) { 
					Cell cell = rows[i][j] ; 
					if( cell.isInAcross) { 
						if (cell.isInDown) { 
							/* Here we know: Cell is crossed both ways. Is not in surrounded-count. */
						} else { 
							/* Here we know: Cell is in an across-word only. */
							boolean isAboveFree = isFree(rows, i, j-1);
							boolean isBelowFree = isFree(rows, i, j+1);
							if (isSurroundedBoth) { 
								if (!isAboveFree && !isBelowFree) surroundedCount ++ ; 
							} else { 
								if (!isAboveFree || !isBelowFree) surroundedCount ++ ; 
							}
						}
					} else { 
						if (cell.isInDown) { 
							/* Here we know: Cell is in a down-word only. */
							boolean isLeftFree = isFree(rows, i-1, j);
							boolean isRightFree = isFree(rows, i+1, j);
							if (isSurroundedBoth) { 
								if (!isLeftFree && !isRightFree) surroundedCount ++ ; 
							} else { 
								if (!isLeftFree || !isRightFree) surroundedCount ++ ; 
							}
						} else { 
							/* Here we know: Cell is not in a word. Is not in surrounded-count. */
						}
					}
				}
			}
			surroundedCellMeasure = - surroundedCount ; 
		} else { 
			surroundedCellMeasure = 0.0 ; // Does not contribute. 
		}
		//////  Compute crossing-neediness measure - sum over words of: how much does this word need a crossing?  
		double needCrossingsMeasure ; // Magnitude: Fraction of filled cells
		if (needinessFactor!=0) { 
			double sumOverWords = 0 ; 
			for (PlacedWord placedWord : placedWordList) { 
				sumOverWords += computeCrossingNeediness(grid, placedWord); 
			}
			needCrossingsMeasure = - sumOverWords ; 
		} else { 
			needCrossingsMeasure = 0.0 ; // Does not contribute. 
		}
		//////  Compute favour-centre measure 
		double favourCentreMeasure = 0 ; 
		if (edgePenalties!=null && edgePenalties.length!=0) { 
			int epLength = edgePenalties.length ; 
			for (PlacedWord placedWord : placedWordList) { 
				if (placedWord.isAcross) { 
					int dimensionLength = grid.rowCount ; 
					int coord = placedWord.row ; 
					int invCoord = dimensionLength - coord - 1 ; 
					if (coord<epLength) favourCentreMeasure -= edgePenalties[coord] ; 
					else if (invCoord<epLength) favourCentreMeasure -= edgePenalties[invCoord] ; 
				} else { 
					int dimensionLength = grid.columnCount ; 
					int coord = placedWord.column ; 
					int invCoord = dimensionLength - coord - 1 ; 
					if (coord<epLength) favourCentreMeasure -= edgePenalties[coord] ; 
					else if (invCoord<epLength) favourCentreMeasure -= edgePenalties[invCoord] ; 
				}
			}
		}
		//////  Combine measures
		double quality = 
				wordLengthsMeasure * lengthsFactor + 
				pendingLengthsMeasure * pendingFactor + 
				fullSpanMeasure * fullSpanFactor + 
				symSpanMeasure * symSpanFactor + 
				crossedCellsMeasure * crossingsFactor + 
				surroundedCellMeasure * surroundedFactor + 
				needCrossingsMeasure * needinessFactor + 
				favourCentreMeasure + 
				0 ; 
		return quality ; 
	}
	
	private boolean isFree(Cell[][] rows, int i, int j) {
		if (i<0 || j<0) return false ; 
		if (i>=rows.length) return false ; 
		Cell [] row = rows[i] ; 
		if (j>=row.length) return false ; 
		if (row[j].hasLetter()) return false ; 
		return true;
	}
	
	public double computeCrossingNeediness(Grid grid, PlacedWord placedWord) { 
		if (!placedWord.isPuzzleWord) return 0 ; 
		int row = placedWord.row ; 
		int column = placedWord.column ; 
		int length = placedWord.word.length() ; 
		boolean isAcross = placedWord.isAcross ; 
		double needyLetters = 0 ; 
		int crossed = 0 ; 
		for (int i=0 ; i<length ; i++) { 
			Cell cell = grid.columns[column][row];
			boolean isCellCrossed = cell.isInAcross && cell.isInDown ; 
			if (isCellCrossed) crossed ++ ; 
			if (!isCellCrossed) needyLetters += 2 * (length-i) / (double)length ; 
			if (isAcross) column++ ; else row ++ ;  
		}
		double neediness = needyLetters * length / (crossed+virginFactor);
		return neediness ; 
	}
	
	/**
	 * Compute value for usable space across a row (or down a column. 
	 * Looks for all places a word can be placed that will only cross a single occupied cell. 
	 * Each place is scored according to how long the word is. Words shorter than 'uselessLength' are not counted. 
	 * 
	 * @param measures Returns the measures. Index 0 is the full-span measure, index 1 is the symmetric-span measure. 
	 */
	private void computeSpaceMeasures(double [] measures, Grid grid, Cell[] line) { 
		int lineLength = line.length;
		int fullSpansSum = 0 ; // Excludes scrappy little words - ie, <= deadSize
		int symmetricSpansSum = 0 ; // Excludes scrappy little words - ie, <= deadSize, and shortens the spans to be symmetric around the crossing. 
		int firstCrossing = 0 ; 
		while (firstCrossing<lineLength && line[firstCrossing].ch!=space) firstCrossing ++ ; 
		/* Here we know: 'firstCrossing' is the index of the first filled cell in the line, or if there are no cells, is the line length. */ 
		if (firstCrossing==lineLength) return ; // Measures are zero if there are no words to cross. 
		int wordStart = 0 ; 
		int secondCrossing_maybe = firstCrossing + 1 ; 
		/* Here we know: Beginning (inclusive) at wordStart, firstCrossing is the first filled cell. */
		/* Assertion "current-span": It is possible to put a word (however short) from 'wordStart' to 'secondCrossing_maybe-1' (inclusive), and it will cross exactly one filled cell at firstCrossing. */
		/* Here we know: Assertion "current-span" is true. */
		while (secondCrossing_maybe<lineLength) { 
			//////  Find span covering the filled cell at firstCrossing. 
			/* Here we know: Invariant "secondCrossing_maybe" is true. */
			while (secondCrossing_maybe<lineLength && line[secondCrossing_maybe].ch==space) { 
				secondCrossing_maybe ++ ; 
				/* Here we know: Assertion "current-span" is true. */
			}
			/* Here we know: 
			 * Cell 'secondCrossing_maybe' is either a filled cell, or off the edge of the grid. 
			 * Assertion "current-span" is true. 
			 * But from 'wordStart' to 'secondCrossing_maybe' either crosses two filled cells, or goes off the edge of the grid. 
			 */
			//////  Update the measure statistics. 
			fullSpansSum += computeFullSpanMeasure(grid, wordStart, secondCrossing_maybe-1) ;
			symmetricSpansSum += computeSymmetricSpanMeasure(grid, wordStart, firstCrossing, secondCrossing_maybe-1) ;
			//////  Advance variables for next iteration. 
			wordStart = firstCrossing + 1 ; 
			firstCrossing = secondCrossing_maybe ; 
			secondCrossing_maybe = firstCrossing + 1 ; 
		}
		fullSpansSum += computeFullSpanMeasure(grid, wordStart, secondCrossing_maybe-1) ; // Adding in last span on line
		symmetricSpansSum += computeSymmetricSpanMeasure(grid, wordStart, firstCrossing, secondCrossing_maybe-1) ; // Adding in last span on line
		//////  Bye bye 
		measures[0] += fullSpansSum ; 
		measures[1] += symmetricSpansSum ; 
	}

	private int computeFullSpanMeasure(Grid grid, int spanStart, int spanEnd) { 
		int spanLength = spanEnd - spanStart + 1 ; 
		int spanMeasure = Math.min(spanLength, grid.getLongestPendingLength()); 
		if (spanMeasure<0) spanMeasure = 0 ;
		return spanMeasure;
	} 
	
	private int computeSymmetricSpanMeasure(Grid grid, int spanStart, int crossing, int spanEnd) { 
		int spanLength = Math.min(crossing-spanStart, spanEnd-crossing) * 2 + 1 ; 
		int spanMeasure = Math.min(spanLength, grid.getLongestPendingLength()); 
		if (spanMeasure<0) spanMeasure = 0 ;
		return spanMeasure;
	} 
}
