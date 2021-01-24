package org.blockframe.blocks;

import java.io.IOException;

import org.blockframe.core.Block;
import org.blockframe.core.Canvas;
import org.blockframe.core.Layout;
import org.blockframe.core.Quill;

/**
 * Whitespace, taking the full height available in its {@link Layout}. 
 * It has small but non-zero width. 
 */
public final class SpacerFullHeight extends Block {
	@Override
	public PlacedBlock fill(Quill receivedQuill, Layout receivedLayout) throws IOException {
		this.quill = inheritQuill(receivedQuill) ; 
		PlacedBlock placedBlock = new PlacedBlock(); 
		placedBlock.setDimensions(Double.MIN_VALUE, receivedLayout.maxHeight); // Non-zero width, so it won't trigger warnings. 
		return placedBlock ; 
	}
	@Override
	public void draw(Canvas canvas, double left, double top, double width, double height) throws IOException {} 
}