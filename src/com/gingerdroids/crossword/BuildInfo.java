package com.gingerdroids.crossword;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.gingerdroids.crossword.Builder3Flesh.CellSet;
import com.gingerdroids.utils_java.Str;

public class BuildInfo { 
	
//	final int sideLength ; 
	final int rowCount ; 
	final int columnCount ; 
	
	final String [] words ; 
	
	private final int longestWordLength ; 

	final StringList [] wordsByLength ; 
	
	List<Cell> crossableCellList = new ArrayList<Cell>(); 
	
	Set<String> puzzleWords = new HashSet<String>() ;

	final Map<Character, ArrayList<WordCell>> letterMap; 
	
	BuildInfo(String [] words, int rowCount, int columnCount) { 
		this.rowCount = rowCount ; 
		this.columnCount = columnCount ; 
		this.words = words ; 
		this.letterMap = BuildInfo.fillLetterMap(words); 
		this.longestWordLength = Str.getMaxStringLength(words); 
		this.wordsByLength = new StringList[longestWordLength+1] ;  
		for (int i=1 ; i<=longestWordLength ; i++) wordsByLength[i] = new StringList(); 
		for (String word : words) wordsByLength[word.length()].add(word); 
	} 
	
	BuildInfo add(List<Cell> crossableCellList) { this.crossableCellList = crossableCellList ; return this ; } 
	
	BuildInfo add(Set<String> puzzleWords) { this.puzzleWords = puzzleWords ; return this ; } 

	static Map<Character, ArrayList<WordCell>> fillLetterMap(String [] words) { 
		Map<Character,ArrayList<WordCell>> letterMap = new HashMap<Character, ArrayList<WordCell>>(); 
		for (String word : words) { 
			int length = word.length(); 
			for (int x=0 ; x<length ; x++) { 
				WordCell wordLetter = new WordCell(word, x); 
				ArrayList<WordCell> list = letterMap.get(wordLetter.letter); 
				if (list==null) { 
					list = new ArrayList<WordCell>();
					letterMap.put(wordLetter.letter, list); 
				}
				list.add(wordLetter); 
			}
		}
		return letterMap ; 
	}
	
	static class WordCell { 
		final String word ; 
		final int index ; 
		final char letter ; 
		boolean isCrossed = false ; 
		WordCell(String word, int index) { 
			this.word = word ; 
			this.index = index ; 
			this.letter = word.charAt(index); 
		}
		public String toString() { 
			StringBuffer sb = new StringBuffer(); 
			sb.append(letter); 
			sb.append(index); 
			sb.append('-'); 
			sb.append(word); 
			return sb.toString(); 
		}
	}
	
	class StringList extends ArrayList<String> {}
}