package org.blockframe.blocks;

import java.io.IOException;

import org.blockframe.core.BlockPipe;
import org.blockframe.core.Canvas;
import org.blockframe.core.DebugLog;
import org.blockframe.core.Frame;
import org.blockframe.core.Layout;
import org.blockframe.core.PdfChapter;
import org.blockframe.core.PdfDocument;
import org.blockframe.core.Quill;

import com.gingerdroids.utils_java.Util;

import org.blockframe.core.Block.PlacedBlock;
import org.blockframe.core.PdfChapter.Page;

/**
 * Writes content in multiple columns. (One column is permitted.)
 * <p>
 * To use this as the top-level frame of a page, override the {@link PdfChapter#newPageFrame(BlockPipe, Page)} method. 
 */
public class MultiColumnFrame extends Frame { 
	
	public final int columnCount ; 
	
	private final Frame [] columnFrames ; 
	private final PlacedBlock [] placedColumnFrames ; 
	
//	private final Frame leftFrame ; 
//	
//	private final Frame rightFrame ; 
//	
//	private PlacedBlock leftPlacedFrame ; 
//	
//	private PlacedBlock rightPlacedFrame ; 
	
	private String gapWidthTemplate = "WWWWWWWW"; 
	
	/**
	 * Step from left of one column to left of the next. 
	 * Equivalently, the width of a column plus the width of the gap. 
	 */
	private double columnStep ; 

//	/**
//	 * Coordinate of the left-edge of the second column. 
//	 */
//	double rightFrameLeftEdge ; 
	
	public MultiColumnFrame(int columnCount) { 
		super(); 
		this.columnCount = columnCount ; 
		this.columnFrames = new FrameVertical[columnCount] ; 
		for (int i=0 ; i<columnCount ; i++) columnFrames[i] = new FrameVertical(pipe); 
		this.placedColumnFrames = new PlacedBlock[columnCount] ; 
	}
	
	public MultiColumnFrame(BlockPipe pipe, int columnCount) { 
		super(pipe); 
		this.columnCount = columnCount ; 
		this.columnFrames = new FrameVertical[columnCount] ; 
		for (int i=0 ; i<columnCount ; i++) columnFrames[i] = new FrameVertical(pipe); 
		this.placedColumnFrames = new PlacedBlock[columnCount] ; 
	}

	@Override
	public PlacedBlock fill(Quill receivedQuill, Layout receivedLayout) throws IOException { 
		DebugLog.add(ENTERING_5, this, null, logMessage_enteringFill, null, null, true); 
		this.quill = inheritQuill(receivedQuill) ; 
		Layout outerLayout = inheritLayout(receivedLayout); 
		DebugLog.add(DETAIL_8, this, null, pipe.logMessage_pipe, null, null, false); 
		DebugLog.add(DETAIL_8, this, null, Layout.logMessage_layout, outerLayout, null, false); 
		//////  Compute inner layout
		double outerMaxWidth = outerLayout.maxWidth ; 
		double outerMaxHeight = outerLayout.maxHeight ; 
		double gapWidth = quill.getStringWidth(gapWidthTemplate); 
		double innerMaxWidth = ((outerMaxWidth+gapWidth) / columnCount) - gapWidth ; 
		Layout innerLayout = outerLayout.copy().setSize(innerMaxWidth, null); 
		this.columnStep = innerMaxWidth + gapWidth ; 
		//////  Fill the columns 
		for (int i=0 ; i<columnCount ; i++) { 
			placedColumnFrames[i] = columnFrames[i].fill(quill, innerLayout); 
		}
		//////  Set my dimensions
		PlacedBlock placedBlock = new PlacedBlock().setDimensions(outerMaxWidth, outerMaxHeight); // Does not honour Layout's "tight" property. 
		DebugLog.add(LEAVING_6, placedBlock, null, logMessage_leavingFill, null, null, false); 
		//////  Bye bye
		return placedBlock ; 
	}
	
	@Override
	public void draw(Canvas canvas, double left, double top, double width, double height) throws IOException { 
		for (int i=0 ; i<columnCount ; i++) { 
			placedColumnFrames[i].draw(canvas, left+columnStep*i, top);
		}
	}

	/**
	 * Returns the template determining how wide the gap between columns is. 
	 */
	public String getGapWidthTemplate() {
		return gapWidthTemplate;
	}

	/**
	 * Sets the template determining how wide the gap between columns is. 
	 * @param gapWidthTemplate May not be null. 
	 */
	public void setGapWidthTemplate(String gapWidthTemplate) { 
		if (gapWidthTemplate==null) throw new IllegalArgumentException("The gap-width-template may not be null"); 
		this.gapWidthTemplate = gapWidthTemplate;
	}
	
}