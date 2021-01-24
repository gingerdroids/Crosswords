package com.gingerdroids.crossword;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import com.gingerdroids.utils_java.Str;

/**
 * Class which writes grid to stdout. Useful for dev & debugging.  
 */
class StdoutGridWriter { 
	
	protected final Grid grid;
	protected final int rowCount;
	protected final int columnCount;
	protected final boolean wantInterstices;

	StdoutGridWriter(Grid grid, boolean wantInterstices) { 
		this.grid = grid;
		this.rowCount = grid.rowCount ; 
		this.columnCount = grid.columnCount ; 
		this.wantInterstices = wantInterstices ; 
	}

	protected void writeDiagonalInterstice(int i, int j) {
		char diagonalInterstice = ' ' ; // WAS  '\u00B7';
		System.out.print(diagonalInterstice); 
	}

	protected void writeVerticalInterstice(int i, int j) { 
		boolean isClosed = false ; 
		isClosed |= grid.columnHardTopEdges[j][i] ; 
		if (j<columnCount && i>0 && i<rowCount) { 
			Cell cellAbove = grid.rows[i-1][j];
			Cell cellHere = grid.rows[i][j];
			isClosed |= cellAbove.hasLetter() && !cellAbove.isInDown ; 
			isClosed |= cellHere.hasLetter() && !cellHere.isInDown ; 
		}
		System.out.print(isClosed ? '\u2014' : ' '); 
	}

	protected void writeCell(int i, int j) { 
		System.out.print(this.grid.rows[i][j].ch); 
	}

	protected void writeHorizontalInterstice(int i, int j) { 
		boolean isClosed = false ; 
		isClosed |= this.grid.rowHardLeftEdges[i][j] ; 
		if (i<rowCount && j>0 && j<columnCount) { 
			Cell cellLeft = grid.rows[i][j-1] ; 
			Cell cellHere = grid.rows[i][j] ; 
			isClosed |= cellLeft.hasLetter() && !cellLeft.isInAcross ; 
			isClosed |= cellHere.hasLetter() && !cellHere.isInAcross ; 
		}
		System.out.print(isClosed ? '|' : ' '); 
	}
	
	protected void finishRow() throws IOException {
		System.out.println(); 
	}
	
	protected void finishGrid() throws IOException {
		for (int i=0 ; i<3 ; i++) System.out.println(); 
	}
	
	void writeQualityRow() { 
		StringWriter writer = new StringWriter(); 
		int columnCount = grid.columnCount; 
		int fullLength = wantInterstices ? 2*columnCount+1 : columnCount ; 
		int contentLength = fullLength - 1 ; 
		String qualityStr = Double.toString(grid.getQuality()); 
		if (qualityStr.length()>contentLength) { 
			System.out.print(qualityStr.substring(0, contentLength)); 
		} else if (qualityStr.length()<contentLength) { 
			System.out.print(qualityStr); 
			System.out.print(Str.makeSpaceString(contentLength-qualityStr.length())); 
		} else { 
			System.out.print(qualityStr); 
		}
		System.out.print(' '); 
	}
	
	protected void writeGrid() throws IOException { 
		for (int i=0 ; i<rowCount ; i++) { 
			if (wantInterstices) { 
				finishRow(); 
			}
			writeLetterRow(i); 
			finishRow(); 
		}
		if (wantInterstices) { 
			writeEdgeRow(rowCount); 
			finishRow(); 
		}
		finishGrid(); 
	}

	void writeEdgeRow(int i) throws IOException { 
		for (int j=0 ; j<columnCount ; j++) { 
			writeDiagonalInterstice(i, j); 
			writeVerticalInterstice(i, j); 
		}
		writeVerticalInterstice(i, columnCount); 
	}

	protected void writeLetterRow(int i) throws IOException {
		for (int j=0 ; j<columnCount ; j++) { 
			if (wantInterstices) { 
				writeHorizontalInterstice(i, j); 
			}
			writeCell(i, j); 
		}
		if (wantInterstices) { 
			writeHorizontalInterstice(i, columnCount);
		}
	}

	static class Queue { 
		
		final int sideLength;
		final private ArrayList<Grid> printQueue = new ArrayList<Grid>();
		private final int gap;
		private final int linesAfter; 
		private boolean wantQuality = false ; 
		
		private final static int screenWidthChars = 320 ; 

		private final int printHorizontalGap = 4 ;
		private final int gridsPerLine; 
		
		public Queue(int sideLength, int gap, int linesAfter) { 
			this.sideLength = sideLength ; 
			this.gap = gap ; 
			this.linesAfter = linesAfter ; 
			gridsPerLine = screenWidthChars / (sideLength+printHorizontalGap); 
		}
		
		void showQuality() { 
			this.wantQuality = true ; 
		}

		void addPerhapsPrint(Grid grid) throws IOException { 
			printQueue.add(grid); 
			if ((printQueue.size()+1)*(sideLength+printHorizontalGap)>screenWidthChars) { 
				writeList(); 
				printQueue.clear(); 
			}
		}

		void print() throws IOException {
			if (!printQueue.isEmpty()) { 
				writeList(); 
				printQueue.clear(); 
			}
		}
		
		private void writeList() throws IOException { 
			String gapStr = Str.makeSpaceString(gap); 
			int gridCount = printQueue.size();
			int rowCount = printQueue.get(0).rowCount ; 
			StdoutGridWriter[] writers = new StdoutGridWriter[gridCount] ; 
			for (int g=0 ; g<gridCount ; g++) { 
				writers[g] = new StdoutGridWriter(printQueue.get(g), false); 
			}
			for (int i=0 ; i<rowCount ; i++) { 
				for (int g=0 ; g<gridCount ; g++) { 
//					if (g>0) for (int s=0 ; s<gap ; s++) System.out.print(' '); // done::::::: Use makeSpaceString to store this in a field. 
					if (g>0) System.out.print(gapStr);
					writers[g].writeLetterRow(i); 
				}
				System.out.println(); 
			}
			if (wantQuality) { 
				for (int g=0 ; g<gridCount ; g++) { 
					if (g>0) System.out.print(gapStr);
					writers[g].writeQualityRow(); 
//				System.out.print(""+printQueue.get(g).quality+"      "); 
				}
				System.out.println(); 
			}
			for (int i=0 ; i<linesAfter ; i++) System.out.println(); 
		}
		
	}
	
}