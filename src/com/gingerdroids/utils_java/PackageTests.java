package com.gingerdroids.utils_java;

public class PackageTests { 
	
	public PackageTests() { 
		new Str.ClassTest(); 
		new LargestN_double.ClassTest(); 
		new LargestN_int.ClassTest(); 
		new SortedList.ClassTest(); 
		new SortByDouble.ClassTest(); 
		new SortByInteger.ClassTest(); 
		new SortByString.ClassTest(); 
		new MediansInt.ClassTest(); 
		new MediansFloat.ClassTest(); 
		new MediansDouble.ClassTest(); 
		new ArcTangentDiscrete.ClassTest(); 
		new TestBufferedWriter.ClassTest(); 
		/* Currently jun16, not many classes have unit tests. */
		System.out.println("Passed package test suite     "+this.getClass().getPackage().getName()); 
	}
	
	public static void main(String [] args) { 
		new PackageTests(); 
	}
}
