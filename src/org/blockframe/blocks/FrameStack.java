package org.blockframe.blocks;

import java.io.IOException;

import org.blockframe.core.Block;
import org.blockframe.core.BlockPipe;
import org.blockframe.core.DebugLog;
import org.blockframe.core.Frame;
import org.blockframe.core.Layout;
import org.blockframe.core.Quill;
import org.blockframe.core.Block.PlacedBlock;
import org.blockframe.core.Frame.PlacedFrame;
import org.blockframe.core.Layout.Alignment;
import org.blockframe.core.Layout.Justification;

/**
 * A {@link Frame} subclass that lays out its children over the top of each other. 
 * <p>
 * This is useful for drawing diagrams, where many blocks will need to sit in the same rectangular area. 
 */
public class FrameStack extends Frame { 
	
	public FrameStack() {}
	
	public FrameStack(BlockPipe pipe) {
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
		//////  Process children
		while (reader.hasMore()) { 
			Block child = reader.read(); 
			PlacedBlock placedChild = child.fill(quill, frameLayout); 
			placedFrame.add(placedChild); 
			DebugLog.add(ALL_9, this, null, logMessage_childAccepted, placedChild, frameLayout, false); 
			/* We don't check child.isFillComplete(). If a child doesn't fit all its children, they are quietly dropped.  */
		}
		//////  Position children in this frame
		for (PlacedBlock child : placedFrame.children) { 
			child.setOffsetInContainer(0, 0); 
		}
		//////  Set my dimensions
		double frameWidth = frameLayout.maxWidth ; 
		double frameHeight = frameLayout.maxHeight ; 
		int childCount = placedFrame.size(); 
		placedFrame.setDimensions(frameWidth, frameHeight); 
		DebugLog.add(LEAVING_6, placedFrame, null, logMessage_leavingFill, childCount, null, false); 
		return placedFrame ; 
	}

}
