package org.blockframe.painters;

public class ProjectionOutShift extends Projection { 
	
	private final double xDelta ; 
	
	private final double yDelta ; 
	
	private final Projection wrappedProjection ; 
	
	public ProjectionOutShift(double xDelta, double yDelta, Projection wrappedProjection) {
		this.xDelta = xDelta ; 
		this.yDelta = yDelta ; 
		this.wrappedProjection = wrappedProjection ; 
	}

	public ProjectedCoords calculate(double xxx, double yyy, double zzz) {
		ProjectedCoords wrappedResult = wrappedProjection.calculate(xxx, yyy, zzz); 
		ProjectedCoords result = new ProjectedCoords(wrappedResult.x+xDelta, wrappedResult.y+yDelta); 
		return result ;
	}

}
