package com.gingerdroids.swing;

import java.awt.GridBagConstraints;

/**
 * Subclass of {@link GridBagConstraints} which allows chained setting of fields. 
 */
@SuppressWarnings("serial")
public class GBSetter extends GridBagConstraints { 
	
	/* Cell 0,0 is at the top-left. */
	
	/**
	 * Set the {@link #weightx} field to <code>1.0</code> 
	 * <p>
	 * They default to <code>0.0</code>. 
	 */
	public GBSetter weightX() { 
		this.weightx = 1.0 ; 
		return this ; 
	} 
	
	/**
	 * Set the {@link #weighty} field to <code>1.0</code> 
	 * <p>
	 * They default to <code>0.0</code>. 
	 */
	public GBSetter weightY() { 
		this.weighty = 1.0 ; 
		return this ; 
	} 
	
	/**
	 * Set the {@link #weightx} and {@link #weighty} fields to <code>1.0</code> 
	 * <p>
	 * They default to <code>0.0</code>. 
	 */
	public GBSetter weightXY() { 
		this.weightx = 1.0 ; 
		this.weighty = 1.0 ; 
		return this ; 
	} 
	
	public void copyTo(GridBagConstraints other) { 
		other.anchor = this.anchor ; 
		other.fill = this.fill ; 
		other.gridheight = this.gridheight ; 
		other.gridwidth = this.gridwidth ; 
		other.gridx = this.gridx ; 
		other.gridy = this.gridy ; 
		other.insets = this.insets ; 
		other.ipadx = this.ipadx ; 
		other.ipady = this.ipady ; 
		other.weightx = this.weightx ; 
		other.weighty = this.weighty ; 
	} 
	
	public GBSetter cloneAt(int gridx, int gridy) { 
		GBSetter clone = new GBSetter(); 
		clone.gridx = gridx ; 
		clone.gridy = gridy ; 
		clone.anchor = this.anchor ; 
		clone.fill = this.fill ; 
		clone.gridheight = this.gridheight ; 
		clone.gridwidth = this.gridwidth ; 
		clone.insets = this.insets ; 
		clone.ipadx = this.ipadx ; 
		clone.ipady = this.ipady ; 
		clone.weightx = this.weightx ; 
		clone.weighty = this.weighty ;
		return clone ; 
	} 

}
