package org.blockframe.blocks;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.blockframe.core.Block;
import org.blockframe.core.BlockPipe;
import org.blockframe.core.Canvas;
import org.blockframe.core.DebugLog;
import org.blockframe.core.Layout;
import org.blockframe.core.PdfChapter.Page;
import org.blockframe.core.Quill;

import com.gingerdroids.utils_java.Str;
import com.gingerdroids.utils_java.Util;

/**
 * {@linkplain LedgerFrame} is similar to {@link TableBlock}, except it can break over pages. 
 * Also, it holds text data only. 
 * (Later, it might be tweaked to allow other data. But within a text-oriented design.)
 */
public class LedgerFrame extends FrameVertical { 
	
	protected static final double narrowColumnFactor = 0.5 ; 

	private double rowPaddingFactor = 0.5 ; 
	
	private double columnPaddingFactor = 4.0 ; 
	
	private double rowPadding ; 
	
	private double columnPadding ; 

	/**
	 * Whether all the columns have been added. 
	 */
	private boolean haveAllColumns = false ; 
	
	/**
	 * Whether all the ledger rows have been added. 
	 */
	private boolean haveAllRows = false ; 
	
	private ArrayList<LedgerColumnConfig> columnList = new ArrayList<LedgerColumnConfig>(); 
	
	private LedgerColumnConfig [] columns ; 
	
	private int columnCount = Integer.MIN_VALUE ; 
	
	private boolean hasCalculatedWidths = false ; 

	private double frameWidth ; 

	private double [] columnWidths ; 
	
	private double [] columnLefts ; 
	
	private double [] columnRights ; 
	
	private boolean isTitleRowPending = true ; 
	
	private ArrayList<LedgerRow> rowList = new ArrayList<LedgerFrame.LedgerRow>();
	
	public LedgerFrame() {
	}
	
	/**
	 * Adds a column's configuration. 
	 */
	public LedgerFrame add(LedgerColumnConfig column) { 
		if (haveAllColumns) throw new RuntimeException("No further columns may be added."); 
		columnList.add(column); 
		return this ; 
	}

	/**
	 * Convenience method: creates a {@link LedgerColumnConfig}, and calls {@link #add(LedgerColumnConfig)}. 
	 */
	public LedgerFrame addColumn(String columnTitle, boolean isMultiline, Layout.Justification justification, boolean isRubberWidth) { 
		add(new LedgerColumnConfig(columnTitle, isMultiline, justification, isRubberWidth)); 
		return this ; 
	}
	
	public LedgerFrame setColumnAlignment(int columnIndex, Layout.Alignment alignment) { 
		if (haveAllColumns) throw new RuntimeException("Column configuration has already completed."); 
		columnList.get(columnIndex).setAlignment(alignment); 
		return this ; 
	}
	
	public LedgerFrame freezeColumns() { 
		if (!haveAllColumns) { 
			this.haveAllColumns = true ; 
			this.columns = LedgerColumnConfig.toArray(columnList); 
			this.columnCount = columns.length ; 
			this.columnQuills = new Quill[columnCount] ; 
		}
		return this ; 
	}
	
	/**
	 * Sets the inter-row gap as a multiple of the font height. 
	 */
	public LedgerFrame setRowPadding(double rowPaddingFactor) { 
		this.rowPaddingFactor = rowPaddingFactor ; 
		return this ; 
	}
	
	/**
	 * Sets the inter-column gap as a multiple of the font height. 
	 */
	public LedgerFrame setColumnPadding(double columnPaddingFactor) { 
		this.columnPaddingFactor = columnPaddingFactor ; 
		return this ; 
	}
	
	@Override
	public void write(Block block) { 
		freezeColumns(); 
		if (block instanceof LedgerRow) { 
			if (haveAllRows) throw new RuntimeException("No further ledger rows may be added."); 
			//////  Keep a local copy so we can layout the columns
			rowList.add((LedgerRow) block); 
			//////  Let the super-class do its work. 
			super.write(block); 
		} else if (block instanceof EmptyLedgerBlock) { 
			super.write(block);
		} else { 
			throw new RuntimeException("Cannot add "+block.getClass().getCanonicalName()+" objects to a LedgerFrame"); 
		}
	}
	
	// TODO Glitch - possible to have orphaned column-titles at bottom of page. 
	/*
	 * Hmm... we could add a isHeader flag to Block. 
	 * Or, we could just say: if you want that fancy functionality, write a Word-document. 
	 */
	
	/**
	 * Convenience method: calls {@link #write(Block)}. 
	 * Arguments may be <code>null</code>, but ensure you have one for each column. 
	 */
	public void writeRow(String firstColumn, String secondColumn) { 
		write(new LedgerRow(new String[] {firstColumn, secondColumn})); 
	}
	
	/**
	 * Convenience method: calls {@link #write(Block)}. 
	 * Arguments may be <code>null</code>, but ensure you have one for each column. 
	 */
	public void writeRow(String firstColumn, String secondColumn, String thirdColumn) { 
		write(new LedgerRow(new String[] {firstColumn, secondColumn, thirdColumn})); 
	}
	/**
	 * Convenience method: calls {@link #write(Block)}. 
	 * Arguments may be <code>null</code>, but ensure you have one for each column. 
	 */
	public void writeRow(String firstColumn, String secondColumn, String thirdColumn, String fourthColumn) { 
		write(new LedgerRow(new String[] {firstColumn, secondColumn, thirdColumn, fourthColumn})); 
	}
	
	/**
	 * Convenience method: calls {@link #write(Block)}. 
	 * Arguments may be <code>null</code>, but ensure you have one for each column. 
	 */
	public void writeRow(String firstColumn, String secondColumn, String thirdColumn, String fourthColumn, String fifthColumn) { 
		write(new LedgerRow(new String[] {firstColumn, secondColumn, thirdColumn, fourthColumn, fifthColumn})); 
	}
	
	/**
	 * Convenience method: calls {@link #write(Block)}. 
	 * Arguments may be <code>null</code>, but ensure you have one for each column. 
	 */
	public void writeRow(String firstColumn, String secondColumn, String thirdColumn, String fourthColumn, String fifthColumn, String sixthColumn) { 
		write(new LedgerRow(new String[] {firstColumn, secondColumn, thirdColumn, fourthColumn, fifthColumn, sixthColumn})); 
	}
	
	/**
	 * Convenience method: calls {@link #write(Block)}. 
	 * Arguments may be <code>null</code>, but ensure you have one for each column. 
	 */
	
	public void writeRow(String firstColumn, String secondColumn, String thirdColumn, String fourthColumn, String fifthColumn, String sixthColumn, String seventhColumn) { 
		write(new LedgerRow(new String[] {firstColumn, secondColumn, thirdColumn, fourthColumn, fifthColumn, sixthColumn, seventhColumn})); 
	}
	
	/**
	 * Convenience method: calls {@link #write(Block)}. 
	 * Arguments may be <code>null</code>, but ensure you have one for each column. 
	 */
	public void writeRow(String firstColumn, String secondColumn, String thirdColumn, String fourthColumn, String fifthColumn, String sixthColumn, String seventhColumn, String eighthColumn) { 
		write(new LedgerRow(new String[] {firstColumn, secondColumn, thirdColumn, fourthColumn, fifthColumn, sixthColumn, seventhColumn, eighthColumn})); 
	}
	
	/**
	 * Convenience method: calls {@link #write(Block)}. 
	 * Arguments may be <code>null</code>, but ensure you have one for each column. 
	 */
	public void writeRow(String firstColumn, String secondColumn, String thirdColumn, String fourthColumn, String fifthColumn, String sixthColumn, String seventhColumn, String eighthColumn, String ninthColumn) { 
		write(new LedgerRow(new String[] {firstColumn, secondColumn, thirdColumn, fourthColumn, fifthColumn, sixthColumn, seventhColumn, eighthColumn, ninthColumn})); 
	}
	
	public LedgerFrame freezeRows() { 
		if (!haveAllRows) { 
			this.haveAllRows = true ; 
			if (rowList.isEmpty()) { 
				write(new EmptyLedgerBlock()); 
			}
		}
		return this ; 
	}
	
	@Override
	public void noteNewPage(Page page) { 
		this.isTitleRowPending = true ; 
	}
	
	/**
	 * Computes the sizes of all ledger rows and their cells. 
	 * A {@link PlacedBlock} object is computed for each of these 
	 * (see {@link LedgerRow#cellPlacedBlocks} and {@link LedgerRow#placedBlock_row}). 
	 * <p>
	 * The between-row padding is added to the top of each row. Thus, vertical positions within the row are offset by the padding. 
	 * However, the between-column padding is external to the cells. 
	 * <p>
	 * The positions are not calculated. 
	 * @param receivedQuill 
	 * @param titlesRow 
	 */
	public void calculateSizes(Quill receivedQuill, Layout receivedLayout, LedgerTitlesRow titlesRow) throws IOException {
		if (titlesRow==null) throw new NullPointerException("titles-row should not be null"); 
		double totalColumnPadding = columnPadding * (columnCount-1); 
		double maxSumCellWidth = receivedLayout.maxWidth - totalColumnPadding ; 
		int rowCount = rowList.size(); // Excludes title-rows. 
		//////  Compute widths of all columns, with loose width constraint. (Just the first pass. Some may be recomputed.)
		double [] firstPassWidths = new double[columnCount] ; 
		Block [][] pipeRevertBlocks = new Block[rowCount][columnCount] ; // First block in the frames of multiline cells. 
		{
			Layout firstPassLayout = receivedLayout.copyTight(true, true); 
			for (int columnIndex=0 ; columnIndex<columnCount ; columnIndex++) { 
				LedgerColumnConfig column = columns[columnIndex];
				//// Content in all rows
				Quill columnQuill = columnQuills[columnIndex];
				double maxWidth = titlesRow.titlePlacedBlocks[columnIndex].getWidth(); // Must be at least as wide as title.  
				for (int rowIndex=0 ; rowIndex<rowCount ; rowIndex++) { 
					LedgerRow row = rowList.get(rowIndex); 
					Block cell = row.cellBlocks[columnIndex]; 
					if (column.isMultiline) { 
						FrameReading cellFrame = (FrameReading) cell ; 
						pipeRevertBlocks[rowIndex][columnIndex] = cellFrame.pipe.peek() ; 
					}
					PlacedBlock placedBlock = cell.fill(columnQuill, firstPassLayout);
					row.cellPlacedBlocks[columnIndex] = placedBlock ; 
					if (placedBlock.getWidth()>maxWidth) maxWidth = placedBlock.getWidth(); 
				}
				firstPassWidths[columnIndex] = maxWidth ; 
			}
		}
		/* Here we know: All cells have been tentatively filled, ie, have a PlacedBlock. */
		//////  Compress width of multi-line columns, if necessary 
		{
			double summedFirstPassWidth = 0 ; 
			for (double w : firstPassWidths) summedFirstPassWidth += w ; 
			if (summedFirstPassWidth>maxSumCellWidth) { 
				/* Here we know: Summed widths exceeds maximum allowed. */
				double summedFixedWidths = 0 ; // ie, columns that cannot be multiline. 
				int countVariableWidths = 0 ; 
				for (int columnIndex=0 ; columnIndex<columnCount ; columnIndex++) { 
					LedgerColumnConfig config = columns[columnIndex]; 
					if (config.isMultiline) { 
						countVariableWidths ++ ; 
					} else { 
						summedFixedWidths += firstPassWidths[columnIndex] ; 
					}
				}
				double variableWidthSpaceAvailable = maxSumCellWidth - summedFixedWidths ; 
				if (countVariableWidths==1) { 
					/* Here we know: There's only one column we can compress. So it gets all the adjustment. */ 
					int variableColumnIndex = -1 ; 
					{ 
						while (!columns[++variableColumnIndex].isMultiline) {} 
					}
					LedgerColumnConfig column = columns[variableColumnIndex];
					Quill columnQuill = columnQuills[variableColumnIndex];
					Layout columnLayout = receivedLayout.copyTight(true, true).setSize(variableWidthSpaceAvailable, null);  
					for (int rowIndex=0 ; rowIndex<rowCount ; rowIndex++) { 
						LedgerRow row = rowList.get(rowIndex); 
						Block cell = row.cellBlocks[variableColumnIndex]; 
						//// Don't recompute if it's already within the maximum width 
						PlacedBlock oldPlacedBlock = row.cellPlacedBlocks[variableColumnIndex]; 
						if (oldPlacedBlock.getWidth()<=variableWidthSpaceAvailable) continue ; 
						//// Revert the cell's frame to the first word 
						if (column.isMultiline) { 
							FrameReading cellFrame = (FrameReading) cell ; 
							cellFrame.pipe.reader.revertTo(pipeRevertBlocks[rowIndex][variableColumnIndex]);
						}
						//// Recompute the layout
						PlacedBlock newPlacedBlock = row.cellBlocks[variableColumnIndex].fill(columnQuill, columnLayout);
						row.cellPlacedBlocks[variableColumnIndex] = newPlacedBlock ; 
					}
				} else { 
					/* Here we know: There's multiple columns with variable width. We must distribute the compression. */ 
					/* The already narrow columns, we'll exclude them from the compression. */
					//// Choose the columns to compress
					ArrayList<Integer> compressableColumnIndices = new ArrayList<Integer>(); 
					{
						double narrowThreshold = (variableWidthSpaceAvailable / countVariableWidths) * narrowColumnFactor ; 
						for (int columnIndex=0 ; columnIndex<columnCount ; columnIndex++) { 
							if (columns[columnIndex].isMultiline && firstPassWidths[columnIndex]>=narrowThreshold) { 
								compressableColumnIndices.add(columnIndex); 
							}
						}
					}
					//// Compute the compression factor 
					double compressionFactor ; 
					{
						double summedCompressableFirstPassWidths = 0.0 ; 
						for (int columnIndex : compressableColumnIndices) summedCompressableFirstPassWidths += firstPassWidths[columnIndex] ; 
						compressionFactor = variableWidthSpaceAvailable / summedCompressableFirstPassWidths ; 
					}
					//// Recompute the column widths, laying out the cells again. 
					{ 
						for (int columnIndex : compressableColumnIndices) { 
							LedgerColumnConfig column = columns[columnIndex];
							Quill columnQuill = columnQuills[columnIndex];
							double compressedColumnWidth = firstPassWidths[columnIndex] * compressionFactor ; 
							Layout columnLayout = receivedLayout.copyTight(true, true).setSize(compressedColumnWidth, null); 
							for (int rowIndex=0 ; rowIndex<rowCount ; rowIndex++) { 
								LedgerRow row = rowList.get(rowIndex); 
								Block cell = row.cellBlocks[columnIndex]; 
								//// Don't recompute if it's already within the maximum width 
								PlacedBlock oldPlacedBlock = row.cellPlacedBlocks[columnIndex]; 
								if (oldPlacedBlock.getWidth()<=compressedColumnWidth) continue ; 
								//// Revert the cell's frame to the first word 
								if (column.isMultiline) { 
									FrameReading cellFrame = (FrameReading) cell ; 
									cellFrame.pipe.reader.revertTo(pipeRevertBlocks[rowIndex][columnIndex]);
								}
								//// Recompute the layout
								PlacedBlock newPlacedBlock = row.cellBlocks[columnIndex].fill(columnQuill, columnLayout);
								row.cellPlacedBlocks[columnIndex] = newPlacedBlock ; 
							}
						}
					}
				}
			} 
		}
		/* Here we know: All cells have been filled with their final size. 
		 * But the position fields of the PlacedBlocks have not been set. */
		//////  Compute the width 
		this.columnWidths = new double[columnCount] ; 
		{ 
			for (int columnIndex=0 ; columnIndex<columnCount ; columnIndex++) { 
				double columnWidth = 0.0 ; 
				for (LedgerRow row : rowList) { 
					PlacedBlock placedBlock = row.cellPlacedBlocks[columnIndex] ; 
					if (placedBlock.getWidth()>columnWidth) columnWidth = placedBlock.getWidth(); 
				}
				columnWidths[columnIndex] = columnWidth ; 
			}
		}
		this.columnLefts = new double[columnCount] ; 
		this.columnRights = new double[columnCount] ; 
		double summedWidths = 0.0 ; 
		{ 
			for (int columnIndex=0 ; columnIndex<columnCount ; columnIndex++) { 
				summedWidths += columnWidths[columnIndex] ; 
				if (columnIndex>0) { 
					columnLefts[columnIndex] = columnLefts[columnIndex-1] + columnWidths[columnIndex-1] + columnPadding ; 
					columnRights[columnIndex-1] = columnLefts[columnIndex] ; 
				} else { 
					columnLefts[columnIndex] = 0 ; 
				}
				columnRights[columnCount-1] = columnLefts[columnCount-1] + columnWidths[columnCount-1] ; 
			}
		}
		this.frameWidth = receivedLayout.isWidthTight ? summedWidths : receivedLayout.maxWidth ; 
		//////  Compute sizes for the individual rows
		for (LedgerRow row : rowList) { 
			double rowHeight = 0.0 ; 
			for (int columnIndex=0 ; columnIndex<columnCount ; columnIndex++) { 
				PlacedBlock placedBlock = row.cellPlacedBlocks[columnIndex] ; 
				if (placedBlock.getHeight()>rowHeight) rowHeight = placedBlock.getHeight(); 
			}
			rowHeight += rowPadding ; 
			row.placedBlock_row = row.new PlacedBlock().setDimensions(frameWidth, rowHeight); 
		}
	}
	
	@Override
	public PlacedBlock fill(Quill receivedQuill, Layout receivedLayout) throws IOException { 
		freezeRows(); 
		/*
		 * Note: A LedgerFrame is two levels of container: 
		 * the FrameVertical containing the ledger rows, and 
		 * several LedgerRow objects containing the individual fields of an row. 
		 */
		if (!rowList.isEmpty()) { 
			Layout layout = inheritLayout(receivedLayout); 
			//////  Prepend a titles-row, if needed
			LedgerTitlesRow titlesRow = null ; 
			if (isTitleRowPending) { 
				titlesRow = new LedgerTitlesRow();
				pipe.prepend(titlesRow);
				titlesRow.prefill(receivedQuill, receivedLayout);
				this.isTitleRowPending = false ; 
			}
			//////  Set up the Quills for each column 
			Quill tmpQuill = super.inheritQuill(receivedQuill); // Duplicates a Quill created in parent class. But need a copy here, before parent's fill() is called. 
			for (int columnIndex=0 ; columnIndex<columnCount ; columnIndex++) { 
				columnQuills[columnIndex] = inheritColumnQuill(tmpQuill, columnIndex); 
			}
			//////  Calculate the column widths, if necessary 
			if (!hasCalculatedWidths) { 
				this.rowPadding = tmpQuill.getFontHeight() * rowPaddingFactor ; 
				this.columnPadding = tmpQuill.getStringWidth("m") * columnPaddingFactor ; 
				calculateSizes(receivedQuill, layout, titlesRow); 
				this.hasCalculatedWidths = true ; 
			}
			/* Here we know: each ledger cell has a PlacedBlock with its dimensions, but is not positioned in its LedgerRow.
			 * The LedgerRow objects don't have PlacedBlocks yet. 
			 * The VerticalFrame's fill() method will call fill() on each LedgerRow, and position each. 
			 */
		}
		//////  Invoke parent-class's frame-filling functionality
		/* Here we know: the pipe has not been read. */
		return super.fill(receivedQuill, receivedLayout); 
	}
	
	private Quill [] columnQuills ; 
	
	protected Quill inheritColumnQuill(Quill receivedQuill, int columnIndex) {
		return receivedQuill ; 
	}
	
	@Override
	public void draw(Canvas canvas, double left, double top, double width, double height) throws IOException { 
		freezeRows(); 
		// TODO Must print column titles when we go to a new page. 
		//////  Parent class method does most of the work. 
		super.draw(canvas, left, top, width, height);
	}
	
	public class LedgerRow extends Block { 
		
		private final String [] cellStrings ; 
		
		private final Block [] cellBlocks ; 
		
		private final PlacedBlock [] cellPlacedBlocks ; 
		
		private PlacedBlock placedBlock_row ; 
		
		public LedgerRow(String [] cellStrings) { 
//			this.loggingVerbosity = ALL_9 ; 
			freezeColumns(); 
			if (cellStrings.length!=columnCount) throw new RuntimeException("Ledger has "+columnCount+" columns, but creating entry with "+cellStrings.length+" columns."); 
			this.cellStrings = cellStrings ; 
			this.cellBlocks = new Block[columnCount] ; 
			this.cellPlacedBlocks = new PlacedBlock[columnCount] ; 
			for (int i=0 ; i<columnCount ; i++) { 
				String text = cellStrings[i];
				if (text!=null) { 
					if (columns[i].isMultiline) { 
						FrameReading frameReading = new FrameReading();
						frameReading.writeTextSplittable(text);
						cellBlocks[i] = frameReading; 
					} else { 
						cellBlocks[i] = new StringBlock(text); 
					}
				}
			}
		}
		
		public LedgerRow(List<String> cellList) { 
			this(Str.toArray(cellList)); 
		}
		
		/**
		 * Sets the {@linkplain Block} for the given column. 
		 * <p>
		 * Warning: Use with caution. 
		 * {@linkplain LedgerFrame} may assume the column configuration is correct when laying out and drawing the cell. 
		 */
		public void setColumn(int columnIndex, Block cellBlock) { 
			cellBlocks[columnIndex] = cellBlock ; 
			cellStrings[columnIndex] = null ; 
		}

		@Override
		public PlacedBlock fill(Quill quill, Layout receivedLayout) throws IOException { 
			/* Here we know: the LedgerFrame fill() method has computed PlacedBlock objects, and set their dimensions. */
			DebugLog.add(ENTERING_5, this, null, logMessage_enteringFill, null, null, false); 
			for (int columnIndex=0 ; columnIndex<columnCount ; columnIndex++) { 
				LedgerColumnConfig columnConfig = columns[columnIndex];
				PlacedBlock placedBlock_cell = cellPlacedBlocks[columnIndex] ; 
				if (placedBlock_cell==null) continue ; 
				double spareSpace = columnWidths[columnIndex] - placedBlock_cell.getWidth(); 
				double spareHeight = placedBlock_row.getHeight() - placedBlock_cell.getHeight() - rowPadding ; 
				double justificationSpace = Layout.getJustificationSpace(columnConfig.justification, spareSpace); 
				double alignmentSpace = Layout.getAlignmentSpace(columnConfig.getAlignment(), spareHeight); 
				placedBlock_cell.setOffsetInContainer(columnLefts[columnIndex]+justificationSpace, rowPadding+alignmentSpace); 
			}
			DebugLog.add(LEAVING_6, placedBlock_row, null, logMessage_leavingFill, null, null, false); 
			return placedBlock_row ;
		}

		@Override
		public void draw(Canvas canvas, double left, double top, double width, double height) throws IOException { 
			for (int i=0 ; i<columnCount ; i++) { 
				PlacedBlock placedBlock_cell = cellPlacedBlocks[i] ; 
				if (placedBlock_cell==null) continue ; 
				placedBlock_cell.draw(canvas, left+placedBlock_cell.getLeftInContainer(), top+placedBlock_cell.getTopInContainer()); 
			}
		} 
	}
	
	public class LedgerTitlesRow extends Block { 
		
		private final String [] titleStrings ; 
		
		private final Block [] titleBlocks ; 
		
		private final PlacedBlock [] titlePlacedBlocks ; 
		
		private PlacedBlock titleRowPlacedBlock ; 
		
		public LedgerTitlesRow() { 
			freezeColumns(); 
			this.titleStrings = new String[columnCount] ; 
			for (int i=0 ; i<columnCount ; i++) titleStrings[i] = columns[i].columnTitle ; 
			this.titleBlocks = new Block[columnCount] ; 
			this.titlePlacedBlocks = new PlacedBlock[columnCount] ; 
			for (int i=0 ; i<columnCount ; i++) { 
				String text = titleStrings[i];
				if (text!=null) { 
					titleBlocks[i] = new StringBlock(text); 
				}
			}
		}

		public void prefill(Quill receivedQuill, Layout receivedLayout) throws IOException {
			this.quill = inheritQuill(receivedQuill); 
			Layout layout = inheritLayout(receivedLayout); 
			double maxHeight = 0.0 ; 
			for (int columnIndex=0 ; columnIndex<columnCount ; columnIndex++) { 
				Block titleBlock = titleBlocks[columnIndex];
				if (titleBlock==null) continue ; 
				titlePlacedBlocks[columnIndex] = titleBlock.fill(quill, receivedLayout) ; 
				double titleHeight = titlePlacedBlocks[columnIndex].getHeight(); 
				if (titleHeight>maxHeight) maxHeight = titleHeight ; 
			}
			this.titleRowPlacedBlock = new PlacedBlock(); 
			titleRowPlacedBlock.setDimensions(frameWidth, maxHeight); 
		}

		@Override
		public PlacedBlock fill(Quill receivedQuill, Layout receivedLayout) throws IOException { 
			DebugLog.add(ENTERING_5, this, null, logMessage_enteringFill, null, null, false); 
			double rowHeight = titleRowPlacedBlock.getHeight(); 
			for (int columnIndex=0 ; columnIndex<columnCount ; columnIndex++) { 
				LedgerColumnConfig columnConfig = columns[columnIndex];
				PlacedBlock placedBlock = titlePlacedBlocks[columnIndex] ; 
				double spareSpace = columnWidths[columnIndex] - placedBlock.getWidth(); 
				double spareHeight = rowHeight - placedBlock.getHeight() ; 
				double justificationSpace = Layout.getJustificationSpace(columnConfig.justification, spareSpace); 
				double alignmentSpace = Layout.getAlignmentSpace(columnConfig.getAlignment(), spareHeight); 
				placedBlock.setOffsetInContainer(columnLefts[columnIndex]+justificationSpace, alignmentSpace); 
			}
			DebugLog.add(LEAVING_6, titleRowPlacedBlock, null, logMessage_leavingFill, null, null, false);
			return titleRowPlacedBlock ;
		}

		@Override
		public void draw(Canvas canvas, double left, double top, double width, double height) throws IOException { 
			for (int i=0 ; i<columnCount ; i++) { 
				PlacedBlock placedBlock_cell = titlePlacedBlocks[i] ; 
				if (placedBlock_cell==null) continue ; 
				placedBlock_cell.draw(canvas, left+placedBlock_cell.getLeftInContainer(), top+placedBlock_cell.getTopInContainer()); 
			}
		} 
		
		@Override
		protected Quill inheritQuill(Quill receivedQuill) {
			return super.inheritQuill(receivedQuill).copyBold();
		}
		
		/**
		 * Does little: the justification and alignment are taken from the column configuration. 
		 */
		@Override
		protected Layout inheritLayout(Layout receivedLayout) {
			return super.inheritLayout(receivedLayout).copyBottom();
		}
	}
	
	private class EmptyLedgerBlock  extends FrameReading { 
		
		EmptyLedgerBlock() { 
			write(new StringBlock("This ledger is empty. Column titles ")); 
			for (LedgerColumnConfig column : columns) { 
				write(new StringBlock(" :: ")); 
				write(new StringBlockBold(column.columnTitle)); 
			}
		}
	}

}
// TODO Handle a ledger with no rows nicely. Currently nov19 throws a NPE. 


