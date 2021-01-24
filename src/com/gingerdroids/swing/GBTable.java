package com.gingerdroids.swing;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * Tools for building a table, or grid. 
 * The size of the table - number of rows and columns - is given in the constructor {@link GBTable#GBTable(int, int)}. 
 * Subclasses provide content by overriding the {@link #buildCellContent(int, int, GBSetter)} method. 
 * <p>
 * Cells may be omitted, but not merged. 
 * <p>
 * {@link GridBagConstraints} is the underlying layout tool. 
 */
public abstract class GBTable { 
	
	public final int rowCount ; 
	
	public final int columnCount ; 
	
	protected final GBSetter [][] childGBCs ; 
	
	protected final JComponent [][] children ; 
	
	private boolean haveBuiltCells = false ; 
	
	private JPanel tablePanel ; 
	
	protected GBTable(int rowCount, int columnCount) { 
		this.rowCount = rowCount ; 
		this.columnCount = columnCount ; 
		this.childGBCs = new GBSetter[rowCount][columnCount] ; 
		this.children = new JComponent[rowCount][columnCount] ; 
	}

	protected void buildCells() { 
		if (haveBuiltCells) return ; 
		this.haveBuiltCells = true ; 
		GBSetter baseContraints = new GBSetter(); 
		setBaseConstraints(baseContraints); 
		for (int rowIndex=0 ; rowIndex<rowCount ; rowIndex++) { 
			for (int columnIndex=0 ; columnIndex<columnCount ; columnIndex++) { 
				GBSetter cellConstraints = (GBSetter) baseContraints.clone(); 
				cellConstraints.gridx = columnIndex ; 
				cellConstraints.gridy = rowIndex ; 
				childGBCs[rowIndex][columnIndex] = cellConstraints ; 
				JComponent child = buildCellContent(rowIndex, columnIndex, cellConstraints); 
				children[rowIndex][columnIndex] = child ; 
				if (cellConstraints.gridx!=columnIndex) throw new RuntimeException("Modified gridx from "+columnIndex+" to "+cellConstraints.gridx); 
				if (cellConstraints.gridy!=rowIndex) throw new RuntimeException("Modified gridy from "+rowIndex+" to "+cellConstraints.gridy); 
			}
		}
	}
	
	protected void setBaseConstraints(GBSetter constraints) {} 
	
	/**
	 * Subclass methods should provide the content of this cell. 
	 * 
	 * @param gbSetter Setter for the {@link GridBagConstraints} applying to this cell. 
	 */
	protected abstract JComponent buildCellContent(int rowIndex, int columnIndex, GBSetter gbSetter); 

	/**
	 * Returns the panel holding the table, creating it if necessary. 
	 */
	public JPanel getTablePanel() { 
		if (tablePanel==null) { 
			if (!haveBuiltCells) buildCells(); 
			this.tablePanel = new JPanel(); 
			addTo(tablePanel, false); 
		}
		return tablePanel ; 
	}
	
	/**
	 * Adds the cells to a container. 
	 * It sets the container's layout (unless the container already has a layout). 
	 * <p>
	 * Usually called by {@link #getTablePanel()}. 
	 */
	protected Container addTo(Container container, boolean hasLayoutAlready) { 
		// TODO I suspect I've stuffed up the copying of constraints. Perhaps recode using GBSetter cloneAt. 
		if (!hasLayoutAlready) container.setLayout(new GridBagLayout());
		GBSetter constraints = new GBSetter(); 
		for (int rowIndex=0 ; rowIndex<rowCount ; rowIndex++) { 
			for (int columnIndex=0 ; columnIndex<columnCount ; columnIndex++) { 
				JComponent child = children[rowIndex][columnIndex]; 
				if (child==null) continue ; 
				childGBCs[rowIndex][columnIndex].copyTo(constraints);
				container.add(child, constraints);
			}
		}
		container.revalidate();
		return container ; 
	}
	
	public static void main(String [] args) { 
		GBTable gbTable = new GBTable(2, 2) {
			@Override
			public JComponent buildCellContent(int rowIndex, int columnIndex, GBSetter gbSetter) {
				return new JButton(""+rowIndex+","+columnIndex);
			}
		};
		JPanel panel = gbTable.getTablePanel(); 
		new FrameHolder().show(panel); 
	}
	
	

}
