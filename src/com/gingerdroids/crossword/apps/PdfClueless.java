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

//import org.apache.batik.svggen.SVGGeneratorContext.GraphicContextDefaults;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.blockframe.blocks.FrameHorizontal;
import org.blockframe.blocks.FrameLeftRight;
import org.blockframe.blocks.FrameReading;
import org.blockframe.blocks.FrameVertical;
import org.blockframe.blocks.SpacerFullHeight;
import org.blockframe.blocks.SpacerFullWidth;
import org.blockframe.blocks.SpacerHeight;
import org.blockframe.blocks.SpacerWidth;
import org.blockframe.blocks.StringBlock;
import org.blockframe.blocks.StringBlockBold;
import org.blockframe.blocks.StringBlockItalic;
import org.blockframe.blocks.TableBlock;
import org.blockframe.core.Block;
import org.blockframe.core.Canvas;
import org.blockframe.core.DebugLog;
import org.blockframe.core.DebugLog.Verbosity;
import org.blockframe.core.PdfChapter.Page;
import org.blockframe.core.Frame;
import org.blockframe.core.Layout;
import org.blockframe.core.PdfDocument;
import org.blockframe.core.Quill;
import org.blockframe.examples.UtilsForExamples;
import org.blockframe.painters.Scribe;

import com.gingerdroids.crossword.apps.GridLines.CellBorderPainter;
import com.gingerdroids.crossword.apps.PdfCrossword.TitleFrame;
import com.gingerdroids.crossword.StandardBuilder;
import com.gingerdroids.crossword.UsualQualityMeasure;
import com.gingerdroids.crossword.Cell;
import com.gingerdroids.crossword.CluelessBuilder;
import com.gingerdroids.crossword.Crossword;
import com.gingerdroids.crossword.CrosswordInput;
import com.gingerdroids.crossword.Grid;
import com.gingerdroids.crossword.Grid.PlacedWord;
import com.gingerdroids.crossword.PuzzlePhrase;
import com.gingerdroids.crossword.PuzzleWord;
import com.gingerdroids.crossword.QualityMeasure;
import com.gingerdroids.crossword.QualityMeasureFactory;
import com.gingerdroids.crossword.WordBank;
import com.gingerdroids.swing.InteractiveFileGetter;
import com.gingerdroids.utils_java.Profiler;
import com.gingerdroids.utils_java.Str;
import com.gingerdroids.utils_java.StringMaker;
import com.gingerdroids.utils_java.Util;

public class PdfClueless extends PdfDocument { 

private static final String answersTitle = "Answers";

private static final String answerWordsEasyTitle = "Words in answers (longest words first)";

private static final String answerWordsHardTitle = "Words in answers";

	private static final String phraseLengthsTitle = "Answer phrases and (number of letters in each word)";

	private static final String wordOccurrencesTitle = "For each word, all the phrases it appears in";
	
	private static final String tipsAndStrategiesTitle = "Tips, tricks and the lists of phrases";

	static String cluesFileName = null ; 
	
	public static void main(String[] args) throws IOException { 
		//////  Read the clues-file and word-banks
		File homeDir = new File(System.getProperty("user.home")); 
		File wordBanksDir = new File(homeDir, "word-banks"); 
		InteractiveFileGetter fileGetter = new InteractiveFileGetter() {
			protected void configure(javax.swing.JFrame frame, javax.swing.JFileChooser chooser) {
				frame.setPreferredSize(new Dimension(800, 800)); 
				chooser.setFileFilter(new FileNameExtensionFilter("Text files", "txt"));
			};
		};
		File cluesFile = fileGetter.chooseFile(); 
		File crosswordDir = cluesFile.getParentFile(); 
		cluesFileName = cluesFile.getName(); 
		WordBank wordBank = WordBank.loadWordBank_EOWL(wordBanksDir); // Be aware EOWL has copyright conditions. 
		CrosswordInput crosswordInput = new CrosswordInput(cluesFile); 
		//////  Build the crosswords - place the words in grids
		Crossword [] allCrosswords = new CluelessBuilder(crosswordInput, wordBank, qualityMeasureFactory).buildCrosswords(); 
		//////  Select best few crosswords 
		final int printCount = 8 ; 
		Crossword [] bestCrosswords = new Crossword[printCount] ; 
		int outIndex = 0 ; 
		for (Crossword crossword : allCrosswords) { 
			if (crossword.grid.getPendingWords().isEmpty()) { 
				bestCrosswords[outIndex] = crossword ; 
				outIndex ++ ; 
				if (outIndex>=printCount) break ; 
			}
		}
		
		{}
		//Build short array of crosswords to report fill-words from ; They should all be completely filled ; 
		
		//////  Report fill words & unplaced puzzle words. 
		if (outIndex<printCount) { 
			System.out.println("Only found "+outIndex+" crosswords with all words placed."); 
		}
		
		HashSet<String> fillWordList = new HashSet<String>(); 
		for (Crossword crossword : bestCrosswords) { 
			if (crossword==null) continue ; 
			for (PlacedWord placedWord : crossword.grid.placedWordList) { 
				if (!placedWord.isPuzzleWord) { 
					fillWordList.add(placedWord.word.toLowerCase());
				}
			}
		}
		printWordsWithHeading("COATING WORDS", fillWordList);
		/*
		List<String> pendingWords = crossword.grid.getPendingWords();
		if (pendingWords.isEmpty()) { 
			System.out.println("All puzzle words placed.");
		} else { 
			printWordsWithHeading("NOT-PLACED PUZZLE WORDS", pendingWords);
		}
		 */
		System.out.println();
		System.out.println("_____________");
		//////  Write PDF files 
		String cluesBasename = Str.getFileBasename(cluesFile); 
		for (int i=0 ; i<outIndex ; i++) { 
			Crossword crossword = bestCrosswords[i] ; 
			if (crossword==null) continue ; 
			new PdfClueless(crossword, crosswordInput, new File(crosswordDir, cluesBasename+" "+i+" soln.pdf"), TYPE_SOLUTION); 
			new PdfClueless(crossword, crosswordInput, new File(crosswordDir, cluesBasename+" "+i+" easy.pdf"), TYPE_EASY); 
			new PdfClueless(crossword, crosswordInput, new File(crosswordDir, cluesBasename+" "+i+" hard.pdf"), TYPE_HARD); 
		}
		//////  Bye bye 
		DebugLog.out(); 
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
			double pendingFactor = 10 ; 
			double crossingsFactor = 4.0 * currentWordFraction ; 
			double fullSpanFactor = 0 ; 
			double symSpanFactor = 8 * (2 - currentWordFraction) ; // WAS  div-by-zero BUG totalWordCount / (double) currentWordCount ;
			double surroundedFactor = 0 ;
			boolean isSurroundedBoth = false ;
			double needinessFactor = 8 * Util.sqr(Util.sqr(currentWordFraction)) ;
			double virginFactor = 1 ; 
			double [] edgePenalties = new double [] {40, 20-currentWordCount, 10-currentWordCount} ; 
			QualityMeasure qualityMeasure = new UsualQualityMeasure(lengthsFactor, pendingFactor, crossingsFactor, fullSpanFactor, symSpanFactor, surroundedFactor, isSurroundedBoth, needinessFactor, virginFactor, edgePenalties);
			return qualityMeasure;
		}
	};
	
	final Crossword crossword ; 
	
	final Grid grid ; 
	
	private float cluesFontSize ;
	
	private float gridFontSize ; 
	
//	private static final float BASE_FONT_SIZE = wantLargeFormat ? 14 : 12 ; 
//	
//	private static final float GRID_FONT_SIZE = wantLargeFormat ? 12 : 10 ; 
//	
//	private static final float headerFontSize = BASE_FONT_SIZE+2; 
	
	private static final int TYPE_SOLUTION = 0 ; 
	private static final int TYPE_EASY = 1 ; 
	private static final int TYPE_HARD = 2 ; 
	private final int gridType ; 
	
	PdfClueless(Crossword crossword, CrosswordInput crosswordInput, File file, int gridType) throws IOException { 
		DebugLog.shouldAlwaysFlush = true ; 
		setPdRectangle(PDRectangle.A4); 
		setMargins(null, null, null, 12.0);
		this.crossword = crossword ; 
		this.grid = crossword.grid ; 
		this.gridType = gridType ; 
		String title = crosswordInput.title ; 
		this.cluesFontSize = 12 ; 
		if (crosswordInput.cluesFontSize!=null) this.cluesFontSize = crosswordInput.cluesFontSize ; 
		this.gridFontSize = 12 ; 
		if (crosswordInput.gridFontSize!=null) this.gridFontSize = crosswordInput.gridFontSize ; 
		//////  Build and send frames 
		{ 
			/*****  FIRST PAGE */
			if (title!=null) { 
				write(new TitleFrame(title)); 
				write(new SpacerHeight(true, 1)); 
			}
			//// Write grid
			GridHolder gridHolder = new GridHolder(); 
			write(gridHolder); 
			GridFrame gridFrame = new GridFrame(gridType);
			gridHolder.write(gridFrame); 
			if (crosswordInput.note!=null) { 
				write(new NoteFrame(crosswordInput.note)); 
			}
			//// Write clues
			InfoFrame infoFrame = new InfoFrame(); 
			write(infoFrame); 
			
			switch (gridType) {
			case TYPE_SOLUTION:
				infoFrame.write(new SpacerHeight(true, 0.3)); 
				infoFrame.write(new StringBlockBold(answersTitle)); 
				SolutionPhrasesFrame solutionPhrasesFrame = new SolutionPhrasesFrame(gridType);
				infoFrame.write(solutionPhrasesFrame);
				break;
			case TYPE_EASY:
				AnswerWordsEasyFrame answerWordsEasyFrame = new AnswerWordsEasyFrame(); 
				infoFrame.write(new SpacerHeight(true, 0.3)); 
				infoFrame.write(new StringBlockBold(answerWordsEasyTitle)); 
				infoFrame.write(answerWordsEasyFrame); 
			case TYPE_HARD:
				PhrasesLengthsFrame phrasesLengthsFrame = new PhrasesLengthsFrame(gridType); 
				infoFrame.write(new SpacerHeight(true, 0.3)); 
				infoFrame.write(new StringBlockBold(phraseLengthsTitle)); 
				infoFrame.write(phrasesLengthsFrame); 
				WordOccurrencesFrame wordOccurrencesFrame = new WordOccurrencesFrame(); 
				if (!crosswordInput.suppressOccurrences) { 
					infoFrame.write(new SpacerHeight(true, 0.3)); 
					infoFrame.write(new StringBlockBold(wordOccurrencesTitle)); 
					infoFrame.write(wordOccurrencesFrame); 
				}
				if (Util.ffalse) { // These appear in "Answer phrases" anyway. 
					SingleWordPhrasesFrame singleWordPhrasesFrame = new SingleWordPhrasesFrame(); 
					if (singleWordPhrasesFrame.hasSomeContent()) { 
						infoFrame.write(new SpacerHeight(true, 0.3)); 
						infoFrame.write(new StringBlockBold(singleWordPhrasesFrame.getTitle())); 
						infoFrame.write(singleWordPhrasesFrame); 
					}
				}
				break;
			default: throw new RuntimeException("Unexpected gridType "+gridType); 
			}
		}
		//// NEW PAGE
		write(new SpacerFullHeight()); 
		{ 
			/*****  SECOND PAGE */
			//// Tips and strategies 
			if (gridType!=TYPE_SOLUTION) { 
				write(new StringBlockBold(tipsAndStrategiesTitle)); 
				TipsFrame tipsFrame = new TipsFrame(); 
				write(tipsFrame); 
				if (crosswordInput.isDemo) { 
					write(new SpacerHeight(true, 1.4)); 
					write(new StringBlockBold(answersTitle)); 
					SolutionPhrasesFrame solutionPhrasesFrame = new SolutionPhrasesFrame(gridType);
					write(solutionPhrasesFrame);
				}
			}
			if (gridType==TYPE_HARD) { 
				AnswerWordsHardFrame answerWordsHardFrame = new AnswerWordsHardFrame(); 
				write(new SpacerHeight(true, 0.3)); 
				write(new StringBlockBold(answerWordsHardTitle)); 
				write(answerWordsHardFrame); 
				
			}
		}
		//////  Finish off
		writeFile(file); 
		DebugLog.out(); 
	}
	
	@Override
	public Quill newPageQuill(Page prevPage) {
		return super.newPageQuill(prevPage).copySize(cluesFontSize);
	}

	private class InfoFrame extends FrameVertical { 
		
		@Override
		protected Layout inheritLayout(Layout receivedLayout) {
			return super.inheritLayout(receivedLayout).copy(Layout.LEFT);
		}
	}
	
	private class TitleFrame extends FrameHorizontal { 
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
	
	private class NoteFrame extends FrameReading { 
		NoteFrame(String note) { 
			writeTextSplittable(note);
		}
		@Override
		protected Layout inheritLayout(Layout receivedLayout) {
			return super.inheritLayout(receivedLayout.copyCentreH());
		}
	}
	
	private class GridHolder extends FrameHorizontal { 
		@Override
		protected Layout inheritLayout(Layout receivedLayout) {
			return super.inheritLayout(receivedLayout.copyCentreH());
		}
	}
	
	class GridFrame extends TableBlock { 
		
		private final int gridType ; 
		
		private final float borderMinWidth = 0.05f ;
		
		final GridLines gridLines ; 

		GridFrame(int gridType) { 
			super(grid.rowCount, grid.columnCount);
			this.gridType = gridType ; 
			this.gridLines = new GridLines(PdfClueless.this, crossword); 
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
			return quill.getFontSize() / 8 ;
		}
		
		@Override
		protected double getTableBorderThickness(boolean isSide, boolean isBefore) { 
			return quill.getFontSize() / 18 + borderMinWidth ;
		}

		@Override
		protected double getCellBorderThickness(int index, boolean isSide) { 
			return getTableBorderThickness(isSide, true);
		} 
		
		@Override
		public void drawBorders(Canvas canvas, double tableLeft, double tableTop) throws IOException {
//			if (gridType==TYPE_HARD) return ; 
			super.drawBorders(canvas, tableLeft, tableTop);
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
				if ((gridType==TYPE_SOLUTION || !cell.isPuzzleCell) && cell.hasLetter()) { 
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
						return super.inheritQuill(receivedQuill).copy(gridType==TYPE_SOLUTION?Color.LIGHT_GRAY:Color.DARK_GRAY); 
					}
				}
			}
			
			class CellNumberBlock extends StringBlock { 
				public CellNumberBlock() {
					super(String.valueOf(cell.wordNumber)); 
				} 
				@Override
				protected Quill inheritQuill(Quill receivedQuill) { 
					return super.inheritQuill(receivedQuill).copy(null, Quill.BOLD, 9f); 
				}
			}
		}
	}
	
	/**
	 * Only word of its length. 
	 * Set by {@link AnswerWordsEasyFrame} or {@link AnswerWordsHardFrame}. 
	 */
	private PuzzleWord longPuzzleWord = null ; 
	
	private class AnswerWordsEasyFrame extends FrameReading { 
		
		public AnswerWordsEasyFrame() {
//			setOtherLineIndent(24); 
			PuzzleWord[] alphabeticWords = new PuzzleWord.SortLexigraphically(crossword.puzzleWords).sortedWords ; 
			PuzzleWord[][] lengthGroups = PuzzleWord.groupIntoLengths(alphabeticWords); // Includes a zero length! 
			int maxLength = lengthGroups.length - 1 ; 
			for (int length=maxLength ; length>0 ; length--) { 
				PuzzleWord[] group = lengthGroups[length] ; 
				if (group==null) continue ; 
				StringBuffer sb = new StringBuffer(); 
				sb.append("("+group[0].word.length()+")"); 
				for (PuzzleWord puzzleWord : group) sb.append("  "+puzzleWord.word.toLowerCase()); 
				sb.append(".   "); 
				write(new StringBlock(sb.toString())); 
				//// Perhaps set field longPuzzleWord
				if (group.length==1 && longPuzzleWord==null) longPuzzleWord = group[0] ; 
			}
		}
	}
	
	private class AnswerWordsHardFrame extends FrameReading { 
		
		public AnswerWordsHardFrame() {
//			setOtherLineIndent(24); 
			PuzzleWord[] alphabeticWords = new PuzzleWord.SortLexigraphically(crossword.puzzleWords).sortedWords ; 
			for (PuzzleWord puzzleWord : alphabeticWords) write(new StringBlock(puzzleWord.word.toLowerCase())); 
////			PuzzleWord[][] lengthGroups = PuzzleWord.groupIntoLengths(alphabeticWords); // Includes a zero length! 
//			int maxLength = lengthGroups.length - 1 ; 
//			for (int length=maxLength ; length>0 ; length--) { 
//				PuzzleWord[] group = lengthGroups[length] ; 
//				if (group==null) continue ; 
//				StringBuffer sb = new StringBuffer(); 
//				sb.append("("+group[0].word.length()+")"); 
//				for (PuzzleWord puzzleWord : group) sb.append("  "+puzzleWord.word.toLowerCase()); 
//				sb.append(".   "); 
//				write(new StringBlock(sb.toString())); 
//				//// Perhaps set field longPuzzleWord
//				if (group.length==1 && longPuzzleWord==null) longPuzzleWord = group[0] ; 
//			}
		}
	}
	
	/**
	 * A puzzle word which only occurs in one phrase. 
	 */
	PuzzleWord singleOccurenceWord = null ; 
	
	/**
	 * The phrase that {@link #singleOccurenceWord} appears in. 
	 */
	PuzzlePhrase singleOccurrencePhrase = null ; 

	private class WordOccurrencesFrame extends FrameReading { 
		
		private boolean hasSomeContent = false ; // Can be set true in constructor. 
		
		public WordOccurrencesFrame() { 
//			setOtherLineIndent(24); 
			PuzzlePhrase[] sortedPhrases = new PuzzlePhrase.SortByFirstLocation(crossword.puzzlePhrases).sortedPhrases ; 
			PuzzleWord[] sortedWords = new PuzzleWord.SortByLocation(crossword.puzzleWords).sortedWords ; 
//			write(new StringBlockItalic("Word occurrences: ")); 
			for (PuzzleWord puzzleWord : sortedWords) { 
				StringBuffer sb = new StringBuffer(); 
				sb.append(puzzleWord.getNumberDirn()+" is in "); 
				boolean wantComma = false ; 
				int occurrenceCount = 0 ; 
				PuzzlePhrase occurrencePhrase = null ; 
				for (PuzzlePhrase puzzlePhrase : sortedPhrases) { 
					if (puzzlePhrase.orderedWords.length<=1) continue ; 
					for (PuzzleWord phraseWord : puzzlePhrase.orderedWords) { 
						if (phraseWord==puzzleWord) { 
							if (wantComma) sb.append(" and "); 
							sb.append(puzzlePhrase.getLocDirns()); 
							wantComma = true ; 
							occurrencePhrase = puzzlePhrase ; 
							occurrenceCount ++ ; 
						}
					}
				}
				sb.append("."); 
				if (wantComma) { 
					hasSomeContent = true ; 
					write(new StringBlock(sb.toString())); 
					write(new SpacerWidth("    ")); 
				}
				//// Set single-occurrence fields. 
				if (singleOccurenceWord==null && occurrenceCount==1) { 
					singleOccurenceWord = puzzleWord ; 
					singleOccurrencePhrase = occurrencePhrase ; 
				}
			}
		}
		
		boolean hasSomeContent() { 
			return hasSomeContent ; 
		}
	}
	
	private class SolutionPhrasesFrame extends FrameReading { 
		
		private final int gridType ; 
		
		SolutionPhrasesFrame(int gridType) { 
			this.gridType = gridType ; 
//			setOtherLineIndent(24); 
//			write(new StringBlockItalic("Phrases with word lengths: ")); 
			PuzzlePhrase[] sortedPhrases = new PuzzlePhrase.SortByFirstLocation(crossword.puzzlePhrases).sortedPhrases ; 
			boolean wantComma = false ; 
			for (PuzzlePhrase puzzlePhrase : sortedPhrases) { 
				if (wantComma) write(new SpacerWidth("    ")); 
				write(new SolutionPhraseFrame(puzzlePhrase)); 
				wantComma = true ; 
			}
		}

		private class SolutionPhraseFrame extends FrameHorizontal { 
			
			SolutionPhraseFrame(PuzzlePhrase puzzlePhrase) { 
				//////  Phrase number, word numbers & lengths, etc ; or the solution phrase
				final String wordNumbersString = computeWordNumbersString(puzzlePhrase);
				write(new StringBlock(wordNumbersString+"  "+puzzlePhrase.clue.rawAnswer.trim()+".")); 
			}
			
			@Override
			protected Layout inheritLayout(Layout receivedLayout) {
				return super.inheritLayout(receivedLayout.copyTight(true, null));
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
		}
	}
	
	private class PhrasesLengthsFrame extends FrameReading { 
		
		private final int gridType ; 
		
		PhrasesLengthsFrame(int gridType) { 
			this.gridType = gridType ; 
//			setOtherLineIndent(24); 
//			write(new StringBlockItalic("Phrases with word lengths: ")); 
			PuzzlePhrase[] sortedPhrases = new PuzzlePhrase.SortByFirstLocation(crossword.puzzlePhrases).sortedPhrases ; 
			boolean wantComma = false ; 
			for (PuzzlePhrase puzzlePhrase : sortedPhrases) { 
				if (wantComma) write(new SpacerWidth("    ")); 
				write(new PuzzlePhraseFrame(puzzlePhrase)); 
				wantComma = true ; 
			}
		}

		private class PuzzlePhraseFrame extends FrameHorizontal { 
			
			PuzzlePhraseFrame(PuzzlePhrase puzzlePhrase) { 
				//////  Phrase number, word numbers & lengths, etc ; or the solution phrase
				final String wordNumbersString = computeWordNumbersString(puzzlePhrase);
				StringMaker<String> wordLengths = new StringMaker<String>(puzzlePhrase.clue.orderedWords, " ") { 
					@Override
					public String getString(String word) {
						return ""+word.length();
					}
				};
				write(new StringBlock(wordNumbersString+" ("+wordLengths.string+").")); 
			}
			
			@Override
			protected Layout inheritLayout(Layout receivedLayout) {
				return super.inheritLayout(receivedLayout.copyTight(true, null));
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
		}
	}

	private class SingleWordPhrasesFrame extends FrameReading { 
		
		public SingleWordPhrasesFrame() {
//			setOtherLineIndent(24); 
//			write(new StringBlockItalic("Single word answers: ")); 
			for (PuzzlePhrase puzzlePhrase : crossword.puzzlePhrases) { 
				if (puzzlePhrase.orderedWords.length>1) continue ; 
				String numberDirn = puzzlePhrase.orderedWords[0].getNumberDirn(); 
				write(new StringBlock(numberDirn+"    ")); 
			}
		}

		public String getTitle() {
			return "Single word answers" ;
		}
		
		boolean hasSomeContent() { 
			for (PuzzlePhrase puzzlePhrase : crossword.puzzlePhrases) { 
				if (puzzlePhrase.orderedWords.length==1) return true ; 
			}
			return false ; 
		}
	}
	
	private class TipsFrame extends FrameVertical { 
		TipsFrame() { 
			//////  Answers can be phrases. 
			{ 
				PuzzlePhrase examplePhrase = null ; 
				for (PuzzlePhrase puzzlePhrase : crossword.puzzlePhrases) { 
					if (puzzlePhrase.orderedWords.length==1) continue ; 
					if (examplePhrase==null || puzzlePhrase.orderedWords.length<examplePhrase.orderedWords.length) { 
						examplePhrase = puzzlePhrase ; 
					}
				}
				if (examplePhrase!=null) { 
					PuzzleWord[] examplePuzzleWords = examplePhrase.orderedWords;
					{ 
						String example = new StringMaker<PuzzleWord>(examplePuzzleWords, " ") {
							@Override
							public String getString(PuzzleWord puzzleWord) {
								return puzzleWord.word.toLowerCase();
							}
						}.string; 
						writeTip("The answers can be phrases, such as '"+example+"'. "); 
					}
					{ 
						String example = new StringMaker<PuzzleWord>(examplePuzzleWords, ", ") {
							@Override
							public String getString(PuzzleWord puzzleWord) {
								return puzzleWord.getNumberDirn() + " " + puzzleWord.word.toLowerCase();
							}
						}.string; 
						writeTip("Each word in a phrase has a separate place in the puzzle. For example: "+example+". "); 
					}
				}
			}
			//////  Filler words 
			{
				PlacedWord fillerWord = null ; 
				PlacedWord crossingPlacedWord = null ; // Crosses the filler word. 
				{ 
					for (PlacedWord placedWord : grid.placedWordList) { 
						if (placedWord.isPuzzleWord) continue ; 
						String word = placedWord.word; 
						if (fillerWord==null || word.length()>fillerWord.word.length()) { 
							fillerWord = placedWord ;  
							if (fillerWord.isAcross) { 
								for (int i=0 ; i<fillerWord.word.length() ; i++) { 
									if (grid.rows[fillerWord.row][fillerWord.column+i].isPuzzleCell) { 
										crossingPlacedWord = grid.getWordAt(fillerWord.row, fillerWord.column+i, !fillerWord.isAcross); 
									}
								}
							} else { 
								for (int i=0 ; i<fillerWord.word.length() ; i++) { 
									if (grid.rows[fillerWord.row+i][fillerWord.column].isPuzzleCell) { 
										crossingPlacedWord = grid.getWordAt(fillerWord.row+i, fillerWord.column, !fillerWord.isAcross); 
									}
								}
								if (crossingPlacedWord==null) { 
									System.out.println(""+fillerWord.row+","+fillerWord.column+"\t"+fillerWord.word);
								}
							}
						}
					}
				}
				if (fillerWord!=null) { 
					PuzzleWord crossingPuzzleWord = grid.mapWordToPuzzleWord.get(crossingPlacedWord.word); 
					String numberDirn = crossingPuzzleWord.getNumberDirn(); 
					writeTip(
							"The answer words are completely blank in the puzzle. " + 
									"The other words are chosen at random, but they can give you a hint on the blank words. " + 
									"For example, "+numberDirn+" is crossed by the word '"+fillerWord.word.toLowerCase()+"'. "
									
//						"The letters already given in the puzzle aren't part of the answer words. "+
//						"But if you can fill in the missing letters, that can help with the answer words. "+
//						"For example, can you find the word '"+fillerWord.toLowerCase()+"'. "
							); 
				}
			}
			//////  Word lengths
			if (gridType==TYPE_EASY && longPuzzleWord!=null) { 
				String longWord = longPuzzleWord.word.toLowerCase();
				TipFrame tipFrame = new TipFrame(); 
				tipFrame.writePlain("From the "); 
				tipFrame.writeBold(answerWordsEasyTitle); 
				tipFrame.writePlain(" list, there is only one answer with "+longWord.length()+" letters. "); 
				tipFrame.writePlain("And, "+longPuzzleWord.getNumberDirn()+" is the only gap of that length. So "+longPuzzleWord.getNumberDirn()+" must be '"+longWord+"'."); 
				
				writeTip(tipFrame); 
			}
			//////  Words that appear once
			if (Util.ffalse && singleOccurenceWord!=null) { 
				Util.cruft("TO DO:::::: More intelligent detection of whether the 'all the phrases' clue is given."); 
				String wordLengths = new StringMaker<String>(singleOccurrencePhrase.clue.orderedWords, " & ") { 
					@Override
					public String getString(String word) {
						return ""+word.length();
					}
				}.string ;
				String phrase = new StringMaker<PuzzleWord>(singleOccurrencePhrase.orderedWords, " ") {
					@Override
					public String getString(PuzzleWord puzzleWord) {
						return puzzleWord.word.toLowerCase();
					}
				}.string; 
				TipFrame tipFrame = new TipFrame(); 
				tipFrame.writePlain("From the ");
				tipFrame.writeBold(wordOccurrencesTitle); 
				tipFrame.writePlain(" list, the word "+singleOccurenceWord.getNumberDirn()+" only occurs in one phrase: "+singleOccurrencePhrase.getLocDirns()+". "); 
				tipFrame.writePlain("From the ");
				tipFrame.writeBold(phraseLengthsTitle); 
				tipFrame.writePlain(" list, the words have "+wordLengths+" letters. "); 
				tipFrame.writePlain("That might help you guess '"+phrase+"'. "); 
				writeTip(tipFrame); 
			}
		}

		private void writeTip(String text) {
			TipFrame tipFrame = new TipFrame(); 
			writeTip(tipFrame); 
			tipFrame.writeTextSplittable(text);
		}

		public void writeTip(TipFrame tipFrame) {
			write(new SpacerHeight(true, 0.8)); 
			write(tipFrame);
		}
	}
	
	private class TipFrame extends FrameReading { 
		
		void writePlain(String text) { 
			String [] words = text.split(" "); 
			for (String word : words) write(new StringBlock(word)); 
		}
		
		void writeBold(String text) { 
			String [] words = text.split(" "); 
			for (String word : words) write(new StringBlockBold(word)); 
		}
		
		TipFrame() { 
			//setOtherLineIndent(36f); 
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
