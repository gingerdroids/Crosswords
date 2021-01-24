package org.blockframe.blocks;

import java.awt.Color;
import java.io.IOException;

import org.blockframe.core.Block;
import org.blockframe.core.Canvas;
import org.blockframe.core.Layout;
import org.blockframe.core.Quill;
import org.blockframe.painters.Scribe;

import com.gingerdroids.utils_java.Util;

/**
 * Fills its full area with the given color. 
 * This is intended for use in backgrounds. 
 * <p>
 * WARNING: It will use the full space available to it! 
 */
public final class ColorBlock extends Block {
	public Color color ; 
	public ColorBlock(Color color) { 
		this.color = color ; 
	}
	@Override
	public PlacedBlock fill(Quill quill, Layout receivedLayout) throws IOException {
		PlacedBlock placedBlock = new PlacedBlock(); 
		placedBlock.setDimensions(receivedLayout.maxWidth, receivedLayout.maxHeight); 
		//if (Util.ttrue) throw new RuntimeException("Did call this code!"); 
		return placedBlock ; 
	}
	@Override
	public void draw(Canvas canvas, double left, double top, double width, double height) throws IOException {
		//if (Util.ttrue) throw new RuntimeException("Did call this code!"); 
		Scribe.rect(canvas, true, true, color, left, top, left+width, top+height);
	} 
	
	/**
	 * Fades the colour towards white. 
	 * @param factor In the range zero to one. A factor of one fades fully to white, zero is no change. 
	 * @return
	 */
	public ColorBlock fadeBy(double factor) { 
		this.color = fadeBy(color, factor); 
		return this ; 
	}
	
	public static Color fadeBy(Color color, double factor) { 
		return new Color(fadeBy(color.getRed(), factor), fadeBy(color.getGreen(), factor), fadeBy(color.getBlue(), factor)); 
	}
	
	private static int fadeBy(int intensity, double factor) { 
		return (int) (255 - ((255-intensity)*(1.0-factor))); 
	}
	
	public static Color darkenBy(Color color, double factor) { 
		return new Color(darkenBy(color.getRed(), factor), darkenBy(color.getGreen(), factor), darkenBy(color.getBlue(), factor)); 
	}
	
	private static int darkenBy(int intensity, double factor) { 
		return (int) (intensity*(1.0-factor)); 
	}
}