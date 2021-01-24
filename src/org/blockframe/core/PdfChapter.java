package org.blockframe.core;

import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.util.Matrix;
import org.blockframe.blocks.FrameVertical;
import org.blockframe.core.Block.PlacedBlock;
import org.blockframe.core.DebugLog.StringGetter;
import org.blockframe.core.DebugLog.Verbosity;
import org.blockframe.core.Frame.PlacedFrame;

import com.gingerdroids.utils_java.Util;

/**
 * Top level class for adding pages to a PDF document. 
 * This class manages receiving content from the client, laying the content out over one or more pages, drawing the content and adding the pages to the supplied PDF-Box document. 
 * <p>
 * Clients who want to generate their document entirely within BlockFrame will probably code a subclass of {@linkplain PdfChapter} or its subclass {@link PdfDocument}. 
 */
public class PdfChapter implements Verbosity { 
	
	/**
	 * Threshold for logging debug messages. Zero is no messages, large is more. 
	 * @see DebugLog
	 */
	public double loggingVerbosity = 0 ; 
	
	/**
	 * The PDF-Box document we are writing into. 
	 */
	protected final PDDocument pdDocument ; 
	
	private double leftMargin = 36 ; // Default units are 1/72in, that is, 1pt. 
	private double topMargin = 36 ; 
	private double rightMargin = 36 ; 
	private double bottomMargin = 36 ; 

	private float pageWidth;
	private float pageHeight; 
	
	private boolean wantLandscapeRotation = false ; 
	
	/**
	 * Paper size. Initially null, may be left null. 
	 */
	private PDRectangle pdRectangle = null ; 
	
	protected final BlockPipe pipe ; 
	
	private Page currentPage ;

	private int pageCount = 0 ; 
	
	/**
	 * Maximum number of pages that can be generated. 
	 * This is configurable. It's mainly intended to catch infinite loops. 
	 */
	private Integer maxPageCount = 1000 ;
	
	/**
	 * Utility field, used in {@link #setPageSizeFields()}. 
	 */
	private PDPage measuringPage ; 
	
	/**
	 * Constructor. 
	 * @param pdDocument The PDF-Box document we are writing to. 
	 */
	protected PdfChapter(PDDocument pdDocument) { 
		this.pdDocument = pdDocument ; 
		this.pipe = new BlockPipe("document"); 
		setPageSizeFields(); 
	}

	/**
	 * Getter method for field {@link #leftMargin}. 
	 * Use {@link #setMargins(Double, Double, Double, Double)} to modify the fields. 
	 */
	public final double getLeftMargin() {
		return leftMargin;
	}

	/**
	 * Getter method for field {@link #topMargin}. 
	 * Use {@link #setMargins(Double, Double, Double, Double)} to modify the fields. 
	 */
	public final double getTopMargin() {
		return topMargin;
	}

	/**
	 * Getter method for field {@link #rightMargin}. 
	 * Use {@link #setMargins(Double, Double, Double, Double)} to modify the fields. 
	 */
	public final double getRightMargin() {
		return rightMargin;
	}

	/**
	 * Getter method for field {@link #bottomMargin}. 
	 * Use {@link #setMargins(Double, Double, Double, Double)} to modify the fields. 
	 */
	public final double getBottomMargin() {
		return bottomMargin;
	}

	/*
	public final double getPageWidth() { 
		return pageWidth ; 
	}
	
	public final double getPageHeight() { 
		return pageHeight ; 
	}
	*/

	/**
	 * Returns the width of the page's content. That is, the page width less the margins. 
	 */
	public final double getContentWidth() { 
		return pageWidth - (leftMargin+rightMargin) ; 
	}

	/**
	 * Returns the height of the page's content. That is, the page height less the margins. 
	 */
	public final double getContentHeight() { 
		return pageHeight - (topMargin+bottomMargin) ; 
	}

	/**
	 * Returns the PdfBox object which gives BlockFrame the paper size. 
	 */
	public PDRectangle getPdRectangle() {
		return pdRectangle;
	}

	/**
	 * Sets the PdfBox object which gives BlockFrame the paper size. 
	 */
	public void setPdRectangle(PDRectangle pdRectangle) {
		this.pdRectangle = pdRectangle;
		setPageSizeFields(); 
	}

	public final boolean isLandscapeRotation() {
		return wantLandscapeRotation;
	}

	public void setLandscapeRotation(boolean wantLandscapeRotation) {
		this.wantLandscapeRotation = wantLandscapeRotation;
		setPageSizeFields(); 
	}

	/**
	 * Sets the four margin fields. 
	 * Any <code>null</code> argument is ignored. 
	 * <p>
	 * The default units are 1/72in, that is, 1pt. 
	 */
	public void setMargins(Double leftMargin, Double topMargin, Double rightMargin, Double bottomMargin) { 
		if (leftMargin!=null) this.leftMargin = leftMargin ; 
		if (topMargin!=null) this.topMargin = topMargin ; 
		if (rightMargin!=null) this.rightMargin = rightMargin ; 
		if (bottomMargin!=null) this.bottomMargin = bottomMargin ; 
	}
	
	private void setPageSizeFields() { 
		if (currentPage!=null) { 
			this.measuringPage = currentPage.pdPage ; 
		} else if (measuringPage==null) { 
			this.measuringPage = newPdPage(); 
		}
		final PDRectangle mediaBox = measuringPage.getMediaBox(); 
		this.pageWidth = wantLandscapeRotation ? mediaBox.getHeight() : mediaBox.getWidth(); 
		this.pageHeight = wantLandscapeRotation ? mediaBox.getWidth() : mediaBox.getHeight(); 
	}
	
	/**
	 * Manages the creation, filling and drawing of pages to absorb all the blocks written into this {@link PdfChapter} instance. 
	 * <p>
	 * Although this method is <code>final</code>, most of its functionality calls on overridable methods. 
	 */
	public final void makePages() throws IOException { 
		pipe.writer.close(); 
		while (pipe.reader.hasMore()) { 
			if (maxPageCount!=null && pageCount>=maxPageCount) throw new RuntimeException("Have exceeded maximum page count of "+maxPageCount); 
			if (loggingVerbosity>=Verbosity.ENTERING_5) DebugLog.add(null, "Starting page "+(pageCount+1), false); // pageCount is incremented in newPage(). 
			//// PdfBox stuff: create PdfBox page, add to PdfBox document
			PDPage pdPage = newPdPage(); 
			pdDocument.addPage(pdPage); 
			//// Update the current page field, and its sizes
			Page prevPage = currentPage;
			this.currentPage = null ; 
			Page newPage = newPage(pipe, pdPage, prevPage);
			this.measuringPage = pdPage ; 
			setPageSizeFields(); 
			this.currentPage = newPage ; 
			//// Fill the page
			if (loggingVerbosity>=Verbosity.LEAVING_6) DebugLog.add(null, "Filling page "+pageCount, false);
			pipe.noteNewPage(newPage);
			PlacedFrame placedPageFrame = fillPageFrame(newPage); 
			if (placedPageFrame.size()==0) throw new RuntimeException("Page did not read anything from pipe."); 
			placedPageFrame.setOffsetInContainer(leftMargin, topMargin); 
			//// Draw the page
			if (loggingVerbosity>=Verbosity.LEAVING_6) DebugLog.add(null, "Drawing page "+pageCount, false);
			Canvas canvas = new Canvas(pdDocument, pdPage); 
			if (wantLandscapeRotation) canvas.stream.transform(new Matrix(0, 1, -1, 0, pageWidth, 0));
			drawPageFrame(canvas, placedPageFrame.getLeftInContainer(), placedPageFrame.getTopInContainer(), placedPageFrame); 
			canvas.close(); 
			//// Administrivia, and Bye bye
			if (loggingVerbosity>=Verbosity.GENERAL_7) DebugLog.add(null, "Finished page "+pageCount, false);
		}
	}
	
	/**
	 * Manages the creation, filling and drawing of a single page to absorb all the blocks written into this {@link PdfChapter} instance. 
	 * <p>
	 * Although this method is <code>final</code>, most of its functionality calls on overridable methods. 
	 * <p>
	 * WARNING: this is <b>not</b> inside the loop of {@link #makePages()}. It is separate functionality, used for single page documents. 
	 */
	public final void makePage() throws IOException { 
		pipe.writer.close(); 
		//// PdfBox stuff: create PdfBox page, add to PdfBox document
		PDPage pdPage = newPdPage(); 
		pdDocument.addPage(pdPage); 
		//// Update the current page field, and its sizes
		double marginSize = 2.0;
		setMargins(marginSize, marginSize, marginSize, marginSize);
//		Page prevPage = currentPage;
		this.currentPage = null ; 
		Page newPage = newPage(pipe, pdPage, null);
		this.measuringPage = pdPage ; 
		setPageSizeFields(); 
		this.currentPage = newPage ; 
		//// Fill the page
		pipe.noteNewPage(newPage);
		PlacedFrame placedPageFrame = fillPageFrame(newPage); 
		placedPageFrame.setOffsetInContainer(leftMargin, topMargin); 
		//// Draw the page
		double tightHeight = placedPageFrame.getHeight()+2*marginSize; 
		double tightWidth = placedPageFrame.getWidth()+2*marginSize; 
		pdPage.setMediaBox(new PDRectangle((float)tightWidth, (float)tightHeight));
		Canvas canvas = new Canvas(pdDocument, pdPage); 
		if (wantLandscapeRotation) { 
			Util.cruft("Not sure that 'pageWidth' is correct in rotation transform.");
			canvas.stream.transform(new Matrix(0, 1, -1, 0, pageWidth, 0)); // BUG? Should this be pageWidth, or something else? 
		}
		drawPageFrame(canvas, placedPageFrame.getLeftInContainer(), placedPageFrame.getTopInContainer(), placedPageFrame); 
		canvas.close(); 
		////  
		if (pipe.reader.hasMore()) throw new RuntimeException("Did not place all blocks."); 
	}
	
	/**
	 * Adds a {@linkplain Block} to the list of top-level blocks in this document. 
	 * Often, each block will hold a paragraph of text. 
	 */
	public void write(Block block) { 
		pipe.writer.write(block); 
	}

	/**
	 * Returns the {@link Page} that we are currently writing to. 
	 * This exposes the top-level frame, and the {@link Layout} and {@link Quill} passed into the top-level frame. 
	 */
	public final Page getCurrentPage() {
		return currentPage;
	}
	
	/**
	 * Getter for {@link #pageCount}. 
	 * The count is zero on initialization, and is incremented in {@link #newPage(BlockPipe, PDPage, Page)} after a new {@link Page} is created, 
	 * but before {@linkplain #newPage(BlockPipe, PDPage, Page)} returns. 
	 */
	public int getPageCount() { 
		return pageCount ; 
	}
	
	/**
	 * Increments field {@link #pageCount}. 
	 * On the first page, this will have the value <code>1</code>. 
	 * That is, it is a count of the number of pages created by {@link #newPage(BlockPipe, PDPage, Page)}, including the current page. 
	 */
	public final int incrementPageCount() { 
		this.pageCount ++ ; 
		return pageCount ; 
	}
	
	/**
	 * Setter for field {@link #maxPageCount}. 
	 */
	public PdfChapter setMaxPageCount(Integer maxPageCount) { 
		this.maxPageCount = maxPageCount ; 
		return this ; 
	}
	
	/**
	 * Exposes the top-level BlockFrame objects for the current page. 
	 * <p>
	 * The PDF-Box {@link PDDocument} is exposed via the field {@link PdfChapter#pdDocument}. 
	 * Other PDF-Box objects are exposed in the {@link Canvas} object passed down the {@link Block#draw(Canvas, double, double, double, double)} pass. 
	 */
	public static class Page { 
		public final Frame frame ; 
		public final Layout layout ; 
		public final Quill quill ; 
		public final PDPage pdPage ; 
		public Page(Frame frame, Layout layout, Quill quill, PDPage pdPage) { 
			this.frame = frame ; 
			this.layout = layout ; 
			this.quill = quill ; 
			this.pdPage = pdPage ; 
		}
	}

	/**
	 * Returns a new {@link PDPage}. 
	 * <p>
	 * This implementation knows about the <code>pdRectangle</code> and <code>wantLandscapeRotation</code> fields. 
	 * <p>
	 * It is called by {@link #newPage} and {@link #setPageSizeFields}. 
	 */
	protected PDPage newPdPage() {
		PDPage pdPage = pdRectangle!=null ? new PDPage(pdRectangle) : new PDPage();
		if (wantLandscapeRotation) pdPage.setRotation(90); 
		return pdPage;
	}

	/**
	 * This is the method called by {@link #makePages()} to generate a new page. 
	 * This implementation delegates building the fields of {@link Page} to the <code>newPageXxxx</code> methods. 
	 * <p>
	 * This method, and the <code>newPageXxxx</code> methods, are intended to be overridable if you wish to configure the top-level objects of a page. 
	 * For example, see the {@link PageFrame#newPageFrame(BlockPipe, Page)} method. 
	 * <p>
	 * The method {@link #incrementPageCount()} is called after the <code>newPageXxxx</code> methods and the <code>Page</code> constructor. 
	 */
	protected Page newPage(BlockPipe pipe, PDPage pdPage, Page prevPage) { 
		Frame frame = newPageFrame(pipe, prevPage); 
		if (frame.pipe!=pipe) throw new RuntimeException("Top level frame must use BlockPipe from newPageFrame() arguments. Have you called 'super(pipe)'?"); 
		Layout layout = newPageLayout(pdPage, prevPage); 
		Quill quill = newPageQuill(prevPage);
		Page page = new Page(frame, layout, quill, pdPage); 
		incrementPageCount(); 
		DebugLog.add(DETAIL_8, frame, null, logMessage_newPage, page, null, false); 
		return page; 
	}

	private StringGetter logMessage_newPage = new StringGetter() {
		public String getString(Block block, PlacedBlock placedBlock, Object pageObject, Object arg1) { 
			Page page = (Page) pageObject ; 
			return "Created new page "+page.getClass().getSimpleName()+", page count now "+pageCount+", frame is "+page.frame.getLogName();
		}
	}; 

	/**
	 * Creates the {@link Frame} object for a new page. 
	 * 
	 * @param prevPage May be null. It is ignored in this default method. But perhaps useful in subclasses. 
	 */
	protected Frame newPageFrame(BlockPipe pipe, Page prevPage) {
		return new FrameVertical(pipe);
	}
	
	/**
	 * Creates the {@link Layout} object for a new page. 
	 * <p>
	 * The width and height of content on the page defaults to the methods {@link #getContentWidth()} and {@link #getContentHeight()}. 
	 * If you generally want to use those values, but occasionally not, you could override this method. 
	 */
	protected Layout newPageLayout(PDPage pdPage, Page prevPage) { 
		Layout layout = new Layout(getContentWidth(), getContentHeight()); 
		return layout ; 
	}
	
	/**
	 * Creates the {@link Quill} object for a new page. 
	 */
	protected Quill newPageQuill(Page prevPage) { 
		return new Quill(); 
	}
	
	/**
	 * Invokes the <code>fill</code> pass on the current page's {@link Frame}. 
	 * <p>
	 * This method is called by {@link #makePages()}. 
	 */
	protected PlacedFrame fillPageFrame(Page page) throws IOException { 
		Frame pageFrame = page.frame;
		PlacedFrame placedFrame = (PlacedFrame) pageFrame.fill(page.quill, page.layout); 
		return placedFrame ; 
	}
	
	/**
	 * Invokes the <code>draw</code> pass on the current page's {@link Frame}. 
	 * <p>
	 * This method is called by {@link #makePages()}. 
	 */
	protected void drawPageFrame(Canvas canvas, double left, double top, PlacedBlock placedPageFrame) throws IOException { 
		placedPageFrame.draw(canvas, left, top); 
	}
	
}
