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
 * Writes content in two columns. 
 * <p>
 * To use this as the top-level frame of a page, override the {@link PdfChapter#newPageFrame(BlockPipe, Page)} method. 
 * 
 * @see {@link MultiColumnFrame} provides similar functionality. 
 */
public class TwoColumnFrame extends Frame { 
	
	private final Frame leftFrame ; 
	
	private final Frame rightFrame ; 
	
	private PlacedBlock leftPlacedFrame ; 
	
	private PlacedBlock rightPlacedFrame ; 
	
	private String gapWidthTemplate = "WWWWWWWW";

	/**
	 * Coordinate of the left-edge of the second column. 
	 */
	double rightFrameLeftEdge ; 
	
	public TwoColumnFrame() { 
		super(); 
		this.leftFrame = new FrameVertical(pipe); 
		this.rightFrame = new FrameVertical(pipe); 
	}
	
	public TwoColumnFrame(BlockPipe pipe) { 
		super(pipe); 
		this.leftFrame = new FrameVertical(pipe); 
		this.rightFrame = new FrameVertical(pipe); 
	}
	
	/**
	 * Factory method which returns a simple {@link FrameVertical} if we don't want two columns. 
	 * <p>
	 * Caveat: Calling this as a subclass method will not return an instance of the subclass! 
	 */
	public static Frame create(boolean wantTwoColumn) { 
		if (wantTwoColumn) return new TwoColumnFrame(); 
		return new FrameVertical(); 
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
		double innerMaxWidth = (outerMaxWidth-gapWidth) / 2 ; 
		Layout innerLayout = outerLayout.copy().setSize(innerMaxWidth, null); 
		this.rightFrameLeftEdge = innerMaxWidth + gapWidth ; 
		//////  Fill the two columns 
		this.leftPlacedFrame = leftFrame.fill(quill, innerLayout); 
		this.rightPlacedFrame = rightFrame.fill(quill, innerLayout); 
		//////  Set my dimensions
		PlacedBlock placedBlock = new PlacedBlock().setDimensions(outerMaxWidth, outerMaxHeight); // Does not honour Layout's "tight" property. 
		DebugLog.add(LEAVING_6, placedBlock, null, logMessage_leavingFill, null, null, false); 
		//////  Bye bye
		return placedBlock ; 
	}
	
	@Override
	public void draw(Canvas canvas, double left, double top, double width, double height) throws IOException { 
		leftPlacedFrame.draw(canvas, left, top);
		rightPlacedFrame.draw(canvas, left+rightFrameLeftEdge, top); 
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