package org.blockframe.blocks;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import org.blockframe.core.Quill;

public class TextFileFrame extends FrameVertical { 
	
	@Override
	protected Quill inheritQuill(Quill receivedQuill) {
		return super.inheritQuill(receivedQuill).copySize(14.0f);
	}
	
	private Paragraph currentParagraph = null ; 
	
	public void appendFile(File file) throws IOException { 
		FileReader fileReader = new FileReader(file); 
		BufferedReader bufferedReader = new BufferedReader(fileReader); 
		while (true) { 
			String lineText = bufferedReader.readLine(); 
			if (lineText==null) break ; 
			lineText = lineText.trim(); 
			if (lineText.trim().length()>0) { 
				if (currentParagraph==null) openParagraph(); 
				appendTextLine(lineText); 
			} else { 
				closeParagraph(); 
			}
		}
		closeParagraph(); 
		bufferedReader.close(); 
		fileReader.close(); 
	}
	
	public void appendTextLine(String lineText) { 
		String[] lineWords = lineText.split(" "); 
		for (int i=0 ; i<lineWords.length ; i++) { 
			String word = lineWords[i] ; 
			if (word.startsWith("{")) { 
				currentParagraph.appendCurly(word); 
			} else if (word.startsWith("}")) { 
				currentParagraph.appendCurly(word); 
			} else { 
				currentParagraph.appendWord(word); 
			}
		}
	}
	
	public void openParagraph() { 
		if (currentParagraph!=null) closeParagraph(); 
		this.currentParagraph = new Paragraph(); 
		write(currentParagraph); 
	}
	
	public void closeParagraph() { 
		this.currentParagraph = null ; 
		write(new SpacerHeight(true, 0.3)); //  Magic constant, should be outer-class parameter. 
	}
	
	private static class Paragraph extends FrameReading { 
		
		ArrayList<Quill> quillList = new ArrayList<Quill>(); 
		
		Quill currentQuill ; 
		
		ArrayList<CurlyInfo> pendingCurlyList ; 
		
		private Paragraph() { 
		}
		
		@Override
		protected Quill inheritQuill(Quill receivedQuill) { 
			this.currentQuill = receivedQuill ; 
			quillList.add(currentQuill); 
			return currentQuill ; 
		}

		
		private void appendCurly(String curlyText) { 
			if (pendingCurlyList==null) this.pendingCurlyList = new ArrayList<TextFileFrame.CurlyInfo>(); 
			pendingCurlyList.add(new CurlyInfo(curlyText)); 
		}
		
		private void appendWord(String wordText) { 
			write(new FontedBlock(wordText, pendingCurlyList)); 
			this.pendingCurlyList = null ; 
		}
		
		
		
		private class FontedBlock extends StringBlock {
		
			private ArrayList<CurlyInfo> curlyList = new ArrayList<TextFileFrame.CurlyInfo>(); 

			public FontedBlock(String text, ArrayList<CurlyInfo> curlyList) {
				super(text);
				this.curlyList = curlyList ; 
			} 
			
			@Override
			protected Quill inheritQuill(Quill receivedQuill) { 
				if (curlyList!=null) { 
					Quill nextQuill = currentQuill ; 
					for (CurlyInfo curlyInfo : curlyList) { 
						String curlyText = curlyInfo.curlyText ; 
						if (curlyText.startsWith("{")) { 
							for (int i=1 ; i<curlyText.length() ; i++) { 
								char formatCh = curlyText.charAt(i); 
								switch(formatCh) { 
								case 'b': nextQuill = nextQuill.copyBold(); break ; 
								case 'i': nextQuill = nextQuill.copyItalic(); break ; 
								case '+': nextQuill = nextQuill.copySize(currentQuill.getFontSize()*1.25f); break ; 
								case '-': nextQuill = nextQuill.copySize(currentQuill.getFontSize()*0.8f); break ; 
								// TODO Handle unrecognized. 
								}
							}
							quillList.add(nextQuill); 
						} else if (curlyText.startsWith("}")) { 
							int tmpSize = quillList.size();
							quillList.remove(tmpSize-1); 
							if (tmpSize>1) nextQuill = quillList.get(tmpSize-2); 
						} else {
							// TODO Oops, shouldn't happen. 
						}
						
						
					}
					Paragraph.this.currentQuill = nextQuill ; 
				}
				return currentQuill ; 
			}
			
		}
		
	}
	
	private static class CurlyInfo { 
		
		private final String curlyText ; 
		
		CurlyInfo(String curlyText) { 
			this.curlyText = curlyText ; 
		}
	}

}
