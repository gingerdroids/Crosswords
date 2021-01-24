package com.gingerdroids.crossword;

import java.io.File;
import java.io.IOException;

import com.gingerdroids.crossword.apps.PdfClueless;
import com.gingerdroids.utils_java.Util;

/**
 * A {@link Builder} subclass for plain crosswords: puzzle words dense on grid, with many crossings. 
 */
public class CluelessBuilder extends Builder { 
	
//	/**
//	 * How many grids are kept between the fleshing iterations (and as output of the fleshing stage). 
//	 * <p>
//	 * (Oct20) I'd like to see this larger than 256 - say 1024 or 2048 - but it hugely slows the code. 
//	 */
//	protected static final int gridsKeptCount_flesh = 512 ; 
	
	
//	public CluelessBuilder(File cluesFile, WordBank wordBank) throws IOException {
//		super(cluesFile); 
//		this.wordBank = wordBank ; 
//	}
	
	public CluelessBuilder(CrosswordInput crosswordInput, WordBank wordBank, QualityMeasureFactory qualityMeasureFactory) throws IOException {
		super(crosswordInput, wordBank, qualityMeasureFactory); 
	}
	
}
