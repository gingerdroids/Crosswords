package org.blockframe.blocks;

import java.io.IOException;

import org.blockframe.core.Block;
import org.blockframe.core.Canvas;
import org.blockframe.core.Layout;
import org.blockframe.core.Quill;


/**
 * Whitespace, taking the full width available in its {@link Layout}. 
 * It has small but non-zero height. 
 */
public final class SpacerFullWidth extends Block {
	@Override
	public PlacedBlock fill(Quill receivedQuill, Layout receivedLayout) throws IOException {
		this.quill = inheritQuill(receivedQuill) ; 
		PlacedBlock placedBlock = new PlacedBlock(); 
		placedBlock.setDimensions(receivedLayout.maxWidth, Double.MIN_VALUE); // Non-zero height, so it won't trigger warnings. 
		return placedBlock ; 
	}
	@Override
	public void draw(Canvas canvas, double left, double top, double width, double height) throws IOException {} 
}