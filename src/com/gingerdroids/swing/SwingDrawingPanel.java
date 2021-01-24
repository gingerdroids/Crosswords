package com.gingerdroids.swing;

import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * A <code>Swing</code> panel with no layout - just for drawing in. 
 * Coordinate <code>0,0</code> is in the top-left. 
 * <p>
 * Override {@link #paintComponent(java.awt.Graphics)} to draw stuff. 
 * Call {@link #repaint()} to redraw contents. 
 * <p>
 * Has convenience methods to wrap in a {@link JFrame} and display. 
 * <p>
 * The <code>blXxxx</code> methods mimic the <code>Graphics</code> methods of similar name, 
 * but with a coordinate system based at the bottom left. 
 * <p>
 * This class can be used as the base for a Java anonymous class: {@link <a href="https://docs.oracle.com/javase/tutorial/java/javaOO/anonymousclasses.html#examples-of-anonymous-classes">Java anonymous class</a>
 */
@SuppressWarnings("serial")
public class SwingDrawingPanel extends JPanel { 
	
	public final int panelWidth ; 
	
	public final int panelHeight ; 
	
	private JFrame frame ; 
	
	public SwingDrawingPanel(int width, int height) { 
		this.panelWidth = width ; 
		this.panelHeight = height ; 
		setPreferredSize(new Dimension(width, height));
	}
	
	public JFrame showInFrame() { 
		boolean wantExitOnClose = true ;
		return showInFrame(wantExitOnClose); 
	}

	public JFrame showInFrame(boolean wantExitOnClose) {
		if (frame==null) this.frame = new JFrame(); 
		if (wantExitOnClose) frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(this); 
		frame.pack();
		frame.setVisible(true);
		return frame ;
	}
	
	public void disposeFrame() { 
		if (frame==null) return ; 
		frame.dispose(); 
	}
	
	protected void blString(Graphics g, String str, double x, double y) { 
		blString(g, str, (int)Math.round(x), (int)Math.round(y));
	}
	
	protected void blString(Graphics g, String str, int x, int y) { 
		g.drawString(str, x, panelHeight-y);
	}
	
	protected void blOval(Graphics g, int x, int y, int width, int height) { 
		g.drawOval(x, panelHeight-(y+height), width, height);
	}
	
	protected void blLine(Graphics g, double x1, double y1, double x2, double y2) { 
		blLine(g, (int)Math.round(x1), (int)Math.round(y1), (int)Math.round(x2), (int)Math.round(y2)); 
	}
	
	protected void blLine(Graphics g, int x1, int y1, int x2, int y2) { 
		g.drawLine(x1, panelHeight-y1, x2, panelHeight-y2);
	}
	

}
