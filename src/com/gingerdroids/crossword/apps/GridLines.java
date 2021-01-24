package com.gingerdroids.crossword.apps;


import java.awt.Color;
import java.io.IOException;

import org.blockframe.core.Canvas;
import org.blockframe.core.DebugLog;
import org.blockframe.core.PdfChapter;
import org.blockframe.core.PdfDocument;
import org.blockframe.painters.BezierCirclePainter;
import org.blockframe.painters.Scribe;

import com.gingerdroids.crossword.Cell;
import com.gingerdroids.crossword.Crossword;
import com.gingerdroids.crossword.Grid;
import com.gingerdroids.crossword.Grid.PlacedWord;


public class GridLines { 

	private final int rowCount;

	private final int columnCount;
	
	/**
	 * Intrusion from each end when drawing topAndBottom, or leftAndRight. 
	 */
	private final double endFrac ; 
	
	private static final double gapFrac = 0.1 ; 
	private static final float bezierFactor = 0.8f;
	
	final Grid grid;
	
	final PdfChapter pdfCrossword ; 
	
	final String[][] acrossWords ; 
	final String[][] downWords ; 

	public GridLines(PdfChapter pdfCrossword, Crossword crossword) { 
		this(pdfCrossword, crossword, 0.06); 
	}
	
	public GridLines(PdfChapter pdfCrossword, Crossword crossword, double endFrac) { 
		this.endFrac = endFrac ; 
		this.grid = crossword.grid ; 
		this.pdfCrossword = pdfCrossword ; 
		this.rowCount = grid.rowCount;
		this.columnCount = grid.columnCount;
		this.acrossWords = new String[rowCount][columnCount] ; 
		this.downWords = new String[rowCount][columnCount] ; 
		for (PlacedWord placedWord : crossword.placedWordList) { 
			if (placedWord.isAcross) { 
				for (int i=0 ; i<placedWord.word.length() ; i++) { 
					acrossWords[placedWord.row][placedWord.column+i] = placedWord.word ; 
				}
			} else { 
				for (int i=0 ; i<placedWord.word.length() ; i++) { 
					downWords[placedWord.row+i][placedWord.column] = placedWord.word ; 
				}
			}
		}
	}
	
	public abstract static class CellBorderPainter { 
		public abstract void draw(Canvas canvas, Color color, double left, double top, double right, double bottom) throws IOException; 
	}
	public static final CellBorderPainter none = new CellBorderPainter() {
		@Override
		public void draw(Canvas canvas, Color color, double left, double top, double right, double bottom) throws IOException {
		}
	};
	public static final CellBorderPainter full = new CellBorderPainter() { 
		@Override
		public void draw(Canvas canvas, Color color, double left, double top, double right, double bottom) throws IOException {
			Scribe.rect(canvas, true, false, color, left, top, right, bottom); 
		}
	};
	static final CellBorderPainter leftEnd = new CellBorderPainter() { 
		@Override
		public void draw(Canvas canvas, Color color, double left, double top, double right, double bottom) throws IOException {
			BezierCirclePainter pathPainter = new BezierCirclePainter(canvas); 
			float gap = (float) (gapFrac * (bottom-top)); 
			pathPainter.traceBezierLeftHalf((float)(left), canvas.getPdfY(bottom)+gap, (float)((right-left)*2), (float)((bottom-top)-2*gap), bezierFactor, bezierFactor); 
			pathPainter.fillPath(); 
		}
	};
	static final CellBorderPainter rightEnd = new CellBorderPainter() { 
		@Override
		public void draw(Canvas canvas, Color color, double left, double top, double right, double bottom) throws IOException {
			BezierCirclePainter pathPainter = new BezierCirclePainter(canvas); 
			float gap = (float) (gapFrac * (bottom-top)); 
			pathPainter.traceBezierRightHalf((float)left, canvas.getPdfY(bottom)+gap, (float)((right-left)*2), (float)((bottom-top)-2*gap), bezierFactor, bezierFactor); 
			pathPainter.fillPath(); 
		}
	};
	static final CellBorderPainter aboveEnd = new CellBorderPainter() { 
		@Override
		public void draw(Canvas canvas, Color color, double left, double top, double right, double bottom) throws IOException {
			BezierCirclePainter pathPainter = new BezierCirclePainter(canvas); 
			double gap = gapFrac * (right-left); 
			pathPainter.traceBezierTopHalf((float)(left+gap), canvas.getPdfY(bottom), (float)((right-left)-2*gap), (float)((bottom-top)*2), bezierFactor, bezierFactor); 
			pathPainter.fillPath(); 
		}
	};
	static final CellBorderPainter belowEnd = new CellBorderPainter() { 
		@Override
		public void draw(Canvas canvas, Color color, double left, double top, double right, double bottom) throws IOException {
			BezierCirclePainter pathPainter = new BezierCirclePainter(canvas); 
			double gap = gapFrac * (right-left); 
			pathPainter.traceBezierBottomHalf((float)(left+gap), canvas.getPdfY(bottom), (float)((right-left)-2*gap), (float)((bottom-top)*2), bezierFactor, bezierFactor); 
			pathPainter.fillPath(); 
		}
	};
	final CellBorderPainter leftAndRight = new CellBorderPainter() { 
		@Override
		public void draw(Canvas canvas, Color color, double left, double top, double right, double bottom) throws IOException {
			double fracWidth = endFrac * (right - left) ; 
			Scribe.rect(canvas, true, false, color, left, top, left+fracWidth, bottom); 
			Scribe.rect(canvas, true, false, color, right-fracWidth, top, right, bottom); 
		}
	};
	static final CellBorderPainter leftOnly_UNCODED = new CellBorderPainter() { 
		@Override
		public void draw(Canvas canvas, Color color, double left, double top, double right, double bottom) throws IOException {
		}
	};
	static final CellBorderPainter rightOnly_UNCODED = new CellBorderPainter() { 
		@Override
		public void draw(Canvas canvas, Color color, double left, double top, double right, double bottom) throws IOException {
		}
	};
	final CellBorderPainter topAndBottom = new CellBorderPainter() { 
		@Override
		public void draw(Canvas canvas, Color color, double left, double top, double right, double bottom) throws IOException {
			double fracHeight = endFrac * (bottom - top) ; 
			Scribe.rect(canvas, true, false, color, left, top, right, top+fracHeight); 
			Scribe.rect(canvas, true, false, color, left, bottom-fracHeight, right, bottom); 
		}
	};
	static final CellBorderPainter topOnly_UNCODED = new CellBorderPainter() { 
		@Override
		public void draw(Canvas canvas, Color color, double left, double top, double right, double bottom) throws IOException {
		}
	};
	static final CellBorderPainter bottomOnly_UNCODED = new CellBorderPainter() { 
		@Override
		public void draw(Canvas canvas, Color color, double left, double top, double right, double bottom) throws IOException {
		}
	};

	/**
	 * Returns the {@link CellBorderPainter} appropriate for the left or top of the given cell. 
	 */
	public CellBorderPainter getPainter(int row, int column, boolean isSide) { 
		if (isSide) { 
			/* The border between columns 'column-1' and 'column', on the given row. */ 
			if (column==0) { 
				/* It's a table border, on the left. */
				Cell rightCell = grid.rows[row][column] ; 
				if (rightCell.hasLetter() /*&& rightCell.isPuzzleCell*/) return full/*leftEnd*/ ; 
				return none ; 
			} else if (column==grid.columnCount) { 
				/* It's a table border, on the right. */
				Cell leftCell = grid.rows[row][column-1] ; 
				if (leftCell.hasLetter() /*&& leftCell.isPuzzleCell*/) return full/*rightEnd*/ ; 
				return none ; 
			} else { 
				/* It's an internal border. */
				Cell leftCell = grid.rows[row][column-1] ; 
				Cell rightCell = grid.rows[row][column] ; 
				if (leftCell.hasLetter()) { 
					if (rightCell.hasLetter()) { 
						/* Both cells have a letter */
						String leftAcrossWord = acrossWords[row][column-1] ; 
						String rightAcrossWord = acrossWords[row][column] ; 
						if (leftAcrossWord==null || rightAcrossWord==null) return full ; 
						if (leftAcrossWord==rightAcrossWord) { 
							return leftCell.isPuzzleCell ? topAndBottom : none ; // Part of same word. 
						} else { 
							return full ; 
						}
					} else { 
						/* Only the left cell has a letter. */
						return leftCell.isPuzzleCell ? full/*rightEnd*/ : none ; 
					}
				} else { 
					if (rightCell.hasLetter()) { 
						/* Only the right cell has a letter. */
						return rightCell.isPuzzleCell ? full/*leftEnd*/ : none ; 
					} else { 
						/* Neither cell has a letter. */
						return none ; 
					}
				}
			}
		} else { 
			/* The border between rows 'row-1' and 'row', in the given column. */ 
			if (row==0) { 
				/* It's a table border, at the top. */
				Cell lowerCell = grid.rows[row][column] ; 
				if (lowerCell.hasLetter() /*&& lowerCell.isPuzzleCell*/) return full/*aboveEnd*/ ; 
				return none ; 
			} else if (row==grid.rowCount) { 
				/* It's a table border, at the bottom. */
				Cell upperCell = grid.rows[row-1][column] ; 
				if (upperCell.hasLetter() /*&& upperCell.isPuzzleCell*/) return full/*belowEnd*/ ; 
				return none ; 
			} else { 
				/* It's an internal border. */
				Cell upperCell = grid.rows[row-1][column] ; 
				Cell lowerCell = grid.rows[row][column] ; 
				if (upperCell.hasLetter()) { 
					if (lowerCell.hasLetter()) { 
						/* Both cells have a letter */
						String upperDownWord = downWords[row-1][column] ; 
						String lowerDownWord = downWords[row][column] ; 
						if (upperDownWord==null || lowerDownWord==null) return full ; 
						if (upperDownWord==lowerDownWord) { 
							return upperCell.isPuzzleCell ? leftAndRight : none ; // Part of same word. 
						} else { 
							return full ; 
						}
					} else { 
						/* Only the upper cell has a letter. */
						return upperCell.isPuzzleCell ? full/*belowEnd*/ : none ; 
					}
				} else { 
					if (lowerCell.hasLetter()) { 
						/* Only the lower cell has a letter. */
						return lowerCell.isPuzzleCell ? full/*aboveEnd*/ : none ; 
					} else { 
						/* Neither cell has a letter. */
						return none ; 
					}
				}
			}
		}
	}

}
