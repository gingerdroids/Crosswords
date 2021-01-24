package org.blockframe.examples;

import java.awt.Color;
import java.io.File;
import java.io.IOException;

import org.blockframe.blocks.FrameReading;
import org.blockframe.blocks.SpacerWidth;
import org.blockframe.blocks.StringBlock;
import org.blockframe.blocks.TwoColumnFrame;
import org.blockframe.core.Block;
import org.blockframe.core.BlockPipe;
import org.blockframe.core.DebugLog;
import org.blockframe.core.Frame;
import org.blockframe.core.PdfDocument;
import org.blockframe.core.Quill;


/**
 * We can use a custom {@link Frame} subclass as the top-level frame on a page. 
 * This class creates a frame which divides its content into columns. 
 */
public class H_PageFrame extends PdfDocument { 

	public static void main(String[] args) throws IOException { 
		new H_PageFrame(); 
	}
	
	protected H_PageFrame() throws IOException {
		
		/*
		 * Write several junk paragraphs, which will more than fill a page. 
		 */
		for (int i=0 ; i<8 ; i++) { 
			write(new ParagraphFrame()); 
		}

		File file = new File(UtilsForExamples.getExamplesDir(), getClass().getSimpleName()+".pdf"); 
		writeFile(file); 
		DebugLog.out(); 
	}
	
	/**
	 * Customizes the frame holding the top-level content of each page. 
	 */
	@Override
	public Frame newPageFrame(BlockPipe pipe, Page prevPage) { 
		// jan21 TwoColumnFrame is now subsumed by MultiColumnFrame. 
		return new TwoColumnFrame(pipe); // Don't forget to pass 'pipe' to the constructor. 
	}
	
	/**
	 * The default {@link Quill} is modified to use colour {@link Color#BLUE}. 
	 */
	@Override
	public Quill newPageQuill(Page prevPage) {
		return super.newPageQuill(prevPage).copy(Color.BLUE);
	}
	
	private class ParagraphFrame extends FrameReading { 
		ParagraphFrame() { 
			write(new SpacerWidth("W")); // Indent at start of paragraph
			for (int i=0 ; i<100 ; i++) { 
				Block block = new StringBlock(UtilsForExamples.number(i+1)); 
				write(block); 
			}
		}
	}

}
