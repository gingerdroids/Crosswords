package org.blockframe.blocks;

import java.io.IOException;
import java.util.ArrayList;

import org.blockframe.core.Block;
import org.blockframe.core.BlockPipe;
import org.blockframe.core.DebugLog;
import org.blockframe.core.Frame;
import org.blockframe.core.Layout;
import org.blockframe.core.Quill;
import org.blockframe.core.Block.PlacedBlock;
import org.blockframe.core.DebugLog.StringGetter;
import org.blockframe.core.Layout.Alignment;
import org.blockframe.core.Layout.Justification;

import com.gingerdroids.utils_java.Str;
import com.gingerdroids.utils_java.Util;


/**
 * A {@link Frame} subclass that lays out its children in reading order: left-to-right in a row, then beginning another row just below, and so forth. 
 */
public class FrameReading extends Frame {

		public FrameReading() {}

		public FrameReading(BlockPipe pipe) {
			super(pipe); 
		}
		
		public void writeTextSplittable(String fullText) { 
			String [] words = fullText.split(" "); 
			for (String word : words) write(new StringBlock(word)); 
		}
		
		private double firstLineIndent = 0 ; 
		
		private double otherLineIndent = 0 ; 

		/**
		 * Sets the left indent, for all lines. 
		 */
		public FrameReading setLeftIndent(double leftIndent) {
			setFirstLineIndent(leftIndent); 
			setOtherLineIndent(leftIndent); 
			return this ; 
		}

		/**
		 * Sets the left indent, for the first line. 
		 */
		public FrameReading setFirstLineIndent(double firstLineIndent) {
			this.firstLineIndent = firstLineIndent; 
			return this ; 
		}

		/**
		 * Sets the left indent, for the second and subsequent lines. 
		 */
		public FrameReading setOtherLineIndent(double otherLineIndent) {
			this.otherLineIndent = otherLineIndent; 
			return this ; 
		}

		@Override
		public PlacedBlock fill(Quill receivedQuill, Layout receivedLayout) throws IOException { 
			DebugLog.add(ENTERING_5, this, null, logMessage_enteringFill, null, null, true); 
			this.quill = inheritQuill(receivedQuill) ; 
			Layout frameLayout = inheritLayout(receivedLayout); 
			DebugLog.add(ALL_9, this, null, pipe.logMessage_pipe, null, null, false); 
			DebugLog.add(DETAIL_8, this, null, Layout.logMessage_layout, frameLayout, null, false); 
			double maxWidth = frameLayout.maxWidth ; 
			PlacedFrame placedFrame = this.new PlacedFrame(); 
			//////  Measure children and allocate them to lines. 
			ArrayList<FrameReading.Line> lines = new ArrayList<FrameReading.Line>(); 
			double sumHeight ; 
			{
				/* Don't position them on the lines yet, until we know which is the last line. Affects last line of a fully-justified section. */ 
				double nextTop = 0 ; 
				Layout eatenLayout = frameLayout.copy(); // Copy of layout, whose max-height and max-width is eaten away as we add blocks. 
				boolean isFirstLine = true ; 
				eatenLayout.reduceWidth(getIndent(isFirstLine)); 
				while(reader.hasMore()) { 
					/* Each iteration processes one line. */
					FrameReading.Line line = fillLine(placedFrame, eatenLayout, nextTop, isFirstLine); // Measures children, places as many as possible on the line. 
					if (!line.canFitLineInHeight) { 
						DebugLog.add(ALL_9, this, null, logMessage_lineRejected, line, null, false); 
						break ; 
					}
					if (line.childrenOnLine.size()==0) { 
						DebugLog.add(ALL_9, this, "No children on line.", false); 
						break ; 
					}
					placedFrame.addAll(line.childrenOnLine); 
					eatenLayout.setSize(frameLayout.maxWidth-line.indent, null); 
					eatenLayout.reduceHeight(line.lineHeight); 
					//////  Store line-info for positioning loop 
					lines.add(line); 
					//////  Prepare for next line 
					nextTop += line.lineHeight ; 
					isFirstLine = false ; 
				}
				sumHeight = nextTop ; 
				int lineCount = lines.size();
				if (lineCount>0 && !reader.hasMore()) lines.get(lineCount-1).isLast = true ; 
			}
			//////  Calculate my dimensions
			double frameWidth ; 
			if (frameLayout.isWidthTight) { 
				frameWidth = 0 ; 
				for (FrameReading.Line line : lines) { 
					if (line.sumWidth>frameWidth) frameWidth = line.sumWidth ; 
				}
			} else { 
				frameWidth = maxWidth;
			}
			double frameHeight = frameLayout.isHeightTight ? sumHeight : frameLayout.maxHeight ; 
			//////  Position the children on each line
			{ 
				Justification justification = frameLayout.justification ; 
				Alignment alignment = frameLayout.alignment ; 
				boolean isFirstLine = true ; 
				for (FrameReading.Line line : lines) { 
					//////  Position the children on the line, remembering justification. 
					double spareSpace = frameWidth - line.sumWidth - line.indent ; 
					int lineChildCount = line.childrenOnLine.size(); 
					//// Compute the gap between words
					double gap = getHorizontalGap(quill) + ((justification==Layout.FULL && !line.isLast && lineChildCount>0) ? spareSpace / (lineChildCount-1) : 0) ; // Horizontal gap between words. 
					double nextLeft = line.indent ; 
					if(justification==Layout.FULL) { 
						nextLeft += 0 ; 
					} else if (justification==Layout.CENTRE_H) { 
						nextLeft += spareSpace / 2 ; 
					} else if (justification==Layout.RIGHT) { 
						nextLeft += spareSpace ; 
					} else { 
						nextLeft += 0 ; 
					}
					//// Compute how much vertical gap to use above each word
					double dropFactor ; 
					if (alignment==Layout.TOP) { 
						dropFactor = 0 ; 
					} else if (alignment==Layout.CENTRE_V) { 
						dropFactor = 0.5 ; 
					} else { 
						dropFactor = 1 ; 
					}
					//// Do It - position the children 
					for (PlacedBlock placedChild : line.childrenOnLine) { 
						double childTop = (line.lineHeight-placedChild.getHeight()) * dropFactor ; 
						placedChild.setOffsetInContainer(nextLeft, line.lineTop+childTop); 
						nextLeft += placedChild.getWidth() + gap ; 
					}
				}
			}
			//////  Set my dimensions
			placedFrame.setDimensions(frameWidth, frameHeight); 
			DebugLog.add(LEAVING_6, placedFrame, null, logMessage_leavingFill, placedFrame.size(), null, false); 
			return placedFrame ; 
		}
		
		private double getIndent(boolean isFirstLine) { 
			return isFirstLine ? firstLineIndent : otherLineIndent ; 
		}

		/**
		 * Generates log message when the {@link #fillLine(PlacedFrame, Layout, double, boolean)} method is entered. 
		 */
		protected static final StringGetter logMessage_enteringLineFill = new StringGetter() { 
			public String getString(Block block, PlacedBlock placedBlock, Object eatenLayoutObject, Object ignore1) {
				Layout eatenLayout = (Layout) eatenLayoutObject;
				return "fillLine() entering, remaining height "+eatenLayout.maxHeight ; 
			}
		};

		private FrameReading.Line fillLine(PlacedFrame placedFrame, Layout eatenLayout, double nextTop, boolean isFirstLine) throws IOException {
			DebugLog.add(DETAIL_8, this, null, logMessage_enteringFill, eatenLayout, null, true); 
			DebugLog.add(ALL_9, this, null, pipe.logMessage_pipe, null, null, false); 
			FrameReading.Line line = new Line(); 
			line.isFirst = isFirstLine ; 
			line.indent = getIndent(isFirstLine); 
			double horizontalGapSize = getHorizontalGap(quill);
			ArrayList<PlacedBlock> childrenOnLine = new ArrayList<PlacedBlock>(); 
			//////  Get children that fit on line, and measure them
			double sumWidth = 0 ; 
			double maxHeightOnLine = 0 ; 
			line.canFitLineInHeight = true ; // Whether this line pushes us past the maximum height. 
			while(reader.hasMore()) { 
				Block child = reader.read(); 
				//// Skip leading space
				boolean isLineEmpty = childrenOnLine.isEmpty();
				boolean isChildSpace = child instanceof SpacerWidth;
				if (isLineEmpty && isChildSpace) continue ; 
				//// Measure child
				PlacedBlock placedChild = child.fill(quill, eatenLayout); 
				double childHeight = placedChild.getHeight() ; 
				double childWidth = placedChild.getWidth();
				double addedWidth = childWidth ; // Will include child and gap. 
				if (sumWidth>0) addedWidth += horizontalGapSize ; 
				boolean isChildMeasuredTooWide = childWidth>eatenLayout.maxWidth;
				boolean isChildMeasuredTooHigh = childHeight>eatenLayout.maxHeight;
				boolean isChildNotCompletelyFilled = child.isUnfinishedFrame();
				boolean isLinePartiallyFilled = sumWidth>0;
				//// Too big for line?
				if ((isChildMeasuredTooWide || isChildNotCompletelyFilled) && isLinePartiallyFilled) { 
					// TODO Frame field boolean shouldAcceptWideBlock - controlling whether a single block that is too wide is accepted (messy), or rejected (infinite loop danger). 
					/*
					 * We are rejecting one child, which hasn't yet been added to 'children'. 
					 * We should revert the child to its start, and push it back onto the feed so the next frame will receive it. 
					 * Note that the child will be read again immediately, at the beginning of the next line. Its layout will have slightly different bounds. 
					 */
					placedChild.revertToStart(); 
					reader.revertTo(child); 
					break ; // Reject out-of-bounds, unless it's the first on the line. 
				}
				//// Too many lines in frame? 
				if (eatenLayout.allowSplitting && isChildMeasuredTooHigh) { 
					// TODO Frame field boolean shouldAcceptHighBlock - controlling whether a single block that is too tall is accepted (messy), or rejected (infinite loop danger). 
					line.canFitLineInHeight = false ; 
					/*
					 * We are rejecting perhaps several children (everything on this line so far), and the current child. 
					 * All blocks on this line should be reverted to their start, and the source moved back to the first one on the line. 
					 * None of these blocks have been added to the 'children' field, just the 'childrenOnLine' variable. 
					 */
					DebugLog.add(DETAIL_8, this, null, "Children rejected because the line would exceed frame height.", true); 
					childrenOnLine.add(placedChild); // Immediately removed, but 'childrenOnLine' list must not be empty in revert (mar18). 
					placedFrame.revertToFirstChildOfList(childrenOnLine); 
					break ; 
				}
				if (eatenLayout.allowSplitting && isChildNotCompletelyFilled) {  
					break ; 
				}
				// TODO BUG? If allowSplitting==false, but the child is not completely filled, I think we'll have an infinite loop. 
				//// Accept child 
				if (childHeight>maxHeightOnLine) maxHeightOnLine = childHeight ; 
				childrenOnLine.add(placedChild); 
				eatenLayout.reduceWidth(horizontalGapSize+childWidth); 
				sumWidth += addedWidth ; 
				DebugLog.add(ALL_9, this, null, logMessage_childAccepted, placedChild, eatenLayout, false); 
			}
			//////  Bye bye
			line.sumWidth = sumWidth ; 
			line.childrenOnLine = childrenOnLine ; 
			line.lineTop = nextTop ; 
			line.lineHeight = maxHeightOnLine ; 
			return line;
		}

		/**
		 * Information about the children on a single line. 
		 * This is internal to this class: it is not passed outside, and is used only in the fill-pass. 
		 */
		private static class Line { 
			ArrayList<PlacedBlock> childrenOnLine; 
			double sumWidth ;
			double lineTop ; 
			double lineHeight ; 
			public boolean canFitLineInHeight;
			boolean isFirst = false ; 
			boolean isLast = false ; 
			double indent = 0.0 ; 
		}
		
		/**
		 * Generates log message when a line is too tall for the remaining space. 
		 */
		protected static final StringGetter logMessage_lineRejected = new StringGetter() { // TODO Test this 
			public String getString(Block block, PlacedBlock placedBlock, Object lineObject, Object dummy) {
				Line line = (Line) lineObject ; 
				String message = "Line rejected, "+line.childrenOnLine.size()+" children reverted" ; 
				return message ; 
			}
		};
	}