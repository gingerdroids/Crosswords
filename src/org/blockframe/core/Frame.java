package org.blockframe.core;

import java.io.IOException;
import java.util.ArrayList;

import org.blockframe.blocks.FrameHorizontal;
import org.blockframe.blocks.FrameReading;
import org.blockframe.blocks.FrameVertical;
import org.blockframe.blocks.StringBlock;
import org.blockframe.blocks.StringBlockBold;
import org.blockframe.blocks.StringBlockBoldItalic;
import org.blockframe.blocks.StringBlockItalic;
import org.blockframe.blocks.TwoColumnFrame;
import org.blockframe.core.Block.PlacedBlock;
import org.blockframe.core.BlockPipe.BlockReader;
import org.blockframe.core.BlockPipe.BlockWriter;
import org.blockframe.core.DebugLog.StringGetter;
import org.blockframe.core.DebugLog.Verbosity;
import org.blockframe.core.PdfChapter.Page;

/**
 * Accepts multiple child {@link Block} instances from a {@link BlockPipe}, and manages their layout and drawing. 
 * <p>
 * If you subclass {@link Frame} and code your own {@linkplain #fill} method, 
 * <ul>
 * <li>
 * You will need to understand reverting pipes. 
 * See the <code>revertXxxx</code> methods and the calls to them in {@link FrameVertical}, {@link FrameHorizontal} and {@link FrameReading}.
 *  <li>
 * Also, the {@linkplain #fill} method should return a {@link PlacedFrame}. 
 * </ul>
 * <p>
 * There are two constructors, one with a {@link BlockPipe} argument, one without. 
 * Which one to use? 
 * <br>
 * The pipe holds the content that will fill this frame. 
 * The client code's calls to {@link #write(Block) put content into the pipe. 
 * <br>
 * If the client code will be writing directly into this frame, use the parameterless constructor. 
 * (That constructor creates a pipe for this frame.)
 * For example, if a {@link FrameReading} will be used for a paragraph of words, use the {@link FrameReading#FrameReading()} constructor. 
 * <br>
 * On the other hand, if the client will be writing to a containing frame which passes its content to this frame, 
 * pass the containing frame's pipe in through the constructor. 
 * For example, the two {@link FrameVertical} instances in the {@link TwoColumnFrame} class are created, passing in the pipe from the {@link TwoColumnFrame}. 
 */
public abstract class Frame extends Block implements Verbosity { 
	
	public final BlockPipe pipe ; 
	protected final BlockWriter writer ; 
	protected final BlockReader reader ; 
	
	private double horizontalGapSize = 0.0 ; 
	private String horizontalGapTemplate = " " ; 
	private Quill horizontalGapQuill = null ; 

	/**
	 * Constructor, for use when this object is an internal frame, 
	 * for example a column within {@link TwoColumnFrame}, 
	 * and the client code writes to an enclosing frame rather than this one. 
	 * @see #Frame()
	 */
	public Frame(BlockPipe pipe) { 
		this.pipe = pipe ; 
		this.writer = pipe.writer ; 
		this.reader = pipe.reader ; 
	}
	
	/**
	 * Constructor, for use when the client code calls this object's {@link #write(Block)} method. 
	 * @see #Frame(BlockPipe)
	 */
	public Frame() { 
		BlockPipe pipe = new BlockPipe(id.str); 
		this.pipe = pipe ; 
		this.writer = pipe.writer ; 
		this.reader = pipe.reader ; 
	}
	
	public void write(Block block) { 
		if (writer==null) throw new RuntimeException("Does not have a writer."); 
		writer.write(block); 
	}
	
	public void write(String text) { 
		write(new StringBlock(text)); 
	}
	
	public void write(String text, Quill.FontStyle style) { 
		if (style==Quill.PLAIN) write(new StringBlock(text)); 
		else if (style==Quill.BOLD) write(new StringBlockBold(text)); 
		else if (style==Quill.ITALIC) write(new StringBlockItalic(text)); 
		else if (style==Quill.BOLD_ITALIC) write(new StringBlockBoldItalic(text)); 
		else write(new StringBlock(text));  ; // Oops. But need to do something. 
	}
	
	// TODO Frame and PdfChapter should have methods to access the last block written. Especially useful for debugging when the block is constructed in the write() arg-list. 
	
	@Override
	public boolean isUnfinishedFrame() { 
		return pipe.reader.hasMore(); 
	}
	
//	/**
//	 * Whether all the blocks in the {@link #pipe} have been placed. 
//	 */
//	@Override
//	public boolean isU() { 
//		return !reader.hasMore(); 
//	}
	
	@Override
	public void draw(Canvas canvas, double left, double top, double width, double height) throws IOException {} 
	
	@Override
	public void noteNewPage(Page page) { 
		pipe.noteNewPage(page);
	}
	
	/**
	 * Sets the template string to use when computing the size of the horizontal gap. 
	 * <p>
	 * If set to <code>null</code>, the current value of {@link #horizontalGapSize} will remain. 
	 * @see #getHorizontalGap(Quill)
	 */
	public void setHorizontalGap(String horizontalGapTemplate) { 
		this.horizontalGapTemplate = horizontalGapTemplate ; 
		this.horizontalGapQuill = null ; 
	}

	/**
	 * Sets the size of the horizontal gap. 
	 * <p>
	 * Sets the template string to <code>null</code> (see {@link #setHorizontalGap(String)}). 
	 * @see #getHorizontalGap(Quill)
	 */
	public void setHorizontalGap(double horizontalGapSize) { 
		this.horizontalGapSize = horizontalGapSize ; 
		this.horizontalGapTemplate = null ; 
	}
	
	/**
	 * Returns the size of the gap to leave between horizontally adjacent children. 
	 * <p>
	 * If {@link #horizontalGapTemplate} is non-null, the size will be the width of that string using the given <code>quill</code>. 
	 * This will be written into {@link #horizontalGapSize}, and returned. 
	 * <p>
	 * Otherwise, the current value of {@link #horizontalGapSize} will be returned. 
	 */
	public double getHorizontalGap(Quill quill) throws IOException { 
		if (horizontalGapTemplate!=null) { 
			if (quill==null) throw new IllegalArgumentException("Argument 'quill' should not be null"); 
			if (quill!=this.horizontalGapQuill) { 
				this.horizontalGapSize = quill.getStringWidth(horizontalGapTemplate); 
				this.horizontalGapQuill = quill ; 
			}
		}
		return horizontalGapSize ; 
	}
	
	/**
	 * Extends the {@link PlacedBlock} with the functionality required for frames. 
	 */
	public class PlacedFrame extends PlacedBlock { 
		
		/**
		 * List of children that have been placed in this {@link PlacedFrame}. 
		 * Note: a single instance of {@linkplain Frame} can have multiple instances of {@linkplain PlacedFrame} to hold all its children. 
		 * (However, the usual case is only one instance, and rarely would there be more than two.)
		 */
		public final ArrayList<PlacedBlock> children = new ArrayList<PlacedBlock>(); 
		
		@Override
		public void draw(Canvas canvas, double left, double top) throws IOException { 
			DebugLog.add(ENTERING_5, this, canvas, logMessage_enteringDraw, left, top, true); 
			Frame.this.draw(canvas, left, top, getWidth(), getHeight()); 
			for (PlacedBlock child : children) { 
				logIfChildOutsideBounds(child); 
				child.draw(canvas, left+child.getLeftInContainer(), top+child.getTopInContainer()); 
			}
			DebugLog.add(LEAVING_6, this, canvas, logMessage_leavingDraw, children.size(), null, false); 
		}

		/**
		 * Reverts the {@link #reader} so all the given blocks are pushed back into the pipe. 
		 * All of the blocks which are pushed back are also reverted, to their reader's state when that child began filling. 
		 * <p>
		 * This is generally called when a frame has read a block (or several) from a reader, measured it and discovered it can't fit it in. 
		 * @param revertingChildren This should match exactly the blocks read from the reader, in the order they were read. 
		 */
		public void revertToFirstChildOfList(ArrayList<PlacedBlock> revertingChildren) {
			PlacedBlock newNextBlock = revertingChildren.get(0);  
			for (int i=revertingChildren.size()-1 ; i>=0 ; i--) { 
				/* Go backwards, coz in any given pipe, the earliest frame should be the one that takes effect. */
				revertingChildren.get(i).revertToStart(); 
			} 
			reader.revertTo(newNextBlock.getBlock());
		}
		
		@Override
		public void revertToStart() { 
			PlacedBlock firstChild = children.size()>0 ? children.get(0) : null ; 
			// TODO Log DETAIL_8 which child we are reverting to. 
			while (children.size()>0) { 
				/* Go backwards, coz in any given pipe, the earliest frame should be the one that takes effect. */
				children.remove(children.size()-1).revertToStart(); 
			}
			if (firstChild!=null) reader.revertTo(firstChild.getBlock()); 
		}
	
		/**
		 * Convenience method, acting on {@link #children}. 
		 */
		public final int size() {
			return children.size();
		}
	
		/**
		 * Convenience method, acting on {@link #children}. 
		 */
		public final void add(PlacedBlock block) {
			children.add(block); 
		}
	
		/**
		 * Convenience method, acting on {@link #children}. 
		 */
		public void addAll(ArrayList<PlacedBlock> blockList) {
			children.addAll(blockList); 
		}
		
	}
	
//	@Override
//	PipeLink makePipeLink() {
//		return new FramePipeLink(this); 
//	}
	
//	/**
//	 * An implementation of {@link PipeLink} that understands when a {@linkplain Frame} needs multiple calls to {@link Block#fill(Quill, Layout)} to consume all the children in its pipe. 
//	 */
//	static class FramePipeLink extends BlockPipe.PipeLink { 
//		
//		private final Frame frame ; 
//		
//		FramePipeLink(Frame frame) { 
//			this.frame = frame ; 
//		}
//		
//		public Block getNextBlockToFill() {
//			if (frame.reader.hasMore()) { 
//				return frame ; 
//			} else { 
//				return nextBlockWritten ; 
//			}
//		}
//		
//		Block getBlock() {  
//			return frame ; 
//		}
//	}	
	
	/**
	 * Similar to {@link Block#logMessage_leavingFill}, except the first message-arg is an integer - how many children. 
	 */
	protected static final StringGetter logMessage_leavingFill = new StringGetter() { 
		public String getString(Block block, PlacedBlock placedBlock, Object countObject, Object ignore1) {
			Integer count = (Integer) countObject ; 
			return "fill() leaving with "+count+" children" ; 
		}
	};

	/**
	 * Similar to {@link Block#logMessage_leavingDraw}, except the first message-arg is an integer - how many children. 
	 */
	protected static final StringGetter logMessage_leavingDraw = new StringGetter() { 
		public String getString(Block block, PlacedBlock placedBlock, Object countObject, Object ignore1) {
			Integer count = (Integer) countObject ; 
			return "draw() leaving with "+count+" children" ;    
		}
	};
	
	/**
	 * Generates log message when a frame decides it can fit a child in its layout. 
	 * Information about the child's size and the remaining layout bounds is included. 
	 */
	protected static final StringGetter logMessage_childAccepted = new StringGetter() {
		public String getString(Block block, PlacedBlock placedBlock, Object childObject, Object eatenLayoutObject) {
			PlacedBlock placedChild = (PlacedBlock) childObject ; 
			Layout eatenLayout = (Layout) eatenLayoutObject ;
			String message = "Child "+placedChild.getBlock().getLogName()+" accepted, has size "+(int)placedChild.getWidth()+"x"+(int)placedChild.getHeight(); 
			if (eatenLayout!=null) message += ", layout remaining size "+(int)eatenLayout.maxWidth+"x"+(int)eatenLayout.maxHeight;
			return message ; 
		}
	};

}
