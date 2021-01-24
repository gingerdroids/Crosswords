package com.gingerdroids.crossword;

import com.gingerdroids.utils_java.SortByDouble;

class SortByPendingWords extends SortByDouble {
	
	public final Grid [] sortedGrids ; 

	SortByPendingWords(Grid[] grids) {
		super(grids, computeMeasures(grids));
		int length = grids.length;
		sortedGrids = new Grid[length] ; 
		for (int i=0 ; i<length ; i++) sortedGrids[i] = (Grid) results[i] ; 
	}
	
	private static double [] computeMeasures(Grid[] grids) { 
		int length = grids.length;
		double [] measures = new double[length] ; 
		for (int i=0 ; i<length ; i++) measures[i] = grids[i].getPendingWords().size(); 
		return measures ; 
	}

}
