package com.gingerdroids.utils_java;

/**
 * A floating-point rectangle class which precomputes width and height. 
 * <p>
 * It is agnostic whether <code>top</code> is greater or less than <code>bottom</code>. 
 * The <code>height</code> is computed as an absolute value. 
 * <p>
 * However, <code>left</code> is assumed less than <code>right</code>. 
 * @author guy
 *
 */
public class Rect_double { 
	
	public final double left ; 
	public final double top ; 
	public final double right ; 
	public final double bottom ; 
	public final double width ; 
	public final double height ; 
	
	private Rect_double(double left, double top, double right, double bottom) { 
		this.left = left ; 
		this.top = top ; 
		this.right = right ; 
		this.bottom = bottom ; 
		this.width = right - left ; 
		this.height = Math.abs(top-bottom); 
	}
	
	public static Rect_double ltrb(double left, double top, double right, double bottom) { 
		return new Rect_double(left, top, right, bottom); 
	}

}
