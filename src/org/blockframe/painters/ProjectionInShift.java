package org.blockframe.painters;

public class ProjectionInShift extends Projection { 
	
	private final double xDelta ; 
	
	private final double yDelta ; 
	
	private final double zDelta ; 
	
	private final Projection wrappedProjection ; 
	
	public ProjectionInShift(double xDelta, double yDelta, double zDelta, Projection wrappedProjection) {
		this.xDelta = xDelta ; 
		this.yDelta = yDelta ; 
		this.zDelta = zDelta ; 
		this.wrappedProjection = wrappedProjection ; 
	}

	public ProjectedCoords calculate(double xxx, double yyy, double zzz) {
		return wrappedProjection.calculate(xxx+xDelta, yyy+yDelta, zzz+zDelta);
	}

}
