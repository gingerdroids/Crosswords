package com.gingerdroids.crossword;

/**
 * Co-ordinate pair, specifying a position in the grid, and (optionally) a direction. 
 */
public class Loc { 
	
	public final int row ; 
	public final int column ; 
	public final Boolean isAcross ; 
	
	public Loc(int row, int column, boolean isAcross) { 
		this.row = row ; 
		this.column = column ; 
		this.isAcross = isAcross ; 
	}
	
	public Loc(int row, int column) { 
		this.row = row ; 
		this.column = column ; 
		this.isAcross = null ; 
	}
	
	public Loc(Loc old) { 
		this.row = old.row ; 
		this.column = old.column ; 
		this.isAcross = old.isAcross ; 
	}
	
	public String toString() { 
		StringBuffer sb = new StringBuffer(); 
		sb.append(row); 
		sb.append(','); 
		sb.append(column); 
		if (isAcross==null) { 
			sb.append('*'); 
		} else if (isAcross) { 
			sb.append('a'); 
		} else {
			sb.append('d'); 
		}
		return sb.toString(); 
	}

}
