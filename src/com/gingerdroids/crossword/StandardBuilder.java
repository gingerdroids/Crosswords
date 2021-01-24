package com.gingerdroids.crossword;

import java.io.IOException;

import com.gingerdroids.crossword.Grid;
import com.gingerdroids.utils_java.Util;

/**
 * A {@link Builder} subclass for plain crosswords: puzzle words dense on grid, with many crossings. 
 */
public class StandardBuilder extends Builder { 
	
//	/**
//	 * How many grids are kept between the fleshing iterations (and as output of the fleshing stage). 
//	 * <p>
//	 * (Oct20) I'd like to see this larger than 256 - say 1024 or 2048 - but it hugely slows the code. 
//	 */
//	protected static final int gridsKeptCount_flesh = 256 ; 
//	
//	private final WordBank wordBank;
	
	public StandardBuilder(CrosswordInput crosswordInput, WordBank wordBank, QualityMeasureFactory qualityMeasureFactory) throws IOException {
		super(crosswordInput, wordBank, qualityMeasureFactory); 
//		this.wordBank = wordBank ; 
	}

	/*
	public Grid[] buildGrids_ASIDE_201020(String[] clueWords, int rowCount, int columnCount, int desiredGridCount) throws IOException {
		Grid [] currentGrids ; 
		currentGrids = new Grid[]{new Grid(this)}; 
		//////  (Maybe) Build spine-only grids 
		if (Util.ffalse) {
			Grid[] spineGrids = buildSpine(clueWords, rowCount, columnCount); 
			printProgress("Spine", spineGrids); 
			currentGrids = spineGrids ; 
		}
		//////  (Maybe) Add ribs onto spine-grids
		if (Util.ffalse) { 
			currentGrids = addRibs(rowCount, columnCount, currentGrids);
		}
		//////  Add bulk of clue words to grids
		{
			Grid[] fleshGrids = new Builder3Flesh(gridsKeptCount_flesh).addFlesh(buildInfoAtStart, qualityMeasureFactory, currentGrids);
			printProgress("Flesh", fleshGrids); 
			currentGrids = fleshGrids ; 
		}
		//////  Mark all grids as 'puzzle done'
		Grid.notePuzzleWordsDone(currentGrids);
		//////  Coat around outside of grids 
		Builder4Coat builderCoat = new Builder4Coat(wordBank); 
		currentGrids = builderCoat.buildCoat(currentGrids); 
		printProgress("Coat", currentGrids); 
		//////  Select best of the grids   
		SortedGridList finalGridList = new SortedGridList(qualityMeasure_final, 1); 
		finalGridList.addGrids(currentGrids, false); 
		currentGrids = finalGridList.getSortedGrids(); 
		printProgress("Final", currentGrids); 
		//////  (Maybe) Add single letter words  
		if (Util.ffalse) {
			addSingleLetterWords(currentGrids, clueWords);
		}
		//////  Bye bye 
		return currentGrids ;
	}
	*/

	
}
