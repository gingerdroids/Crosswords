package com.gingerdroids.utils_java;

import java.util.Random;

import com.gingerdroids.utils_java.LargestN_int.ClassTest;

/**
 * Computes approximate arcsines, quickly.
 * <p>
 * Creates an array mapping short intervals of the x-to-y ratio to an arcsine. 
 * For each request, returns the value of the appropriate interval. 
 * <p>
 * No interpolation is done. Only a finite set of values can be returned. 
 *
 */
public class ArcTangentDiscrete { 
	
	public static ArcTangentDiscrete instance ;  
	
	private static final double pi = Math.PI ; 
	private static final double piTimesTwo = 2 * Math.PI ; 
	private static final double piTimesHalf = 0.5 * Math.PI ; 
	
	private final int intervalCount ; 
	
	private final double [] arctans ; 
	
	/**
	 * What to return for the undefined value at the origin. 
	 */
	private static double ohoh = Double.NaN ; 
			
	/**
	 * Set the undefined value at the origin to return zero, or Not-A-Number. 
	 * Initially, is Not-A-Number. 
	 */
	public static void set00Finite(boolean isFinite) { 
		if (isFinite) ohoh = 0 ; 
		else ohoh = Double.NaN ; 
	}
	
	public static ArcTangentDiscrete getInstance() { 
		if (instance==null) instance = new ArcTangentDiscrete(1024); 
		return instance ; 
	}

	public ArcTangentDiscrete(int intervalCount) { 
		this.intervalCount = intervalCount ; 
		this.arctans = new double[intervalCount+1] ; 
		for (int i=0 ; i<=intervalCount ; i++) arctans[i] = Math.atan(i/(double)(intervalCount)); 
	}
	
	public final double xy(double x, double y) { 
		if (x>=0) { 
			if (y>=0) { 
				return quadrant(x, y); 
			} else { 
				return piTimesTwo - quadrant(x, -y); 
			}
		} else { 
			if (y>=0) { 
				return pi - quadrant(-x, y); 
			} else { 
				return pi + quadrant(-x, -y); 
			}
		}
	}
	
	/**
	 * Computes theta in first quadrant (theta less-equal pi/2). 
	 * Requires, but does not check, that the arguments are zero or positive. 
	 */
	public final double quadrant(double x, double y) { 
		if (x>=y) {
			return octant(x, y); 
		} else { 
			return piTimesHalf - octant(y, x); 
		}
	}

	/**
	 * Computes theta in first octant (theta less-equal pi/4). 
	 * Requires, but does not check, that the arguments are zero or positive, and x>=y. 
	 */
	public final double octant(double x, double y) { 
		if (x>0) { 
			if (y>0) { 
				double ratio = y / x ; 
				int index = (int) Math.round((intervalCount*ratio)); 
				return arctans[index] ; 
			} else { 
				return 0 ; 
			}
		} else { 
			/* Here we know: x==0. Problem. */
			return ohoh ; 
		}
	}
	
	public static class ClassTest { 

		private final Random random = new Random(458645245521L); 
		
		private void testAngle(double theta) { 
			double x = Math.cos(theta); 
			double y = Math.sin(theta); 
			double computedTheta = getInstance().xy(x, y); 
			if (Math.abs(theta-computedTheta)>0.01) {
				String msg = 
						"computedTheta is " + computedTheta + 
						", theta is " + theta + 
						", theta/pi is " + (theta/Math.PI) + 
						", x is " + x + 
						", y is " + y + 
						"" ; 
				throw new RuntimeException(msg); 
			}
		}
		
		ClassTest() { 
			ArcTangentDiscrete.getInstance(); // Force lazy evaluation. 
			for (int i=0 ; i<=8 ; i++) testAngle(i*Math.PI/4); // Test major angles 
			for (int i=0 ; i<1000 ; i++) testAngle(2*Math.PI*random.nextDouble()); // Test random angles
			//////  Passed! Bye bye.  
			System.out.println("Passed test suite "+this.getClass().getCanonicalName()); 
		}
	}

//	public static void main(String [] args) { 
//		new ClassTest(); 
//	}

}
