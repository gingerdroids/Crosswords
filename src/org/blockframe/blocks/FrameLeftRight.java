package org.blockframe.blocks;

import java.io.IOException;

import org.blockframe.core.Block;
import org.blockframe.core.BlockPipe;
import org.blockframe.core.Canvas;
import org.blockframe.core.DebugLog;
import org.blockframe.core.DebugLog.Verbosity;
import org.blockframe.core.Frame;
import org.blockframe.core.Layout;
import org.blockframe.core.Quill;
import org.blockframe.core.Layout.Alignment;
import org.blockframe.core.Layout.Justification;

/**
 * A {@link Frame} subclass that expects two children, the first to be left justified, the second right justified. 
 */
public class FrameLeftRight extends Frame { 
		
		public FrameLeftRight() {
		}
		
		public FrameLeftRight(BlockPipe pipe) {
			super(pipe); 
		}

		@Override
		public PlacedBlock fill(Quill receivedQuill, Layout receivedLayout) throws IOException { 
			DebugLog.add(ENTERING_5, this, null, logMessage_enteringFill, null, null, true); 
			this.quill = inheritQuill(receivedQuill) ; 
			Layout frameLayout = inheritLayout(receivedLayout); 
			DebugLog.add(ALL_9, this, null, pipe.logMessage_pipe, null, null, false); 
			DebugLog.add(DETAIL_8, this, null, Layout.logMessage_layout, frameLayout, null, false); 
			PlacedFrame placedFrame = this.new PlacedFrame(); 
			//////  Measure children (two expected) and place them
			double frameWidth = receivedLayout.maxWidth;
			Block firstChild = reader.read(); 
			PlacedBlock firstPlaced = firstChild.fill(quill, receivedLayout); 
			double firstWidth = firstPlaced.getWidth();
			double firstHeight = firstPlaced.getHeight();
			placedFrame.add(firstPlaced);
			firstPlaced.setOffsetInContainer(0, 0); 
			Block secondChild = reader.read(); 
			if (secondChild==null) throw new RuntimeException("FrameLeftRight missing second child."); 
			PlacedBlock secondPlaced = secondChild.fill(quill, receivedLayout); 
			double secondWidth = secondPlaced.getWidth();
			double secondHeight = secondPlaced.getHeight();
			placedFrame.add(secondPlaced);
			secondPlaced.setOffsetInContainer(frameWidth-secondWidth, 0); 
			if (reader.hasMore()) throw new RuntimeException("Should only have two children"); 
			//////  Set my dimensions
			double maxChildHeight = Math.max(firstHeight, secondHeight); 
			placedFrame.setDimensions(frameWidth, maxChildHeight); 
			DebugLog.add(LEAVING_6, placedFrame, null, logMessage_leavingFill, 2, null, false); 
			return placedFrame ; 
		}

	}