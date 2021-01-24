/**
 * This package contains the core of the software for laying out a set of word in a grid.
 * Essentially, building crosswords. 
 * <p>
 * The input to the apps is in lightly formatted text files. 
 * The class {@link com.gingerdroids.crossword.CrosswordInput} reads these files, extracts words & clues, 
 * and also extracts some configuration (grid dimensions, font sizes, title, etc). 
 * <p>
 * Efficiency (October 2020): the code spends much time in the copy-constructor for {@link com.gingerdroids.crossword.Grid}. 
 */
package com.gingerdroids.crossword;
