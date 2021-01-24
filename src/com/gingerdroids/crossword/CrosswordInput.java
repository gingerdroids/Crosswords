package com.gingerdroids.crossword;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.gingerdroids.utils_java.ConfigLineReader.Pair;

/**
 * The crossword's words, clues and some configuration. 
 * <p>
 * Currently (oct20), the only constructor reads this from a file.  
 */
public class CrosswordInput {
	
	private static final String SECTION_CLUES = "Clues" ; 
	private static final String SECTION_MISC = "Misc" ; 
	
	public final Clue [] clues ;

	public final String title ; 

	public final String note ; 
	
	public final int rowCount ; 
	public final int columnCount ; 
	
	public final boolean isDemo ; 
	public final boolean suppressOccurrences ; 
	
	public final Float gridFontSize ; 
	public final Float cluesFontSize ; 
	public final int cluesColumnCount ; 
	
	public CrosswordInput(File file) throws IOException { 
		Map<String, Pair[]> sectionsMap = Clue.readSections(file); 
		Pair[] cluesSection = sectionsMap.get(SECTION_CLUES); 
		if (cluesSection==null) { 
			cluesSection = sectionsMap.get(Clue.SECTION_UNTITLED); 
		}
		this.clues = Clue.extractClues(cluesSection);
		Pair[] miscSection = sectionsMap.get(SECTION_MISC); 
		// TODO Handle missing Clues section nicely. 
		//////  Set misc-section fields
		/* Note: left-half of pair probably contains trailing space. */
		//// Extract map
		HashMap<String,String> miscMap = new HashMap<String, String>(); 
		if (miscSection!=null) { 
			for (Pair pair : miscSection) { 
				miscMap.put(pair.left.trim(), pair.right); 
			}
		} 
		//// Set fields
		this.title = miscMap.get("title"); 
		this.note = miscMap.get("note"); 
		this.isDemo = miscMap.containsKey("demo"); 
		this.suppressOccurrences = miscMap.containsKey("suppress_occurrences"); 
		int defaultSize = getInt(miscMap, "size", 12); 
		this.rowCount = getInt(miscMap, "rows", defaultSize); 
		this.columnCount = getInt(miscMap, "columns", defaultSize); 
		this.gridFontSize = getFloat(miscMap, "grid_font_size", 12.0f); 
		this.cluesFontSize = getFloat(miscMap, "clues_font_size", 12.0f); 
		this.cluesColumnCount = getInt(miscMap, "clue_columns", 1); 
	}
	
	private static int getInt(HashMap<String,String> map, String key, int defaultInt) { 
		if (key==null) return defaultInt ; 
		String intStr = map.get(key); 
		if (intStr==null) return defaultInt ; 
		return Integer.valueOf(intStr); 
	}
	
	private static float getFloat(HashMap<String,String> map, String key, float defaultFloat) { // TOSO:::::: Copy to _twoColumns
		if (key==null) return defaultFloat ; 
		String FloatStr = map.get(key); 
		if (FloatStr==null) return defaultFloat ; 
		return Float.valueOf(FloatStr); 
	}
	

//	private int extractSize(String sizeStr) { 
//		if (sizeStr!=null) { 
//			return Integer.valueOf(sizeStr); 
//		} else { 
//			return 12 ; // TO DO Compute more sensible value from 'clues' 
//		}
//	}

}
