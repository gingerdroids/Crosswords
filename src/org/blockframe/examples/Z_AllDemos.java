package org.blockframe.examples;

import java.io.IOException;

/**
 * Runs all of the example code for BlockFrame. 
 * <p>
 * The PDFs are written into a folder <code>BlockFrame examples</code> in the user's home folder (see {@link UtilsForExamples#getExamplesDir()}. 
 */
public class Z_AllDemos { 
	
	public static void main(String[] args) throws IOException { 
		final String[] emptyStringArray = new String[]{} ;
		
		A_HelloWorld	.main(emptyStringArray); 
		B_Vertical		.main(emptyStringArray); 
		C_VerticalMany	.main(emptyStringArray); 
		D_Reading		.main(emptyStringArray); 
		E_Quill			.main(emptyStringArray); 
		F_Layout		.main(emptyStringArray); 
		G_Table			.main(emptyStringArray); 
		H_PageFrame		.main(emptyStringArray); 
		I_BezierCircles	.main(emptyStringArray); 
		J_Overlays		.main(emptyStringArray); 
	}
	
}

