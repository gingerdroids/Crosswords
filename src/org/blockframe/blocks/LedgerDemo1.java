package org.blockframe.blocks;

import java.io.File;
import java.io.IOException;

import org.blockframe.core.DebugLog;
import org.blockframe.core.Layout;
import org.blockframe.core.PdfDocument;
import org.blockframe.core.Quill;

import com.gingerdroids.utils_java.Util;

public class LedgerDemo1 extends PdfDocument {
	
	public static void main(String[] args) throws IOException { 
		new LedgerDemo1(); 
	}
	
	LedgerDemo1() throws IOException { 
		File pdfFile = new File(Util.getDesktopSubdirectory(null), "ZZ.pdf"); 
		
		write(new StringBlock("Line 1")); 
		
		LedgerFrame frame = new DemoLedgerFrame()
				.addColumn("First", false, Layout.LEFT, true)
				.addColumn("Second", false, Layout.CENTRE_H, true) 
				.addColumn("Third", false, Layout.RIGHT, true) 
				.setColumnAlignment(1, Layout.CENTRE_V)
				.setColumnAlignment(2, Layout.BOTTOM)
				; 
		frame.writeRow("111 111 111", "222 222 222", "333 333 333");
		frame.writeRow("One", "Two", "Three");
		frame.writeRow("Un", "Deux", "Trois");
		frame.writeRow("Ein", "Zwei", "Drei");
		write(frame); 
		writeFile(pdfFile); 
		DebugLog.out();
	}
	
	private class DemoLedgerFrame extends LedgerFrame { 
		
		@Override
		protected Quill inheritColumnQuill(Quill receivedQuill, int columnIndex) { 
			if (columnIndex==2) return receivedQuill.copy().multiplySize(1.5); 
			return receivedQuill ; 
		}
		
	}

}
