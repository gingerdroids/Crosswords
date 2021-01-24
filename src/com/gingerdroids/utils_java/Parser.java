package com.gingerdroids.utils_java;

import java.io.File;
import java.io.IOException;

public class Parser { 
	
	protected final String [] allTokens ; 
	
	protected final int tokenCount ; 
	
	public Parser(String [] allTokens) { 
		this.allTokens = allTokens ; 
		this.tokenCount = allTokens.length ; 
	}
	
	public Parser(File file) throws IOException { 
		this(FileTo.words(file)); 
	}
	
	public abstract class ElemProcessor<Elem> { 
		
		public abstract boolean isMatch(int startIndex) throws Exception ; 
		
		/**
		 * Scans through the element. 
		 * @return The first index after the element. 
		 */
		public abstract int scanFromStart(int startIndex) throws Exception ; 
		
		public abstract Elem getElement(int startIndex, int finishIndex) throws Exception ; 
		
	}

}
