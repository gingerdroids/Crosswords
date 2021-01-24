package org.blockframe.painters;

public abstract class Projection {

	public abstract ProjectedCoords calculate(double xxx, double yyy, double zzz);  
	
	public Projection shiftObject(double xDelta, double yDelta, double zDelta) { 
		return new ProjectionInShift(xDelta, yDelta, zDelta, this); 
	}
	
	public Projection shiftImage(double xDelta, double yDelta) { 
		return new ProjectionOutShift(xDelta, yDelta, this); 
	}
	
	public Projection scaleImage(double xFactor, double yFactor) { 
		return new ProjectionOutScale(xFactor, yFactor, this); 
	}
	
	public Projection shiftCamera(double xDelta, double yDelta, double zDelta) { 
		return new ProjectionInShift(-xDelta, -yDelta, -zDelta, this); 
	}
	
	public Projection shiftScreen(double xDelta, double yDelta) { 
		return new ProjectionOutShift(-xDelta, -yDelta, this); 
	}
	
	public Projection logger() { 
		return new ProjectionLogger(this); 
	}
	
	public Projection noop() { 
		return this ; 
	}
	
	public static class ThreeDCoords { 
		
		public static final ThreeDCoords oneZeroZero = new ThreeDCoords(0, 0, 0); 
		
		public final double x ; 
		public final double y ; 
		public final double z ; 
		
		public ThreeDCoords(double x, double y, double z) { 
			this.x = x ; 
			this.y = y ; 
			this.z = z ; 
		}
	}
	
	public static class ProjectedCoords { 
		public final double x ; 
		public final double y ; 
		public final double distanceSqr ; 
		
		public ProjectedCoords(double x, double y, double distanceSqr) { 
			this.x = x ; 
			this.y = y ; 
			this.distanceSqr = distanceSqr ; 
		}
		
		public ProjectedCoords(double x, double y) { 
			this(x, y, Double.NEGATIVE_INFINITY); 
		}
		
		@Override
		public String toString() {
			return super.toString()+"("+x+","+y+")";
		}
	}

}
