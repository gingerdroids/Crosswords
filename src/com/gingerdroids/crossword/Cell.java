package com.gingerdroids.crossword;

/**
 * Single cell within a grid. 
 */
public class Cell { 
	
	public static final char space = ' ' ; 
	
	final int row ; 
	final int column ; 
	
	public char ch = space ; // Ideally should be private, but very frequently accessed. 
	
	/**
	 * The clue-number associated with this cell. 
	 * <p>
	 * Most cells don't have a number - this field has value zero for them. 
	 */
	public int wordNumber = 0 ; // Flag value. First proper word-number is 1. 
	
	public boolean isInAcross = false ; 
	
	public boolean isInDown = false ; 
	
	public boolean isPuzzleCell = false ; 
	
	public void reset() { 
		this.ch = space ; 
		this.wordNumber = 0 ; 
		this.isInAcross = false ; 
		this.isInDown = false ; 
		this.isPuzzleCell = false ; 
	}
	
	Cell(int row, int column) { 
		this.row = row ; 
		this.column = column ; 
		reset(); 
	}
	
	Cell(Cell old) { 
		this.row = old.row ; 
		this.column = old.column ; 
		this.ch = old.ch ; 
		this.isInAcross = old.isInAcross ; 
		this.isInDown = old.isInDown ; 
	}
	
	void setChar(char ch, boolean isAcross) { 
		if (this.ch!=space && this.ch!=ch) throw new RuntimeException("Cell already has letter '"+this.ch+"', cannot insert '"+ch+"'"); 
		if (isAcross) { 
			if (isInAcross) throw new RuntimeException("Already have an across letter."); 
			this.isInAcross = true ; 
		} else { 
			if (isInDown) throw new RuntimeException("Already have a down letter."); 
			this.isInDown = true ; 
		}
		this.ch = ch ; 
	} 
	
	public boolean isCrossingCell() { 
		return isInAcross && isInDown ; 
	}

	public boolean hasLetter() {
		return ch!=space ;
	}

}
