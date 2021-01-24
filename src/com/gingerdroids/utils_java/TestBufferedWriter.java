package com.gingerdroids.utils_java;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

/**
 * Compares what is written through a {@link BufferedWriter} against a ground-truth in a file. 
 * <p>
 * Doesn't play nicely with {@link PrintWriter}. 
 * I suspect the new-line calls aren't compatible with {@linkplain BufferedWriter}. 
 * (I know, coz I wrote the test suite with {@linkplain PrintWriter}. Then fixed it. Grumble.)
 */
public class TestBufferedWriter extends BufferedWriter { 
	
	private static final int bufferSize = 5 ; 

	private final BufferedReader expectedReader ; 
	
	private CircularBuffer<String> recentExpected = new CircularBuffer<>(bufferSize); 
	
	/**
	 * The line from the expected file that is currently being written by the caller. 
	 * <p>
	 * This is <code>null</code> if we are at the start of a line. 
	 */
	private String currentText = null ; 
	
	/**
	 * Current line number. 
	 * The first line has number<code>1</code>. 
	 */
	private int currentLineNumber = 0 ; 

	/**
	 * Column of the next character to be written. 
	 * The first character on a line has number <code>1</code>. 
	 */
	private int nextColumnNumber = Integer.MIN_VALUE ; // Meaningless when currentText is null. 
	
	private boolean isClosed = false ; 

	public TestBufferedWriter(File expectedFile) throws FileNotFoundException { 
		super(new DummyWriter()); 
		if (!expectedFile.canRead()) throw new FileNotFoundException("Could not read file "+expectedFile.getAbsolutePath()); 
//		this.file = expectedFile ; 
		this.expectedReader = new BufferedReader(new FileReader(expectedFile)); 
	}
	
	@Override
	public void close() throws IOException { 
		this.isClosed = true ; 
		if (currentText==null) { 
			String expectedLine = expectedReader.readLine(); 
			if (expectedLine!=null) { 
				throw new TestWriterException(this, "Closing writer when expected-file has more lines."); 
			}
		} else if (nextColumnNumber<=currentText.length()) { 
			throw new TestWriterException(this, "Closing writer when current expected-line has more characters."); 
		} 
		expectedReader.close(); 
	}

	@Override
	public void flush() throws IOException { 
		if (isClosed) throw new TestWriterException(this, "Writer is closed."); 
	}

	@Override
	public void newLine() throws IOException { 
		if (isClosed) throw new TestWriterException(this, "Writer is closed."); 
		if (nextColumnNumber-1!=currentText.length()) { 
			throw new TestWriterException(this, "Writing new line before end of current line in expected-text."); 
		}
		readExpectedLine();
	}

	@Override
	public void write(int c) throws IOException { 
		if (isClosed) throw new IOException("Writer is closed."); 
		if (currentText==null) readExpectedLine(); 
		if (currentText==null) { 
			throw new TestWriterException(this, "Writing new line, but no more lines in expected-text."); 
		}
		if (nextColumnNumber>currentText.length()) { 
			throw new TestWriterException(this, (char)c, "Writing character beyond end of line in expected-text."); 
		}
		char expectedChar = currentText.charAt(nextColumnNumber-1);
		if (expectedChar!=c) { 
			throw new TestWriterException(this, (char)c, "Expected character different to written character."); 
		}
		nextColumnNumber ++ ; 
	}

	private void readExpectedLine() throws IOException {
		this.currentText = expectedReader.readLine(); 
		recentExpected.write(currentText);
		currentLineNumber ++ ; 
		nextColumnNumber = 1 ;
	}

	@Override
	public void write(char[] cbuf, int off, int len) throws IOException { 
		if (isClosed) throw new IOException("Writer is closed."); 
		if (off+len>cbuf.length) throw new RuntimeException("'cbuf' too short. Error in caller."); 
		for (int i=0 ; i<len ; i++) write(cbuf[off+i]); 
	}
	
	@Override
	public void write(String str, int off, int len) throws IOException { 
		if (isClosed) throw new IOException("Writer is closed."); 
		if (off+len>str.length()) throw new RuntimeException("'str' too short. Error in caller."); 
		for (int i=0 ; i<len ; i++) write(str.charAt(off+i)); 
	}

	@Override
	public void write(String str) throws IOException { 
		write(str, 0, str.length()); 
	}
	
	@SuppressWarnings("serial")
	public static class TestWriterException extends IOException { 
		
		public final CircularBuffer<String> recentExpected ; 
		
		public final int lineNumber ; 
		
		public final int columnNumber ; 
		
		public final Character writtenChar ; 
		
		TestWriterException(TestBufferedWriter testWriter, String message) { 
			this(testWriter, null, message); 
		}
		
		TestWriterException(TestBufferedWriter testWriter, Character writtenChar, String message) { 
			super(message); 
			this.recentExpected = testWriter.recentExpected ; 
			this.lineNumber = testWriter.currentLineNumber ; 
			this.columnNumber = testWriter.nextColumnNumber ; 
			this.writtenChar = writtenChar ; 
		}

		public void report() {
			System.out.println(getMessage()); 
			System.out.println("Problem at line "+lineNumber+" column "+columnNumber); 
			System.out.println(); 
			String mostRecent = recentExpected.getMostRecent(); 
			if (mostRecent!=null) { 
				for (String recentLine : recentExpected.getRecent()) { 
					System.out.println(recentLine); 
				}
				for (int i=0 ; i<columnNumber-1 ; i++) System.out.print("\\u00B7");
				System.out.println("|"); 
				System.out.println(mostRecent.substring(0, columnNumber-1)+writtenChar);
				System.out.println(); 
			} else { 
				System.out.println("No expected-text read."); 
			}
			System.out.flush(); 
		}
	}
	
	static class ClassTest { 

		private File buildTestFile(String[] lines) throws IOException {
			File file = File.createTempFile("tw-", "-tw.txt"); 
			PrintWriter writer = new PrintWriter(file); 
			for (String line : lines) { 
				writer.println(line); 
			}
			writer.close(); 
			file.deleteOnExit(); // Deleted explicitly later, but belt&braces. 
			return file ;
		}
		
		private void checkFails(String [] lines, File file, String expectedMessage) throws IOException { 
			try { 
				checkFile(lines, file); 
				Str.quoted(expectedMessage); 
				throw new RuntimeException("Should have thrown an exception before here. Expected message "+Str.quoted(expectedMessage)); 
			} catch (TestWriterException e) { 
				String actualMessage = e.getMessage();
				if (actualMessage.equals(expectedMessage)) { 
					/* This is the expected functionality. Return normally!. */
				} else { 
					throw new RuntimeException("Expect message "+Str.quoted(expectedMessage)+", actual message "+Str.quoted(actualMessage)); 
				}
			}
		}
		
		private void checkFile(String [] lines, File file) throws IOException { 
			TestBufferedWriter writer = new TestBufferedWriter(file); 
			for (String line : lines) { 
				writer.write(line);
				writer.newLine();
				writer.flush(); 
			}
			writer.close(); 
		}
		
		ClassTest() { 
			try {
				String[] aLines = new String [] {"abc"};
				String[] adLines = new String [] {"abc", "def"};
				String[] adgLines = new String [] {"abc", "def", "ghi"};
				String[] adgLines_plus = new String [] {"abc", "deef", "ghi"};
				String[] adgLines_minus = new String [] {"abc", "df", "ghi"};
				//////  Build files 
				File aFile = buildTestFile(aLines);
				File adFile = buildTestFile(adLines);
				File adgFile = buildTestFile(adgLines);
				File adgFile_plus = buildTestFile(adgLines_plus);
				File adgFile_minus = buildTestFile(adgLines_minus);
				//////  Check correct files pass
				checkFile(aLines, aFile);
				checkFile(adLines, adFile);
				checkFile(adgLines, adgFile);
				checkFile(adgLines_plus, adgFile_plus);
				checkFile(adgLines_minus, adgFile_minus);
				//////  Check failures 
				checkFails(aLines, adFile, "Closing writer when current expected-line has more characters."); 
				checkFails(adgLines, adFile, "Writing new line, but no more lines in expected-text."); 
				checkFails(adLines, aFile, "Writing new line, but no more lines in expected-text."); 
				checkFails(adLines, adgFile, "Closing writer when current expected-line has more characters."); 
				checkFails(adgLines_plus, adgFile, "Expected character different to written character."); 
				checkFails(adgLines_minus, adgFile, "Expected character different to written character."); 
				//////  Delete files 
				aFile.delete(); 
				adFile.delete(); 
				adgFile.delete(); 
				adgFile_plus.delete(); 
				adgFile_minus.delete(); 
				//////  Passed! Bye bye.  
				System.out.println("Passed test suite "+this.getClass().getCanonicalName()); 
			} catch (IOException e) { 
				throw new RuntimeException(e); 
			} 
		}
	}
	
//	public static void main(String [] args) { 
//		new ClassTest(); 
//	}

}


