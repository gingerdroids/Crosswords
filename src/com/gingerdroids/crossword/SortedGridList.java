package com.gingerdroids.crossword;

import java.util.HashSet;
import java.util.Set;

import com.gingerdroids.crossword.apps.PdfClueless;
import com.gingerdroids.utils_java.SortedList;
import com.gingerdroids.utils_java.Util;

/**
 * Extracts the best however-many grids from all the grids added to it. 
 * <p>
 * Every grid should have the {@link Grid#quality} field set. 
 * <p>
 * All the grids added - and therefore compared - should be in some sense be peers. 
 * They should be similarly progressed through the layout process. 
 */
public class SortedGridList extends SortedList { 
	
	/*
	 * Efficiency note (oct20):
	 * The parent class SorteddList uses a linear algorithm to keep the kept-grids sorted. 
	 * Fearing this could be a big time-hit, I measured how much time was spent in this module. 
	 * Not much. 
	 */
	
	final QualityMeasure qualityMeasure ; 
	
	
	Set<Object> seenSignatures = new HashSet<Object>() ; 

	public SortedGridList(QualityMeasure qualityMeasure, int maxKept) { 
		super(maxKept);
		if (qualityMeasure==null) throw new NullPointerException("QualityMeasure arg cannot be null."); 
		this.qualityMeasure = qualityMeasure ; 
	}
	
	/* Needs to be thread-safe coz of parallelization in Build3Flesh.addOneFleshWord(). */
	synchronized boolean addGrid(Grid grid, boolean wantCopy) { 
		Object signature = grid.getSignature();
		if (seenSignatures.contains(signature)) { 
			return false ; 
		}
		seenSignatures.add(signature); 
		grid.setQuality(qualityMeasure); 
		double quality = grid.getQuality();
		final double thresholdQuality = getQualityThreshold(); 
		if (quality<=thresholdQuality) return false ; 
		Grid storableGrid = wantCopy ? grid.copy() : grid ; 
		boolean wasAdded = super.add(storableGrid, -quality); // SortedList uses negated quality. 
		if (!wasAdded) throw new RuntimeException("We checked the threshold above... should have been OK."); 
		return true ; 
	}
	
	public int addGrids(Grid [] grids, boolean wantCopy) { 
		int addedCount = 0 ; 
		for (Grid grid : grids) if (addGrid(grid, wantCopy)) addedCount ++ ;
		return addedCount ;  
	}
	
	/**
	 * Returns a newly created array of {@link Grid}. 
	 */
	public Grid[] getSortedGrids() { 
		Object[] sortedObjects = super.getSortedObjects(); 
		int length = sortedObjects.length;
		Grid [] sortedGrids = new Grid[length] ; 
		for (int i=0 ; i<length ; i++) { 
			sortedGrids[i] = (Grid) sortedObjects[i] ; 
		}
		return sortedGrids ; 
	}
	
	double getQualityThreshold() { 
		return - getCurrentThreshold() ; 
	}
	
	public static Grid[] resort(Grid [] in, QualityMeasure qualityMeasure, int maxKept) { 
		SortedGridList out = new SortedGridList(qualityMeasure, maxKept); 
		out.addGrids(in, false); 
		return out.getSortedGrids() ; 
	}

}
