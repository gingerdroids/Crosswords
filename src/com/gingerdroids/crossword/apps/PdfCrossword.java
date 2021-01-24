package com.gingerdroids.crossword.apps;

import java.awt.Color;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import javax.swing.filechooser.FileNameExtensionFilter;

import org.blockframe.blocks.FrameHorizontal;
import org.blockframe.blocks.FrameReading;
import org.blockframe.blocks.FrameVertical;
import org.blockframe.blocks.MultiColumnFrame;
import org.blockframe.blocks.SpacerFullWidth;
import org.blockframe.blocks.SpacerHeight;
import org.blockframe.blocks.SpacerWidth;
import org.blockframe.blocks.StringBlock;
import org.blockframe.blocks.StringBlockBold;
import org.blockframe.blocks.TableBlock;
import org.blockframe.blocks.TwoColumnFrame;
import org.blockframe.core.Block;
import org.blockframe.core.Canvas;
import org.blockframe.core.DebugLog;
import org.blockframe.core.Layout;
import org.blockframe.core.PdfDocument;
import org.blockframe.core.Quill;
import org.blockframe.examples.UtilsForExamples;
import org.blockframe.painters.Scribe;

import com.gingerdroids.crossword.apps.GridLines.CellBorderPainter;
import com.gingerdroids.crossword.StandardBuilder;
import com.gingerdroids.crossword.UsualQualityMeasure;
import com.gingerdroids.crossword.Cell;
import com.gingerdroids.crossword.Crossword;
import com.gingerdroids.crossword.CrosswordInput;
import com.gingerdroids.crossword.Grid;
import com.gingerdroids.crossword.PuzzlePhrase;
import com.gingerdroids.crossword.PuzzleWord;
import com.gingerdroids.crossword.QualityMeasure;
import com.gingerdroids.crossword.QualityMeasureFactory;
import com.gingerdroids.crossword.WordBank;
import com.gingerdroids.crossword.Grid.PlacedWord;
import com.gingerdroids.swing.InteractiveFileGetter;
import com.gingerdroids.utils_java.Str;
import com.gingerdroids.utils_java.StringMaker;
import com.gingerdroids.utils_java.Util;

public class PdfCrossword extends PdfDocument { 

	static String cluesFileName ; 
	
	public static void main(String[] args) throws IOException { 
		//////  Read the clues-file and word-banks
		File homeDir = new File(System.getProperty("user.home")); 
		File wordBanksDir = new File(homeDir, "word-banks"); 
		InteractiveFileGetter cluesFileGetter = new InteractiveFileGetter() {
			protected void configure(javax.swing.JFrame frame, javax.swing.JFileChooser chooser) {
				frame.setPreferredSize(new Dimension(800, 800)); 
				chooser.setFileFilter(new FileNameExtensionFilter("Text files", "txt"));
			};
		};
		File cluesFile = cluesFileGetter.chooseFile(); 
		File crosswordDir = cluesFile.getParentFile(); 
		cluesFileName = cluesFile.getName(); 
		CrosswordInput crosswordInput = new CrosswordInput(cluesFile); 
		WordBank wordBank = WordBank.loadWordBank_EOWL(wordBanksDir); // Be aware EOWL has copyright conditions. 
		//////  Build the crossword - place the words in the grid
		Crossword crossword = new StandardBuilder(crosswordInput, wordBank, qualityMeasureFactory).buildCrossword(); 
		//// Feedback to compositor 
		{ 
			HashSet<String> fillWordList = new HashSet<String>(); 
			for (PlacedWord placedWord : crossword.grid.placedWordList) { 
				if (!placedWord.isPuzzleWord) { 
					fillWordList.add(placedWord.word.toLowerCase());
				}
			}
			List<String> pendingWords = crossword.grid.getPendingWords();
			if (pendingWords.isEmpty()) { 
				System.out.println();
				System.out.println("All puzzle words were placed in the crossword.");
			} else { 
				printWordsWithHeading("NOT-PLACED PUZZLE WORDS", pendingWords);
			}
			printWordsWithHeading("COATING WORDS", fillWordList);
			System.out.println();
			System.out.println("_____________");
		}
		//////  Write out the PDFs
		String cluesBasename = Str.getFileBasename(cluesFile); 
		try { 
			new PdfCrossword(crossword, crosswordInput, new File(crosswordDir, cluesBasename+" blank.pdf"), false); 
			new PdfCrossword(crossword, crosswordInput, new File(crosswordDir, cluesBasename+" soln.pdf"), true); 
		} catch (Exception e) { 
			throw e ; 
		} finally { 
			DebugLog.out(); 
		}
	}
	
	public static void printWordsWithHeading(String heading, Collection<String> words) { 
		System.out.println();
		System.out.println(heading);
		String[] lines = Str.toLineArray(words, 120); 
		for (String line : lines) System.out.println(line);
	}
	
	static QualityMeasureFactory qualityMeasureFactory = new QualityMeasureFactory() {

		public QualityMeasure makeQualityMeasure(int totalWordCount, int currentWordCount) {
			/* I've tuned these parameters a little, but not systematically or carefully. */
			double currentWordFraction = currentWordCount / (double) totalWordCount;
			double lengthsFactor = currentWordCount ;
			double pendingFactor = 100 ;
			double crossingsFactor = 8 + currentWordCount / 4.0 ; 
			double fullSpanFactor = currentWordFraction * (1-currentWordFraction); 
			double symSpanFactor = 2 * (1 - currentWordFraction) ; // WAS  div-by-zero BUG totalWordCount / (double) currentWordCount ;
			double surroundedFactor = 0 ;
			boolean isSurroundedBoth = false ;
			double needinessFactor = 1 + 2 * Util.sqr(currentWordFraction) ;
			double virginFactor = 1 ; 
			QualityMeasure qualityMeasure = new UsualQualityMeasure(lengthsFactor, pendingFactor, crossingsFactor, fullSpanFactor, symSpanFactor, surroundedFactor, isSurroundedBoth, needinessFactor, virginFactor, null);
			return qualityMeasure;
		}
	};
	
	final Crossword crossword ; 
	
	final Grid grid ; 
	
	private final boolean wantSolution ; 
	
	private float cluesFontSize ;
	
	private float gridFontSize ; 
	
	PdfCrossword(Crossword crossword, CrosswordInput crosswordInput, File file, boolean wantSolution) throws IOException { 
		DebugLog.shouldAlwaysFlush = true ; 
		setMaxPageCount(3); // Setting this low makes debugging easier. 
		this.crossword = crossword ; 
		this.grid = crossword.grid ; 
		this.wantSolution = wantSolution ; 
		String title = crosswordInput.title ; 
		this.cluesFontSize = 12 ; 
		if (crosswordInput.cluesFontSize!=null) this.cluesFontSize = crosswordInput.cluesFontSize ; 
		this.gridFontSize = 12 ; 
		if (crosswordInput.gridFontSize!=null) this.gridFontSize = crosswordInput.gridFontSize ; 
//		setMargins(null, null, null, 18.0); 
		//////  Build and send frames 
		if (title!=null) { 
			write(new TitleFrame(title)); 
			write(new SpacerHeight(true, 1)); 
		}
		GridHolder gridHolder = new GridHolder(); 
		write(gridHolder); 
		gridHolder.write(new GridFrame()); 
		//write(new GridAndWordsFrame()); 
		write(new SpacerHeight(true, 0.8)); 
		if (crosswordInput.note!=null) { 
			write(new NoteFrame(crosswordInput.note)); 
			write(new SpacerHeight(true, 0.3)); 
		}
		PhrasesFrame phrasesFrame = new PhrasesFrame();
		write(phrasesFrame); 
		//////  Finish off
		writeFile(file); 
		DebugLog.out(); // Catches the writing of blocks, but not the laying out and drawing into PDF. 
	}
	
	@Override
	public Quill newPageQuill(Page prevPage) {
		return super.newPageQuill(prevPage).copySize(cluesFontSize);
	}
	
	class TitleFrame extends FrameHorizontal { 
		TitleFrame(String title) { 
			write(new StringBlock(title)); 
		}
		@Override
		protected Quill inheritQuill(Quill receivedQuill) {
			return super.inheritQuill(receivedQuill).copySize(receivedQuill.getFontSize()+6);
		}
		@Override
		protected Layout inheritLayout(Layout receivedLayout) {
			return super.inheritLayout(receivedLayout).copy(Layout.CENTRE_H);
		}
	}
	
	class NoteFrame extends FrameHorizontal { 
		NoteFrame(String note) { 
			write(new StringBlock(note)); 
		}
		@Override
		protected Quill inheritQuill(Quill receivedQuill) {
			return super.inheritQuill(receivedQuill).copyItalic(); 
		}
	}
	
	class GridHolder extends FrameHorizontal { 
		@Override
		protected Layout inheritLayout(Layout receivedLayout) {
			return super.inheritLayout(receivedLayout.copyCentreH());
		}
	}
	
	class GridFrame extends TableBlock { 
		
		private final float borderMinWidth = 0.05f ;
		
		final GridLines gridLines ; 

		GridFrame() { 
			super(grid.rowCount, grid.columnCount);
			this.gridLines = new GridLines(PdfCrossword.this, crossword); 
			setBorderColor(Color.DARK_GRAY); 
		}
		
		@Override
		protected Quill inheritQuill(Quill receivedQuill) {
			return super.inheritQuill(receivedQuill).copySize(gridFontSize);
		}

		@Override
		protected Block getCellBlock(int row, int column) { 
			return new CellBlock(grid.rows[row][column]); 
		}
		
		@Override
		protected double getCellPadding(int row, int column, boolean isSide, boolean isBefore) {
			if (isBefore && ! isSide) return 0 ; // No padding at top of cell. 
			return quill.getFontSize() / 4 ;
		}
		
		@Override
		protected double getTableBorderThickness(boolean isSide, boolean isBefore) { 
			return quill.getFontSize() / 12 + borderMinWidth ;
		}

		@Override
		protected double getCellBorderThickness(int index, boolean isSide) { 
			return getTableBorderThickness(isSide, true);
		} 
		
		@Override
		protected void drawTableBorderAtRow(Canvas canvas, double tableLeft, double tableTop, int row, boolean isLeft) throws IOException {
			drawCellLeftBorder(canvas, tableLeft, tableTop, row, isLeft?0:columnCount); 
		}
		
		@Override
		protected void drawTableBorderAtColumn(Canvas canvas, double tableLeft, double tableTop, int column, boolean isTop) throws IOException {
			drawCellTopBorder(canvas, tableLeft, tableTop, isTop?0:rowCount, column); 
		}
		
		@Override
		protected void drawCellLeftBorder(Canvas canvas, double tableLeft, double tableTop, int row, int column) throws IOException {
			CellBorderPainter painter = gridLines.getPainter(row, column, true);
			if (painter==GridLines.none) return ; 
			double offsetLeft = column>0 ? getCellBorderCornerX(column-1, false) : 0 ;
			double offsetRight = column<columnCount ? getCellBorderCornerX(column, true) : placedTable.getWidth();
			double offsetTop = getCellBorderCornerY(row, true) ;  // WAS  row>0 ? getCellCornerY(row, true) : 0 ; 
			double offsetBottom = getCellBorderCornerY(row, false) ; // WAS row<rowCount ? getCellCornerY(row, false) : getHeight(); 
			painter.draw(canvas, getBorderColor(), tableLeft+offsetLeft, tableTop+offsetTop, tableLeft+offsetRight, tableTop+offsetBottom); 
		}
		
		@Override
		protected void drawCellTopBorder(Canvas canvas, double tableLeft, double tableTop, int row, int column) throws IOException {
			CellBorderPainter painter = gridLines.getPainter(row, column, false);
			if (painter==GridLines.none) return ; 
			double offsetLeft = getCellBorderCornerX(column, true);
			double offsetRight = getCellBorderCornerX(column, false);
			double offsetTop = row>0 ? getCellBorderCornerY(row-1, false) : 0 ; 
			double offsetBottom = row<rowCount ? getCellBorderCornerY(row, true) : placedTable.getHeight(); 
			painter.draw(canvas, getBorderColor(), tableLeft+offsetLeft, tableTop+offsetTop, tableLeft+offsetRight, tableTop+offsetBottom); 
		}

		
//		@Override
//		protected void drawIntersticeLeftTop(Canvas canvas, double tableLeft, double tableTop, int row, int column) throws IOException {} // Empty interstices look OK. Probably visually help human see size of squares. 
		
		class CellBlock extends Block { 
			
			final Cell cell ; 
			
			double size ;
			
			private CellLetterBlock letterBlock; 
			private CellNumberBlock numberBlock; 
			private PlacedBlock placedLetterBlock ; 
			private PlacedBlock placedNumberBlock ; 
			
			CellBlock(Cell cell) { 
				this.cell = cell ; 
				if ((wantSolution || !cell.isPuzzleCell) && cell.hasLetter()) { 
					letterBlock = new CellLetterBlock();
				}
				if (cell.wordNumber>0) { 
					numberBlock = new CellNumberBlock();
				}
			}
			
			@Override
			public PlacedBlock fill(Quill receivedQuill, Layout receivedLayout) throws IOException {
				this.quill = inheritQuill(receivedQuill) ; 
				Layout layout = inheritLayout(receivedLayout); 
				PlacedBlock placedCellBlock = new PlacedBlock(); 
				this.size = 2 * quill.getFontSize();
				placedCellBlock.setDimensions(size, size); 
				if (letterBlock!=null) { 
					this.placedLetterBlock = letterBlock.fill(quill, layout); 
					placedLetterBlock.setOffsetInContainer(placedCellBlock, ChildPlacer.CENTRE_CENTRE); 
				}
				if (numberBlock!=null) { 
					this.placedNumberBlock = numberBlock.fill(quill, layout); 
					placedNumberBlock.setOffsetInContainer(placedCellBlock, ChildPlacer.TOP_LEFT); 
				}
				return placedCellBlock ; 
			}
			
			@Override
			public void draw(Canvas canvas, double left, double top, double width, double height) throws IOException {
				if (letterBlock!=null) { 
					placedLetterBlock.draw(canvas, left+placedLetterBlock.getLeftInContainer(), top+placedLetterBlock.getTopInContainer()); 
				}
				if (numberBlock!=null) { 
					placedNumberBlock.draw(canvas, left+placedNumberBlock.getLeftInContainer(), top+placedNumberBlock.getTopInContainer()); 
				}
				if (!cell.hasLetter()) { 
					int dummyRow = 0 ;// Dumb, but good enough. 
					int dummyColumn = 0 ;
					double outerLeft = left - getCellPadding(dummyRow, dummyColumn, true, true) - getCellBorderThickness(dummyRow, true); // Includes padding and border
					double outerTop = top - getCellPadding(dummyRow, dummyColumn, false, true) - getCellBorderThickness(dummyRow, true); 
					double outerRight = left + width + getCellPadding(dummyRow, dummyColumn, true, false) + getCellBorderThickness(dummyRow, true);
					double outerBottom = top + height + getCellPadding(dummyRow, dummyColumn, false, false) + getCellBorderThickness(dummyRow, true);
					Scribe.rect(canvas, true, true, getBorderColor(), outerLeft, outerTop, outerRight, outerBottom); 
				}
			}
			
			class CellLetterBlock extends StringBlock {
				public CellLetterBlock() {
					super(String.valueOf(cell.ch));
				} 
				@Override
				protected Quill inheritQuill(Quill receivedQuill) {
					if (cell.isPuzzleCell) { 
						return super.inheritQuill(receivedQuill);
					} else { 
						return super.inheritQuill(receivedQuill).copy(wantSolution?Color.LIGHT_GRAY:Color.DARK_GRAY); 
					}
				}
			}
			
			class CellNumberBlock extends StringBlock { 
				public CellNumberBlock() {
					super(String.valueOf(cell.wordNumber)); 
				} 
				@Override
				protected Quill inheritQuill(Quill receivedQuill) { 
					return super.inheritQuill(receivedQuill).copy(null, Quill.BOLD, 10f); 
				}
			}
		}
	}

	class PhrasesFrame extends MultiColumnFrame { 
		
		PhrasesFrame() { 
			super(crossword.crosswordInput.cluesColumnCount); 
			setGapWidthTemplate("WWWW"); 
			//write(new CluesHeader()); 
			for (PuzzlePhrase puzzlePhrase : crossword.puzzlePhrases) { 
				write(new PuzzlePhraseFrame(puzzlePhrase)); 
			}
		}
		
		class PuzzlePhraseFrame extends FrameReading { 
			
			final PuzzlePhrase puzzlePhrase ; 
			
			PuzzlePhraseFrame(PuzzlePhrase puzzlePhrase) { 
				this.puzzlePhrase = puzzlePhrase ; 
				//////  Phrase number, word numbers & lengths, etc
				final String wordNumbersString = computeWordNumbersString(puzzlePhrase);
				StringMaker<String> wordLengths = new StringMaker<String>(puzzlePhrase.clue.orderedWords, " ") { 
					@Override
					public String getString(String word) {
						return ""+word.length();
					}
				};
				write(new StringBlockBold(wordNumbersString)); 
				write(new WordLengthsBlock("  ("+wordLengths.string+")")); 
				//////  
				if (puzzlePhrase.clue.hint!=null) { 
					write(new SpacerWidth()); 
					HintFrame hintBlock = new HintFrame(); 
					write(hintBlock); 
					for (Block block : splitString(puzzlePhrase.clue.hint)) { 
						hintBlock.write(block); 
					}
				}
			}
			
			@Override
			protected Layout inheritLayout(Layout receivedLayout) {
				return super.inheritLayout(receivedLayout.copyTop());
			}

			private String computeWordNumbersString(PuzzlePhrase puzzlePhrase) {
				StringMaker<String> wordNumbers = new StringMaker<String>(puzzlePhrase.clue.orderedWords, " ") { 
					@Override
					public String getString(String word) {
						PuzzleWord puzzleWord = grid.mapWordToPuzzleWord.get(word); 
						if (puzzleWord==null) return word.toLowerCase(); 
						return puzzleWord.getNumberDirn(); 
					}
				};
				final String wordNumbersString = wordNumbers.string;
				return wordNumbersString;
			}
			
			private class WordLengthsBlock extends StringBlock { 
				public WordLengthsBlock(String text) {
					super(text);
				}

				@Override
				protected Quill inheritQuill(Quill receivedQuill) {
					return receivedQuill.copySize((float) (receivedQuill.getFontSize()*0.85)); 
				}
			}
			
			class HintFrame extends FrameReading {} // Named class helps DebugLog messages. 
		}
	}

	public static Block[] splitString(String wholeString) { 
		String [] words = wholeString.split(" "); 
		final int count = words.length;
		Block [] blocks = new Block[count] ; 
		for (int i=0 ; i<count ; i++) { 
			blocks[i] = new StringBlock(words[i]); 
		}
		return blocks ; 
	}
	
}
