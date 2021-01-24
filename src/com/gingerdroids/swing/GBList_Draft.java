package com.gingerdroids.swing;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.LayoutManager;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * Tools for building a list. 
 * <p> 
 * This class is meant as a simple first draft of the functionality. 
 * <p>
 * This class does not hold the data items. The caller will likely need to be able to map data items to row numbers. 
 * <p>
 * {@link GridBagConstraints} is the underlying layout tool. 
 */
public abstract class GBList_Draft<ItemType, ViewType extends JComponent> { 
	
	protected GBSetter templateConstraints = new GBSetter(); 
	
	private JPanel listPanel ; 
	
	public GBList_Draft() { 
	}
	
	protected abstract ViewType getViewFor(ItemType item); 
	
	/**
	 * Sets the constraints used to display all the items. 
	 * <p>
	 * Called once, before views are shown. (Not necessarily immediately before.) 
	 * <p>
	 * The <code>gridx</code> and <code>gridy</code> fields will be ignored. 
	 */
	public void setBaseConstraints(GBSetter constraints) {} 
	
	public JPanel getListPanel(List<ItemType> dataList) { 
		setList(dataList);
		return listPanel ; 
	}
	
	public JPanel getListPanel() { 
		if (listPanel==null) { 
			this.listPanel = new JPanel(new GridBagLayout()); 
			setBaseConstraints(templateConstraints);
		}
		return listPanel ;
	}

	public void setList(List<ItemType> dataList) { 
		getListPanel(); // Forces creation of panel. 
		listPanel.removeAll(); 
		int rowNumber = 0 ; 
		for (ItemType item : dataList) { 
			util_setItem(item, rowNumber); 
			rowNumber ++ ; 
		}
	}
	
	public void add(ItemType item) { 
		getListPanel(); 
		int rowNumber = listPanel.getComponentCount(); 
		util_setItem(item, rowNumber);
		listPanel.revalidate(); 
	} 
	
	public void insert(ItemType item, int rowNumber) { 
		getListPanel(); // Forces creation of panel. 
		int existingCount = listPanel.getComponentCount(); 
		if (rowNumber>existingCount) throw new RuntimeException("Inserting row-number too large."); 
		//// Shuffle-down rows after the insertion point. 
		Component[] children = listPanel.getComponents(); 
		GridBagLayout layout = (GridBagLayout) listPanel.getLayout(); 
		for (Component child : children) { 
			GridBagConstraints constraints = layout.getConstraints(child); 
			if (constraints.gridy>=rowNumber) { 
				constraints.gridy++ ; 
				layout.setConstraints(child, constraints);
			}
		}
		//// Insert the item. 
		util_setItem(item, rowNumber);
	} 

	/**
	 * Adds a child with the given row number. 
	 * <p>
	 * WARNING: This does NOT alter the <code>gridy</code> of other elements. The caller is responsible for that. 
	 */
	protected void util_setItem(ItemType item, int rowNumber) {
		GBSetter itemConstraints = templateConstraints.cloneAt(0, rowNumber); 
		ViewType view = getViewFor(item); 
		listPanel.add(view, itemConstraints);
	}
	
	public void remove(int rowNumber) { 
		if (listPanel==null) return ; 
		Component[] children = listPanel.getComponents(); 
		GridBagLayout layout = (GridBagLayout) listPanel.getLayout(); 
		for (Component child : children) { 
			GridBagConstraints constraints = layout.getConstraints(child); 
			if (constraints.gridy==rowNumber) { 
				listPanel.remove(child);
			} else if (constraints.gridy>=rowNumber) { 
				constraints.gridy-- ; 
				layout.setConstraints(child, constraints);
			}
		}
	}

}
