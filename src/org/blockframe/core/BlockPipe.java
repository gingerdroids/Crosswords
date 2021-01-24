package org.blockframe.core;

import org.blockframe.core.Block.NovLink;
import org.blockframe.core.Block.PlacedBlock;
import org.blockframe.core.DebugLog.StringGetter;
import org.blockframe.core.DebugLog.Verbosity;
import org.blockframe.core.PdfChapter.Page;

/**
 * Holds a pipeline of blocks for a container-block, usually a {@link Frame}, to layout and display. 
 * <p>
 * Although pipes are the main tool for passing content from the client to BlockFrame, they are fairly invisible to the client code. 
 * When the client code calls {@link PdfChapter#write(Block)} or {@link Frame#write(Block)}, the block ends up being added to a pipe. 
 * <p>
 * Blocks are pulled from a pipe with the {@link BlockReader#read()} method, usually in a frame's {@link Block#fill(Quill, Layout)} method. 
 */
public class BlockPipe implements Verbosity { 

	public final BlockWriter writer = new BlockWriter(); 
	
	public final BlockReader reader = new BlockReader();  
	
	public final String id ; 
	
//	/**
//	 * The most recent link <em>added</em> to the pipe. 
//	 */
//	private BlockPipe.PipeLink lastLinkAdded = null ; 
	
	/**
	 * The most recent link <em>added</em> to the pipe. 
	 */
	private Block.NovLink endLink = null ; 
	
	private Block.NovLink currentLink = null ; 
	
	private Block.NovLink nextLink = null ; 
	
//	/**
//	 * Provides the next block in the pipe. 
//	 * <p>
//	 * Note: the next block might not be known until after {@link Block#fill(Quill, Layout)} on the current block has finished. 
//	 * The next block may be a continuation of the current block.
//	 * For example, if a paragraph is split between two pages. 
//	 */
//	private NextGetter_interface nextGetter = null ; 
	
	public BlockPipe(String id) { 
		this.id = id ; 
	}
	
	/**
	 * Calls {@link Block#noteNewPage(Page) in the next {@linkplain Block} (or the current one, 
	 * if it's an unfinished {@linkplain Frame}). 
	 * Where the {@linkplain Frame} has children, the first is recursively called. 
	 */
	void noteNewPage(Page page) { 
		NovLink unfinished = reader.getUnfinished();
		if (unfinished==null) return ; 
		unfinished.block.noteNewPage(page);
	}
	
	public void prepend(Block block) { 
		NovLink newLink = block.getLink(this); 
		NovLink oldCurrent = currentLink ; 
		NovLink oldNext = nextLink ; 
		if (oldCurrent!=null) { 
			newLink.setNextLink(oldCurrent.block);
			this.currentLink = null ; 
			this.nextLink = oldCurrent ; 
		} else { 
			newLink.setNextLink(oldNext.block);
			this.currentLink = null ; 
			this.nextLink = newLink ; 
		}
	}
	
	/**
	 * Returns the front block in the pipe. 
	 * @return null if there are no blocks in the pipe. 
	 */
	public Block peek() { 
		if (currentLink!=null) { 
			return currentLink.block ; 
		} else if (nextLink!=null) { 
			return nextLink.block ; 
		} else { 
			return null ; 
		}
	}

	/**
	 * An interface into {@link BlockPipe} for writing blocks to the end of the pipe. 
	 */
	public final class BlockWriter { 

		private boolean isClosed = false ; 
		
		public void write(Block block) { 
			if (isClosed) DebugLog.add(WARNING_4, block, null, "Writing block to a closed pipe. Block is dropped.", true); // TODO BUG? This should return without writing. 
			if (currentLink!=null) throw new RuntimeException("Didn't expect reading to have begun here."); 
			if (endLink!=null) { 
				/* Here we know: The pipe has something in it, and so a last item. */
				endLink.setNextLink(block); 
				BlockPipe.this.endLink = endLink.getNextLink(); 
			} else { 
				/* Here we know: Empty pipe. This is the first item. */
				NovLink newLink = block.getLink(BlockPipe.this) ; 
				BlockPipe.this.endLink = newLink ; 
				BlockPipe.this.nextLink = newLink ; 
			}
//			BlockPipe pipe = BlockPipe.this;
//			if (lastLinkAdded!=null) { 
//				BlockPipe.PipeLink oldLast = lastLinkAdded ; 
//				oldLast.setNext(block); 
//				pipe.lastLinkAdded = block.pipeLink ; 
//			} else { 
//				/* Adding first block. */ 
//				pipe.lastLinkAdded = block.pipeLink ; 
//				pipe.nextGetter = new NextGetter(block); 
//			}
		}

		public void close() {
			this.isClosed = true ; 
		}
	}

	/**
	 * An interface into {@link BlockPipe} for reading the next block from the pipe. 
	 */
	public final class BlockReader { 
		
//		private Block peekNextBlock() { // TO DO::::::: RECODE 
////			if (nextGetter==null) return null ; 
////			Block nextBlock = nextGetter.getNextBlockToFill(); 
////			return nextBlock ; 
//			throw new RuntimeException(); 
//		}

		/**
		 * Whether there are more blocks in the pipe. 
		 * If this returns <code>false</code>, all subsequent calls to {@link #read()} will return <code>null</code>. 
		 */
		public boolean hasMore() { 
			if (nextLink!=null) return true ; 
			if (currentLink!=null && currentLink.block.isUnfinishedFrame()) return true ; 
//			Block nextBlock = peekNextBlock(); 
//			if (nextBlock!=null) return true ; 
			writer.close(); // Don't allow writing after we've told a reader there are no more. 
			return false ; 
		}
		
		public NovLink getUnfinished() { 
			if (currentLink!=null && currentLink.block.isUnfinishedFrame()) { 
				return currentLink ; 
			} else { 
				return nextLink ; 
			}
		}

		public Block read() { 
			if (currentLink!=null && currentLink.block.isUnfinishedFrame()) { 
				return currentLink.block ; 
			}
			NovLink oldNextLink = nextLink ; 
			NovLink newCurrentLink = oldNextLink ; 
			NovLink newNextLink = newCurrentLink!=null ? newCurrentLink.getNextLink() : null ; 
			BlockPipe.this.currentLink = newCurrentLink ; 
			BlockPipe.this.nextLink = newNextLink ; 
			Block resultBlock = currentLink!=null ? currentLink.block : null ; 
			if (resultBlock==null) writer.close(); // Don't allow writing after we've told a reader there are no more. 
			return resultBlock ; 
		}
		
		/**
		 * Reverts the pipe so the given block is the next block to be read. 
		 */
		public void revertTo(Block block) { 
			BlockPipe.this.currentLink = null ; 
			BlockPipe.this.nextLink = block.getLink(); 
		}
		
	}
	
	/**
	 * Logs the first few blocks in the pipe. 
	 * The message-arguments may be <code>null</code>. 
	 */
	public final StringGetter logMessage_pipe = new StringGetter() { 
		public String getString(Block block, PlacedBlock placedBlock, Object ignore0, Object ignore1) {
			StringBuffer sb = new StringBuffer(); 
			sb.append(BlockPipe.this.getClass().getSimpleName()); 
			sb.append("-"); 
			sb.append(BlockPipe.this.id); 
			boolean isEmpty = true ; 
			if (currentLink!=null && currentLink.block.isUnfinishedFrame()) { 
				isEmpty = false ; 
				sb.append(", current frame is "+currentLink.block.getLogName()); 
			}
			NovLink tmpLink = nextLink ; 
			if (tmpLink!=null) { 
				isEmpty = false ; 
				sb.append(", next blocks are"); 
				for (int i=0 ; i<3 && tmpLink!=null ; i++) { 
					sb.append("  "); 
					sb.append(tmpLink.block.getLogName()); 
					tmpLink = tmpLink.getNextLink(); 
				}
				if (tmpLink!=null) { 
					sb.append("  ..."); 
				}
			} 
			if (isEmpty){ 
				sb.append(" is empty"); 
			}
			return sb.toString();
		}
	};
	
//	/**
//	 * Links the blocks in a {@link BlockPipe}. 
//	 * Every {@link Block} has a unique instance associated with it. 
//	 * @see Block#pipeLink
//	 */
//	abstract static class PipeLink implements NextGetter_interface { 
//	
//		/**
//		 * The next block which was written to the {@linkplain BlockPipe}. 
//		 */
//		Block nextBlockWritten = null ; 
//	
//		void setNext(Block nextBlock) { 
//			if (this.nextBlockWritten!=null) DebugLog.add(Verbosity.WARNING_4, nextBlock, null, "Block being linked into a pipe more than once.", true); 
//			this.nextBlockWritten = nextBlock ; 
//		}
//	
//		/**
//		 * Typically, returns the {@link Block} associated with this link. See {@link Block#pipeLink}. 
//		 * <p>
//		 * Note: this method should not be called on subclass {@link NextGetter}. 
//		 */
//		abstract Block getBlock(); 
//		
//		/**
//		 * Returns the next block in the pipe, regardless of whether the current block has finished processing. 
//		 * This is useful for debugging. 
//		 */
//		Block dbgGetNext() {
//			return nextBlockWritten ;
//		} 
//	}
	
//	/**
//	 * Provides method {@link #getNextBlockToFill()}. 
//	 */
//	interface NextGetter_interface { 
//		/**
//		 * Returns the next block that should be processed. 
//		 * Usually, this will be the next block that was written to the pipe. 
//		 * However, this can vary - see {@link Frame#FramePipeLink}. 
//		 * <p>
//		 * Warning: this method should not be called until the next block is known. 
//		 * Specifically, until the {@link Block#fill(Quill, Layout)} method has completed in the block associated with this link. 
//		 * <p>
//		 * This method does not advance the pipe. An immediately subsequent call will return the same value. 
//		 */
//		Block getNextBlockToFill(); 
//	}

//	/**
//	 * Holder for a next-block, used when we don't have a previous block to link from. 
//	 * This happens for the first block in a pipe, and when reverting the pipe. 
//	 */
//	static class NextGetter implements NextGetter_interface { 
//		
//		Block nextBlock = null ; 
//		
//		NextGetter(Block nextBlock) { 
//			this.nextBlock = nextBlock ; 
//		}
//		
//		public Block getNextBlockToFill() {
//			return nextBlock ;
//		}
//	}

}
