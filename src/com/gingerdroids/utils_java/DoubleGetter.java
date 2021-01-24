package com.gingerdroids.utils_java;

public interface DoubleGetter<Type> {
	double getDouble(Type t); 
	
//	public static class OnArray<Type> { 
//		
//		double [] getDoubles(Type [] bags, DoubleGetter<Type> measure) { 
//			int count = bags.length ; 
//			double [] array = new double[count] ; 
//			for (int i=0 ; i<count ; i++) { 
//				array[i] = measure.getDouble(bags[i]); 
//			}
//			return array ; 
//		}
//	}
}
