package com.gingerdroids.crossword;

public interface QualityMeasureFactory {

	QualityMeasure makeQualityMeasure(int totalWordCount, int currentWordCount); 

}
