package org.blockframe.core;

import java.io.File;
import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;

/**
 * Creates a PDF smaller than a page, with the bounds tight around the content. 
 * <p>
 * Useful for generating content to be inserted into GUI word-processors. 
 * It might be necessary to convert to SVG or an image before insertion. <code>LibreOffice Draw</code> does this nicely. 
 */
public class PdfInsert extends PdfChapter {

	protected PdfInsert() {
		super(new PDDocument());
	}
	
	/**
	 * Writes a PDF to the given file. 
	 */
	public void writeFile(File file) throws IOException { 
		makePage();  
		pdDocument.save(file); 
		pdDocument.close(); 
	}

}
