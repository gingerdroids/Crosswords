package org.blockframe.blocks;

import org.blockframe.core.Quill;

/**
 * Subclass of {@link StringBlock} which forces bold font. 
 */
public class StringBlockBoldItalic extends StringBlock {
	public StringBlockBoldItalic(String text) {
		super(text);
	} 
	@Override
	protected Quill inheritQuill(Quill receivedQuill) {
		return super.inheritQuill(receivedQuill).copyBoldItalic(); 
	}
}