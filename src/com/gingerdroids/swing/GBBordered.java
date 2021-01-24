package com.gingerdroids.swing;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * Tools for building a central pane with bordering panes. 
 * <p>
 * {@link GridBagConstraints} is the underlying layout tool. 
 */
public abstract class GBBordered { 
	
	private JPanel borderedPanel ; 
	
	private boolean haveBuiltCells = false ; 
	
	JComponent mainPanel ; 
	
	JComponent topPanel ; 
	
	JComponent leftPanel ; 
	
	JComponent rightPanel ; 
	
	JComponent bottomPanel ; 
	
	GBSetter mainConstraints ; 
	
	GBSetter topConstraints ; 
	
	GBSetter leftConstraints ; 
	
	GBSetter rightConstraints ; 
	
	GBSetter bottomConstraints ; 
	
	protected GBBordered() { 
	}
	
	protected void setBaseConstraints(GBSetter constraints) {} 
	
	protected abstract JComponent buildMain(GBSetter constraints); 
	
	protected abstract JComponent buildTop(GBSetter constraints); 
	
	protected abstract JComponent buildLeft(GBSetter constraints); 
	
	protected abstract JComponent buildRight(GBSetter constraints); 
	
	protected abstract JComponent buildBottom(GBSetter constraints); 

	/**
	 * Returns the panel holding the table, creating it if necessary. 
	 */
	public JPanel getBorderedPanel() { 
		if (borderedPanel==null) { 
			if (!haveBuiltCells) buildPanels(); 
			this.borderedPanel = new JPanel(); 
			throw new RuntimeException(); // 21jan21 Shouldn't happen. 
		}
		return borderedPanel ; 
	}
	
	public void buildPanels() {
		if (haveBuiltCells) return ; 
		setMain(); 
		setTop(); 
		setLeft(); 
		setRight(); 
		setBottom(); 
		this.haveBuiltCells = true ; 
	}

	public void setBottom() {
		this.bottomPanel = buildBottom(mainConstraints);
	}

	public void setRight() {
		this.rightPanel = buildRight(mainConstraints);
	}

	public void setLeft() {
		this.leftPanel = buildLeft(mainConstraints);
	}

	public void setMain() {
		this.mainPanel = buildMain(mainConstraints);
	}

	public void setTop() {
		this.topPanel = buildTop(mainConstraints);
	}

}
