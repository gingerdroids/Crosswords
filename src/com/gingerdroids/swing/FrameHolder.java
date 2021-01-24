package com.gingerdroids.swing;

import java.awt.Container;

import javax.swing.JFrame;

/**
 * Utility class with the two major lifecycle events of a frame: displaying and disposing. 
 * <p>
 * Also, provides example code for cut&paste. 
 */
public class FrameHolder { 
	
	private static FrameHolder instance ; 
	
	private JFrame frame ; 
	
	public JFrame getFrame() { 
		if (frame==null) this.frame = new JFrame(); 
		return frame ; 
	}
	
	public FrameHolder show(Container container) { 
		if (frame==null) this.frame = new JFrame(); 
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(container); 
		frame.pack();
		frame.setVisible(true);
		return this ; 
	}
	
	public void disposeFrame() { 
		if (frame==null) return ; 
		frame.dispose(); 
	}
	
	/**
	 * Returns a statically held instance, creating it if necessary. 
	 * <p>
	 * NOTE: you don't need to use the statically held instance. 
	 * In fact, it's probably better for the caller to hold the instance(s). 
	 */
	public static FrameHolder instance() { 
		if (instance==null) instance = new FrameHolder(); 
		return instance ; 
	}

}
