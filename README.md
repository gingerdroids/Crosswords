This repository contains Java code for generating crosswords. 

It is not a download-and-run-immediately repository. It will require configuration. A knowledge of Java will be very helpful, perhaps necessary. 


** The Java code

The main part of the Java takes a list of words (more or less) and lays them out in a crossword grid.

Another part of the Java writes these grids out as PDFs. 

There are many utility classes. Also, much cruft. 

This is not intended as a polished product. Once it is going, you will probably want to tweak it for your own use. 

There are many heuristics, and some parameterisation, used in filling the grid. They work for me, but you might want to tweak them. 


** Not standard crosswords

This generates two types of crosswords, neither of them the conventional crossword. 

Conventional crosswords are symetric. There is no attempt at symmetry here. 

Conventional crosswords keep a full blank space between words running parallel. I don't. I use a thickened line to indicate parallel words. 

There are two types of crossword here. 

Firstly, the "clueful" crosswords have phrases and clues. Each phrase has a clue. Each separate word in the phrase is a separate word in the grid. 

Secondly, the "clueless" only have words, without clues. The words are given to the puzzler, but not their location. 

Both types of crosswords coat the puzzle-words with other words from a dictionary. 

The files in the "Examples" folder illustrate the two types of crossword. 


** Requires substantial configuration

The Apache PDFBox library is required. 
pdfbox-app-2.0.16.jar

The coating requires a dictionary of words to be in a standard place. The class WordBank reads the dictionary in. 

A file "exclude.txt" can be used to list words which you don't want to use in coating. Seriously, English (and dictionaries) have many unusual words which wouldn't be much help to puzzlers. 


** Examples

The Examples directory contains an example of both the clueful and clueless crosswords. In fact, one of each: 9x9 and 10x10. The algorithm can pack words quite densely, but this makes the puzzle harder to solve. 

