package org.blockframe.examples;

import java.awt.Color;
import java.io.File;
import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.blockframe.blocks.FrameReading;
import org.blockframe.blocks.FrameStack;
import org.blockframe.blocks.FrameVertical;
import org.blockframe.blocks.SpacerFullHeight;
import org.blockframe.blocks.SpacerHeight;
import org.blockframe.blocks.StringBlock;
import org.blockframe.core.Block;
import org.blockframe.core.BlockPipe;
import org.blockframe.core.Canvas;
import org.blockframe.core.DebugLog;
import org.blockframe.core.Frame;
import org.blockframe.core.Layout;
import org.blockframe.core.PdfDocument;
import org.blockframe.core.Quill;
import org.blockframe.core.Layout.Justification;
import org.blockframe.painters.Scribe;

/**
 * This class demonstrates how to draw several blocks in the same space. 
 * This is useful for figures.  
 */
public class K_Stack extends PdfDocument { 

	public static void main(String[] args) throws IOException { 
		new K_Stack(); 
	}
	
	class WordBlock extends Block { 
		final String word ; 
		final double across ;  
		final double up ; 
		WordBlock(String word, double across, double up) { 
			this.word = word ; 
			this.across = across ; 
			this.up = up ; 
		}
		
		@Override
		public PlacedBlock fill(Quill quill, Layout receivedLayout) throws IOException { 
			this.quill = quill ; 
			PlacedBlock placedBlock = new PlacedBlock(); 
			placedBlock.setDimensions(receivedLayout.maxWidth, receivedLayout.maxHeight); 
			return placedBlock ; 
		}
		
		@Override
		public void draw(Canvas canvas, double left, double top, double width, double height) throws IOException {
			System.out.println("Drawing word '"+word+"' at "+left+","+top+" "+width+"x"+height); 
			float pdfBottom = canvas.getPdfBottom(top, height);
			Scribe.string_lb(canvas, quill, (float)(left+across), (float)(pdfBottom+up), word);
		}
	}
	
	class FigureFrame extends FrameStack { 
		
		public final double figureWidth ; 
		
		public final double figureHeight ; 
		
		FigureFrame(double figureWidth, double figureHeight) { 
			this.figureWidth = figureWidth ; 
			this.figureHeight = figureHeight ; 
		}
		
		@Override
		public void draw(Canvas canvas, double left, double top, double width, double height) throws IOException {
			super.draw(canvas, left, top, width, height);
			Scribe.border(canvas, Color.RED, left, top, width, height, null, null); 
			
			
			PDPageContentStream stream = canvas.stream;
			float pdfLeft = (float) left ; 
			float pdfWidth = (float) width ; 
			float pdfHeight = (float) height ;
			float pdfTop = canvas.getPdfY(top); 
			float pdfBottom = canvas.getPdfY(top+pdfHeight); 
			float midX = pdfLeft + pdfWidth * 0.5f ; 
			float midY = pdfBottom + pdfHeight * 0.5f ; 
			float pdfThickness = 1.0f ; 
			stream.setLineWidth(pdfThickness); 
			
			{
				stream.moveTo(pdfLeft+lowCoord, pdfBottom+midCoord); 
				stream.lineTo(pdfLeft+midCoord, pdfBottom+highCoord); 
				stream.lineTo(pdfLeft+highCoord, pdfBottom+midCoord); 
				stream.lineTo(pdfLeft+midCoord, pdfBottom+lowCoord); 
				stream.lineTo(pdfLeft+lowCoord, pdfBottom+midCoord); 
				stream.stroke(); 
			}
		}
		
		@Override
		protected Layout inheritLayout(Layout receivedLayout) {
			return super.inheritLayout(receivedLayout).copy().setSize(figureWidth, figureHeight);
		}
		
	}
	final int figureSize = 180 ; 
	final int midCoord = figureSize / 2 ; 
	final int lowCoord = figureSize / 4 ; 
	final int highCoord = 3 * figureSize / 4 ; 
	
	protected K_Stack() throws IOException { 
		//////  Build the figure 
		FrameStack figure = new FigureFrame(180, 180); 
		figure.write(new WordBlock("left", lowCoord, midCoord)); 
		figure.write(new WordBlock("top", midCoord, lowCoord)); 
		figure.write(new WordBlock("right", highCoord, midCoord)); 
		figure.write(new WordBlock("bottom", midCoord, highCoord)); 
		//////  Write sentences and figure into document. 
		write(new StringBlock("This sentence is before the figure.")); 
		write(figure); 
		write(new StringBlock("This sentence is after the figure.")); 
		
		File file = new File(UtilsForExamples.getExamplesDir(), getClass().getSimpleName()+".pdf"); 
		writeFile(file); 
		DebugLog.out(); 
	}
	
}
