package com.gingerdroids.utils_java;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;

public class ConfigLineReader extends BufferedReader { 

	private String commentStart = "#";
	private String nameValueSeparator = ":";
	
	/** Whether to skip empty lines, or return them as a <code>null</code> pair. **/
	private boolean isEmptyLineSkipped = true ; 
	
	/** Set true when reading a line fails. **/
	private boolean isEOF = false ; 
	
	public ConfigLineReader(Reader inReader) {
		super(inReader);
	}

	public String getNameValueSeparator() {
		return nameValueSeparator;
	}

	public ConfigLineReader setNameValueSeparator(String nameValueSeparator) {
		this.nameValueSeparator = nameValueSeparator;
		return this ;
	}

	public String getCommentStart() {
		return commentStart;
	}

	public ConfigLineReader setCommentStart(String commentStart) {
		this.commentStart = commentStart;
		return this ;
	}
	
	public boolean isEmptyLineSkipped() {
		return isEmptyLineSkipped;
	}

	public void setEmptyLineSkipped(boolean isEmptyLineSkipped) {
		this.isEmptyLineSkipped = isEmptyLineSkipped;
	}
	
	public boolean isEOF() {
		return isEOF;
	}

	/**
	 * Reads a line from the file, as per the parent class's method, but filters out lines starting with "#" and (optionally) blank lines. 
	 * <p>
	 * When appropriate, sets {@link #isEOF}. 
	 * 
	 * @see #isEmptyLineSkipped
	 */
	@Override
	public String readLine() throws IOException { 
		while (true) { 
			String line = super.readLine(); 
			if (line==null) { 
				this.isEOF = true ; 
				return line ; 
			}
			if (line.startsWith(commentStart)) continue ; 
			if (line.trim().length()==0) { 
				if (isEmptyLineSkipped) { 
					continue ; 
				} else { 
					return null ; 
				}
			}
			return line ; 
		}
	}
	
	/**
	 * Reads the next line, and converts it into a {@link Pair}. Note that it will skip empty and comment lines, as per {@link #readLine()}.  
	 * <p>
	 * If the line contains {@link #nameValueSeparator}, the <code>right</code> is after the splitter. 
	 * If there are multiple splitters on the line, the <em>last</em> one is used. 
	 * The <code>left</code> is before the splitter. 
	 * The splitter itself is not included, so the lengths of <code>left</code> and <code>right</code> are less than the length of the line. 
	 * <p>
	 * If the line contains no splitter, the <code>left</code> contains the entire line, and the <code>right</code> is null. 
	 * <p>
	 * At the end-of-file, <code>null</code> is returned. 
	 * @return
	 * @throws IOException
	 */
	public Pair readPair() throws IOException { 
		String line = readLine(); 
		if (line==null) return null ; 
		int splitterIndex = line.lastIndexOf(nameValueSeparator);
		if (splitterIndex>=0) { 
			int rightStart = splitterIndex+nameValueSeparator.length();
			String leftText = line.substring(0, splitterIndex).trim();
			if (leftText.length()==0) leftText = null ; 
			String rightText = line.substring(rightStart, line.length()).trim();
			if (rightText.length()==0) rightText = null ; 
			return new Pair(line, leftText, rightText); 
		} else { 
			return new Pair(line, line, null); 
		}
	}
	
	public static Pair [] readPairsUntilBreak(ConfigLineReader lineReader) throws IOException { 
		ArrayList<Pair> pairList = new ArrayList<ConfigLineReader.Pair>(); 
		while(true) { 
			Pair pair = lineReader.readPair(); 
			if (pair==null) break ; 
			if (lineReader.isEOF) break ; 
			pairList.add(pair); 
		}
		if (lineReader.isEOF) lineReader.close(); 
		return Pair.toArray(pairList); 
	}
	
	public static Pair[] readAllPairs(File file, String nameValueSeparator) throws IOException { 
		FileReader fileReader = new FileReader(file); 
		ConfigLineReader lineReader = new ConfigLineReader(fileReader); 
		if (nameValueSeparator!=null) lineReader.setNameValueSeparator(nameValueSeparator); 
		ArrayList<Pair> pairList = new ArrayList<ConfigLineReader.Pair>(); 
		while(true) { 
			Pair pair = lineReader.readPair(); 
			if (lineReader.isEOF) break ; 
			pairList.add(pair); 
		}
		lineReader.close(); 
		return Pair.toArray(pairList); 
	}
	
	public static class Pair { 
		
		public final String left ; 
		public final String right ; 
		public final String line ; 
		
		private Pair(String line, String left, String right) { 
			this.left = left ; 
			this.right = right ; 
			this.line = line ; 
		}

		public static Pair[] toArray(ArrayList<Pair> pairList) {
			Pair[] pairs = new Pair[pairList.size()]; 
			pairList.toArray(pairs); 
			return pairs ;
		}
	}

}
