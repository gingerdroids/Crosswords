package com.gingerdroids.utils_java;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Builds a grid data-structure from a stream of cells. 
 * <p>
 * Usually, when you're building a grid / table / spreadsheet, you can access all the cell content information by iterating over the rows and columns. 
 * But not always, especially for sparse grids. If you can't see the point of this class, you probably don't need it. 
 * <p>
 * Data is added with the {@link #addCell(Object, Object, Object)} method. 
 * The grid data structure is returned by the {@link #getFixed(Comparator, Comparator)} method. 
 * <p>
 * I used this class building a table of movie screenings versus dates for film festivals. 
 *
 * @param <CellType> Holds the content of a single cell. For example, the time and location of a movie screening. 
 * @param <RowKeyType> Distinct value for each row of the grid. For example, the name of a movie. 
 * @param <ColumnKeyType> Distinct value for each column of the grid. For example, the date of a movie screening. 
 */
public class GridBuilder<CellType,RowKeyType,ColumnKeyType> { 
	
	private HashSet<RowKeyType> rowKeySet = new HashSet<RowKeyType>(); 
	private HashSet<ColumnKeyType> columnKeySet = new HashSet<ColumnKeyType>(); 
	private ArrayList<CellInfo> cellInfoList = new ArrayList<CellInfo>(); 
	
	/**
	 * Adds the information for a cell. 
	 * <p>
	 * It's possible for the client to call this method twice for the one cell - the same row-key and column-key. 
	 * Nothing happens until {@link #getFixed(Comparator, Comparator)} is called. 
	 * But then, the method {@link #handleClash(Object, Object, CellInfo, CellInfo)} is called. 
	 */
	public void addCell(CellType cellContent, RowKeyType rowKey, ColumnKeyType columnKey) { 
		this.rowKeySet.add(rowKey); 
		this.columnKeySet.add(columnKey); 
		this.cellInfoList.add(new CellInfo(rowKey, columnKey, cellContent)); 
	}
	
	/**
	 * Ensures a row with the given key will be in the final grid. 
	 * <p>
	 * Often, this is unnecessary, since the row-keys are harvested from the calls to {@link #addCell(Object, Object, Object)}. 
	 * But there may be a row with no filled cells that you still want to appear in the final grid. 
	 */
	public void addRowKey(RowKeyType rowKey) { 
		this.rowKeySet.add(rowKey); 
	} 

	/**
	 * Ensures a column with the given key will be in the final grid. 
	 * <p>
	 * Often, this is unnecessary, since the column-keys are harvested from the calls to {@link #addCell(Object, Object, Object)}. 
	 * But there may be a column with no filled cells that you still want to appear in the final grid. 
	 * For example, if the columns are dates, you might want to include every date in the range, even if there are some where nothing happens. 
	 */
	public void addColumnKey(ColumnKeyType columnKey) { 
		this.columnKeySet.add(columnKey); 
	} 
	
	public class CellInfo { 
		public final RowKeyType rowKey ; 
		public final ColumnKeyType columnKey ; 
		public final CellType cellContent ; 
		private CellInfo(RowKeyType rowKey, ColumnKeyType columnKey, CellType cellContent) { 
			this.rowKey = rowKey ; 
			this.columnKey = columnKey ; 
			this.cellContent = cellContent ; 
		}
	}
	
	/**
	 * Returns a {@link Fixed} instance, which has all the cell information in 2D array. 
	 * It also maps the indices of the rows and columns in the 2D array to row-keys and column-keys. 
	 * Also, there is an inverse mapping, from row-keys and column-keys to integers. 
	 * <p>
	 * Each call to this method returns a new instance of {@link Fixed}. 
	 * It's possible to call this method, then add more cells, and call it again. 
	 * You'll just get instances with different content. 
	 * 
	 * @param rowKeySorter Determines the order of the row-keys. 
	 * @param columnKeySorter Determines the order of the column-keys. 
	 * @return
	 */
	public Fixed getFixed(Comparator<RowKeyType> rowKeySorter, Comparator<ColumnKeyType> columnKeySorter) { 
		return new Fixed(rowKeySorter, columnKeySorter); 
	}
	
	public class Fixed { 
		
		public final int rowCount ; 
		public final int columnCount ; 
		public final ArrayList<RowKeyType> rowKeys ; 
		public final ArrayList<ColumnKeyType> columnKeys ; 
		public final HashMap<RowKeyType, Integer> rowKeyIndices = new HashMap<RowKeyType, Integer>(); 
		public final HashMap<ColumnKeyType, Integer> columnKeyIndices = new HashMap<ColumnKeyType, Integer>(); 
		public final Object[][] rows ; // Always are CellInfo instances (or null). First index row, second index column. 
		public final Object[][] columns ; // Always are CellInfo instances (or null). First index column, second index row. 
		
		private Fixed(Comparator<RowKeyType> rowKeySorter, Comparator<ColumnKeyType> columnKeySorter) { 
			this.rowCount = rowKeySet.size(); 
			this.columnCount = columnKeySet.size(); 
			//////  Creating the lists (unfilled)
			this.rowKeys = new ArrayList<RowKeyType>(rowCount); 
			this.columnKeys = new ArrayList<ColumnKeyType>(columnCount); 
			this.rows = new Object[rowCount][] ; 
			for (int i=0 ; i<rowCount ; i++) { 
				Object[] row = new Object[columnCount];
				Arrays.fill(row,  null); 
				rows[i] = row ; 
			}
			this.columns = new Object[columnCount][] ; 
			for (int j=0 ; j<columnCount ; j++) { 
				Object[] column = new Object[rowCount];
				Arrays.fill(column,  null); 
				columns[j] = column ; 
			}
			//////  Fill and sort the key lists
			for (RowKeyType rowKey : rowKeySet) rowKeys.add(rowKey); 
			Collections.sort(rowKeys, rowKeySorter);
			for (ColumnKeyType columnKey : columnKeySet) columnKeys.add(columnKey); 
			Collections.sort(columnKeys, columnKeySorter);
			//////  Fill key indices
			for (int i=0 ; i<rowCount ; i++) rowKeyIndices.put(rowKeys.get(i), i); 
			for (int j=0 ; j<columnCount ; j++) columnKeyIndices.put(columnKeys.get(j), j); 
			//////  Fill the cell content
			for (CellInfo cellInfo : cellInfoList) { 
				int rowIndex = rowKeyIndices.get(cellInfo.rowKey); 
				int columnIndex = columnKeyIndices.get(cellInfo.columnKey); 
				if (rows[rowIndex][columnIndex]==null) { 
					/* Normal case. */
					rows[rowIndex][columnIndex] = cellInfo ; 
				} else { 
					/* Two different content objects for this cell. Ask subclass to resolve. */
					@SuppressWarnings("unchecked")
					CellInfo cellInfoExisting = (CellInfo)(rows[rowIndex][columnIndex]);
					CellInfo chosenCellInfo = handleClash(cellInfo.rowKey, cellInfo.columnKey, cellInfoExisting, cellInfo); 
					rows[rowIndex][columnIndex] = chosenCellInfo ; 
				}
			}
		}
	}
	
	/**
	 * Called when there are two contents for the one cell. 
	 * <p>
	 * This implementation just throws a {@link RuntimeException}. 
	 * <p>
	 * For more sophisticated handling, you may want to override this method. 
	 */
	public CellInfo handleClash(RowKeyType row, ColumnKeyType column, CellInfo firstInfo, CellInfo secondInfo) { 
		throw new RuntimeException("Two items for one cell."); 
	}

}
