package com.gingerdroids.crossword;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.gingerdroids.utils_java.ConfigLineReader;
import com.gingerdroids.utils_java.ConfigLineReader.Pair;
import com.gingerdroids.utils_java.Str;

/**
 * A clue which could be used in the crossword. 
 * The answer might be a word or a phrase. 
 * There might be hint part - clue for the crossword-solver - or not. 
 * @author guy
 *
 */
public class Clue { 
	
	static final String SECTION_UNTITLED = "";
	
	/**
	 * Answer, as it appeared in the input file. 
	 */
	public final String rawAnswer ; 
	
	/**
	 * The words of the answer, in order, but canonical - upper case, no punctuation. 
	 */
	public final String[] orderedWords ; 
	
	/**
	 * Unique words in the answer, in canonical form - upper case, no punctuation. 
	 * These words are unique within this clue. They might be present in other clues. 
	 */
	final String[] uniqueWords ; 
	
	public final String hint ; 
	
	Clue(Pair pair) { 
		this(pair.left, pair.right) ;
	}
	
	Clue(String answer, String hint) { 
		this.rawAnswer = answer ; 
		this.orderedWords = phraseToCanonicalWords(rawAnswer); 
		this.uniqueWords = Str.toArray(new HashSet<String>(Arrays.asList(orderedWords))); 
		this.hint = hint ; 
	}
	
	static Clue[] readClues(File file) throws IOException { 
		Pair[] pairs = readPairs(file); 
		return extractClues(pairs); 
	}

	static Clue[] extractClues(Pair[] pairs) {
		int count = pairs.length; 
		Clue[] clues = new Clue[count] ; 
		for (int i=0 ; i<count ; i++) clues[i] = new Clue(pairs[i]); 
		return clues ;
	}

	static Pair[] readPairs(File file) throws IOException {
		Pair[] pairs = ConfigLineReader.readAllPairs(file, "-- ");
		return pairs;
	}
	
	static Map<String,Pair[]> readSections(File file) throws IOException { 
		Map<String, Pair[]> result = new HashMap<String, ConfigLineReader.Pair[]>();
		Pair[] allPairs = readPairs(file); 
		int pairIndex = 0 ; 
		while (pairIndex<allPairs.length) { 
			/* Each iteration of loop processes one section. */
			//// Extract section title 
			Pair firstPairOfSection = allPairs[pairIndex] ; 
			String sectionTitle ; // Untitled sections have empty-string as title, not null. 
			String leftPart = firstPairOfSection.left;
			if (!Str.hasContent(leftPart)) { 
				/* Normal case. */
				pairIndex ++ ; 
				sectionTitle = firstPairOfSection.right ; 
			} else { 
				/* Untitled section - must be at start of file. */
				sectionTitle = SECTION_UNTITLED ; 
			}
			//// Extract pairs for section
			ArrayList<Pair> pairList = new ArrayList<ConfigLineReader.Pair>(); 
			while (pairIndex<allPairs.length && Str.hasContent(allPairs[pairIndex].left)) { 
				pairList.add(allPairs[pairIndex]); 
				pairIndex ++ ; 
			}
			//// Add section to 'result'
			Pair[] sectionPairs = Pair.toArray(pairList); 
			result.put(sectionTitle, sectionPairs); 
		}
		return result ; 
	}
	
	
	
	static String [] getUniqueWords(Clue[] clues, int maxLength) { 
		Set<String> wordSet = new HashSet<String>(); 
		for (Clue clue : clues) { 
			for (String word : clue.uniqueWords) { 
				if (word.length()>maxLength) continue ; 
				wordSet.add(word); 
			}
		}
		String[] uniqueWords = Str.toArray(wordSet); 
		return uniqueWords ; 
	}

	static String [] phraseToCanonicalWords(String in) { 
		String[] rawWords = in.toUpperCase().split("[ -]");
		List<String> depunctedList = new ArrayList<String>(); 
		for (String rawWord : rawWords) { 
			String trimmedWord = rawWord.trim();
			if (trimmedWord.length()==0) continue ; 
			String depunctedWord = trimmedWord.replaceAll("[^\\w\\d]", "");
			depunctedList.add(depunctedWord); 
		}
		return Str.toArray(depunctedList); 
	}

}
