package com.gingerdroids.crossword;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

import com.gingerdroids.utils_java.Str;

/**
 * Holds the words from a dictionary. 
 * <p>
 * The data structure allows access by given-letter in given-position. 
 * <p>
 * You may find the English Open Word List website useful. 
 * 
 * @see https://diginoodles.com/projects/eowl
 */
public class WordBank { 
	
	private static final int maxWordLength = 10 ; 

	private static final int maxChar = 128;

	private static IndexedWord[] emptyArray = new IndexedWord[]{};
	
	private IndexedWord [][][] wordArrays = new IndexedWord[maxChar][maxWordLength+1][]; 
	
	private WordBank(String [] allWords) { 
		IndexedWordList [][] tmpLists = new IndexedWordList[maxChar][maxWordLength+1]; 
		for (int i=' ' ; i<'z' ; i++) for (int j=1 ; j<=maxWordLength ; j++) tmpLists[i][j] = new IndexedWordList(); 
		for (String word : allWords) { 
			int wordLength = word.length(); 
			if (wordLength>maxWordLength) continue ; 
			for (int index=0 ; index<wordLength ; index++) { 
				tmpLists[word.charAt(index)][wordLength].add(new IndexedWord(word, index)); 
			}
		}
		for (int i=' ' ; i<'z' ; i++) { 
			for (int j=1 ; j<=maxWordLength ; j++) { 
				IndexedWordList list = tmpLists[i][j]; 
				IndexedWord [] array ; 
				if (list.isEmpty()) { 
					array = emptyArray; 
				} else { 
					array = new IndexedWord[list.size()] ; 
					list.toArray(array); 
				}
				wordArrays[i][j] = array ; 
			}
		}
	}
	
	/**
	 * Returns all words which have the given letter, and are the given length. 
	 * If a word has the letter twice, it will appear in the returned array twice, with different <code>index</code> values. 
	 */
	public IndexedWord [] getWords(char letter, int length) { 
		if (length>maxWordLength) return emptyArray ; 
		return wordArrays[letter][length] ; 
	}
	
	/**
	 * Reads a file of words into a {@link WordBank}. 
	 * The file is assumed to have a header - a series of non-empty lines, terminating in an empty line (which may have white space). 
	 * After that, every line should have a single word on it. 
	 */
	public static WordBank loadWordBank(File file) throws IOException { 
		return new WordBank(loadFile_HasHeader(file)) ; 
	}

	@SuppressWarnings("serial")
	private class IndexedWordList extends ArrayList<IndexedWord> {} 
	
	static class IndexedWord { 
		final String word ; 
		final char letter ; 
		final int index ; // Index of 'letter' in 'word'
		private IndexedWord(String word, int index) { 
			this.word = word ; 
			this.letter = word.charAt(index); 
			this.index = index ; 
		}
	}
	
	public static WordBank loadWordBank_EOWL(File wordBanksDir) throws IOException { 
		//////  Find the folder with the EOWL corpus files. 
		if (!wordBanksDir.exists()) { 
			System.err.println(wordBanksDir.getPath()+" does not exist. Continuing without a word-bank."); 
			return null ; 
		}
		if (!wordBanksDir.isDirectory()) { 
			System.err.println(wordBanksDir.getPath()+" is not a folder. Continuing without a word-bank."); 
			return null ; 
		}
		File corpusDir = findEowlCorpusDir(wordBanksDir); 
		//////  Load the words from the corpus 
		Set<String> wordSet = new HashSet<String>(); 
		File [] corpusFiles = corpusDir.listFiles(new FilenameFilter() {
			final String endStr = "words.txt" ; 
			final int expectedNameLength = endStr.length()+2 ; 
			@Override
			public boolean accept(File dir, String filename) {
				filename = filename.toLowerCase(); 
				if (filename.length()>expectedNameLength) return false ; 
				return filename.endsWith(endStr);
			}
		}); 
		final int alphabetLength = 26 ; 
		if (corpusFiles.length!=alphabetLength) { 
			System.err.println("Expected "+alphabetLength+" corpus files. Found "+corpusFiles.length+". But continuing regardless."); 
		}
		for (File file : corpusFiles) { 
			String [] words = loadFile_EOWL(file); 
			wordSet.addAll(Arrays.asList(words)); 
		}
		//////  Exclude words from 'exclude.txt'. 
		File exclusionFile = new File(wordBanksDir, "exclude.txt"); 
		if (exclusionFile.exists()) { 
			String[] exclusionWords = loadFile_HasHeader(exclusionFile); 
			wordSet.removeAll(Arrays.asList(exclusionWords)); 
		}
		//////  Build word-bank 
		final String[] words = Str.toArray(wordSet);
		WordBank wordBank = new WordBank(words); 
		return wordBank ; 
	}
	
	private static File findEowlCorpusDir(File dir) { 
		/* Oh, the joys of unzip and folders within folders. Not. */
		File corpusDir = getSubdir(dir, "LF Delimited Format"); 
		if (corpusDir!=null) return corpusDir ; 
		File subdir = getSubdir(dir, "EOWL-"); 
		if (subdir==null) return null ; 
		return findEowlCorpusDir(subdir); // If this recursion doesn't bottom out, the file system is a mess. 
	}
	
	private static File getSubdir(File dir, final String filenameStart) { 
		File[] matchingFiles = dir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String filename) {
				boolean isOK = filename.startsWith(filenameStart);
				return isOK;
			}
		}); 
		for (File file : matchingFiles) { 
			if (file.isDirectory()) return file ; 
		}
		return null ; 
	}
	
	/**
	 * Returns all the words in the given files. Words are all upper-cased. 
	 */
	public static WordBank loadFilesInDir(File dir, String [] filenames) throws IOException {
		Set<String> wordSet = new HashSet<String>(); 
		for (String filename : filenames) { 
			File file = new File(dir, filename); 
			String[] fileWords = loadFile_HasHeader(file); 
			wordSet.addAll(Arrays.asList(fileWords)); 
		}
		final String[] words = Str.toArray(wordSet); 
		return new WordBank(words); 
	}

	/**
	 * Returns all the words in the given files, except words in the exclusion-file. Words are all upper-cased. 
	 */
	public static WordBank loadFilesInDirExcluding(File dir, String [] filenames, String exclusionFilename) throws IOException {
		final String[] words = loadArrayFromDirExcluding(dir, filenames, exclusionFilename); 
		return new WordBank(words); 
	}

	public static String[] loadArrayFromDirExcluding(File dir, String[] filenames, String exclusionFilename) throws IOException {
		Set<String> wordSet = new HashSet<String>(); 
		for (String filename : filenames) { 
			File file = new File(dir, filename); 
			String[] fileWords = loadFile_HasHeader(file); 
			wordSet.addAll(Arrays.asList(fileWords)); 
		}
		if (exclusionFilename!=null) { 
			File exclusionFile = new File(dir, exclusionFilename); 
			String[] exclusionWords = loadFile_HasHeader(exclusionFile); 
			wordSet.removeAll(Arrays.asList(exclusionWords)); 
		}
		final String[] words = Str.toArray(wordSet);
		return words;
	}

	/**
	 * Returns all the words in the file, in upper case. 
	 * Assumes there is a header terminated by an empty line. 
	 */
	private static String[] loadFile_HasHeader(File file) throws IOException { 
		boolean allowMultipleWordsOnLine = false ; 
		//////  Open file and skip header
		FileReader fileReader = new FileReader(file); 
		BufferedReader reader = new BufferedReader(fileReader); 
		while (true) { 
			String line = reader.readLine(); 
			if (line==null) break ; 
			if (line.trim().length()==0) break ; 
			if (line.toLowerCase().indexOf("#wordsonly")>=0) allowMultipleWordsOnLine = true ; 
		}
		/* Here we know: have just read the first empty line. (Empty allows white space on otherwise empty line.) */
		//////  Read list of words, build array
		ArrayList<String> list = new ArrayList<String>(); 
		while (true) { 
			String line = reader.readLine(); 
			if (line==null) break ; 
			if (allowMultipleWordsOnLine) { 
				String[] lineWords = line.split(" "); 
				for (String word : lineWords) { 
					if (isWordAcceptable(word)) list.add(word.toUpperCase()); 
				}
			} else { 
				String word = line.trim();
				if (word.length()==0) continue ; 
				if (word.indexOf(' ')>=0) throw new RuntimeException("Multiple words on a line"); 
				if (!isWordAcceptable(word)) continue ; 
				list.add(word.toUpperCase()); 
			}
		}
		//////  Bye bye 
		reader.close(); 
		String [] array = Str.toArray(list);
		return array;
	}
	
	
	/**
	 * Returns all the words in the file, in upper case. 
	 * <p>
	 * It assumes EOWL format, one word per line. No blanks, no empties, no comments. 
	 */
	private static String[] loadFile_EOWL(File file) throws IOException { 
		//////  Open file and skip header
		FileReader fileReader = new FileReader(file); 
		BufferedReader reader = new BufferedReader(fileReader); 
		//////  Read list of words, build array
		ArrayList<String> list = new ArrayList<String>(); 
		while (true) { 
			String line = reader.readLine(); 
			if (line==null) break ; 
			String word = line.trim();
			if (word.length()==0) continue ; 
			if (word.indexOf(' ')>=0) throw new RuntimeException(file.getName()+": Multiple words on a line "+Str.quoted(line)); 
			if (!isWordAcceptable(word)) continue ; 
			if (!isWordSimpleAscii(word)) continue ; // EOWL has non-simple letters. Reject them. 
			list.add(word.toUpperCase()); 
		}
		//////  Bye bye 
		reader.close(); 
		String [] array = Str.toArray(list);
		return array;
	}
	
	private static boolean isWordSimpleAscii(String word) { 
		for (int i=0 ; i<word.length() ; i++) {
			if (word.charAt(i)>=128) return false ; 
		} 
		return true ; 
	}
	
	private static boolean isWordAcceptable(String word) { 
		int wordLength = word.length();
		if (wordLength>maxWordLength) return false ; 
		if (wordLength>1) return true ; 
		//// Some single letter words are OK. 
		switch (word.charAt(0)) { 
		case 'A': 
		case 'I': 
			return true ; 
		}
		return false ; 
	}

}
