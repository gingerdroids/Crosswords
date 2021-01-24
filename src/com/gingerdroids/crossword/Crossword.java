package com.gingerdroids.crossword;

import java.util.List;

import com.gingerdroids.crossword.Grid.PlacedWord;

public class Crossword { 
	
	public final Grid grid ; 
	
	public final List<PlacedWord> placedWordList ; 
	
	public final PuzzleWord [] puzzleWords ; 
	
	public final PuzzlePhrase [] puzzlePhrases ;
	
	public final CrosswordInput crosswordInput ; 
	
	public Crossword(CrosswordInput crosswordInput, Grid grid, List<PlacedWord> placedWordList, PuzzleWord [] words, PuzzlePhrase [] phrases) {
		this.grid = grid ; 
		this.puzzleWords = words ; 
		this.puzzlePhrases = phrases ; 
		this.placedWordList = placedWordList ; 
		this.crosswordInput = crosswordInput ; 
	}

}
