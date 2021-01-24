package org.blockframe.blocks;

import org.blockframe.core.Quill;

/**
 * Subclass of {@link StringBlock} which forces italic font. 
 */
public class StringBlockItalic extends StringBlock {
	public StringBlockItalic(String text) {
		super(text);
	} 
	@Override
	protected Quill inheritQuill(Quill receivedQuill) {
		return super.inheritQuill(receivedQuill).copyItalic(); 
	}
}